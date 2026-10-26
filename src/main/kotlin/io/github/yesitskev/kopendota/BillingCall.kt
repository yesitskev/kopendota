package io.github.yesitskev.kopendota

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Hands every call Retrofit makes to a [BillingCall], so that each one is held to the rate limits before it is sent.
 *
 * This sits where the OkHttp client would, rather than in an interceptor, because a request may have to wait for a
 * rate limit and an interceptor can only wait by blocking a dispatcher thread. A queued request here is a suspended
 * coroutine and holds no thread at all.
 *
 * @param delegate What actually sends a request once it has been admitted.
 * @param rateLimiter Limiter each request is admitted by.
 */
internal class BillingCallFactory(private val delegate: Call.Factory, private val rateLimiter: RateLimiter) :
    Call.Factory {

    override fun newCall(request: Request): Call =
        BillingCall(original = request, delegate = delegate, rateLimiter = rateLimiter)
}

/**
 * One call, queued until a billing tier has room and then sent on that tier.
 *
 * A free-tier call answered with an HTTP 429 is queued again and sent on the subscription tier, where there is a
 * subscription to send it on, so the caller sees the second response rather than the limit.
 *
 * Everything but sending and cancelling is delegated to [unsent], a call the delegate builds from the request as it
 * arrived and that is never sent. That covers the timeout, the tags and the event listeners of a call OkHttp has not
 * created yet, and it keeps this class honest as OkHttp adds members to [Call]. Nothing outside the library holds a
 * [BillingCall], so the tags and listeners of the sent call being out of reach costs a caller nothing.
 *
 * @param original Request as Retrofit built it, before a tier has been chosen.
 * @param delegate What actually sends a request once it has been admitted.
 * @param rateLimiter Limiter this call is admitted by.
 * @param unsent Call standing in for the one that will be sent, for everything other than sending it.
 */
internal class BillingCall(
    private val original: Request,
    private val delegate: Call.Factory,
    private val rateLimiter: RateLimiter,
    private val unsent: Call = delegate.newCall(original),
) : Call by unsent {

    /** Whether the call has been handed to [enqueue] or [execute], neither of which may be called twice. */
    private val enqueued = AtomicBoolean(false)

    /** Whether the call has been cancelled, which it may be before it has been sent or even admitted. */
    private val canceled = AtomicBoolean(false)

    /**
     * Whether the caller has been told the outcome.
     *
     * A response and a failure can reach the callback from either the coroutine doing the work or the end of the job
     * that was running it, so this makes sure the caller hears exactly one of them.
     */
    private val answered = AtomicBoolean(false)

    /** The call now out at the API, so that cancelling this one cancels that too. */
    private val sending = AtomicReference<Call?>(null)

    /** The coroutine waiting for a tier and sending on it, so that cancelling this one cancels that too. */
    private val waiting = AtomicReference<Job?>(null)

    override fun request(): Request = original

    override fun enqueue(responseCallback: Callback) {
        check(enqueued.compareAndSet(false, true)) { "Already Executed" }

        val job =
            rateLimiter.scope.launch {
                try {
                    val response = send()
                    if (answered.compareAndSet(false, true)) {
                        responseCallback.onResponse(this@BillingCall, response)
                    } else {
                        response.closeQuietly()
                    }
                } catch (cancellation: CancellationException) {
                    fail(responseCallback, IOException("Canceled", cancellation))
                } catch (failure: IOException) {
                    fail(responseCallback, failure)
                } catch (failure: Throwable) {
                    fail(responseCallback, IOException(failure))
                }
            }

        // A scope that is already cancelled — a closed client — never runs the block above, so the end of the job
        // is what answers the caller rather than leaving it waiting on a call that will never be made.
        job.invokeOnCompletion { cause -> if (cause != null) fail(responseCallback, IOException("Canceled", cause)) }

        waiting.set(job)
        if (canceled.get()) job.cancel()
    }

    override fun execute(): Response {
        check(enqueued.compareAndSet(false, true)) { "Already Executed" }

        return runBlocking { send() }
    }

    override fun cancel() {
        canceled.set(true)
        sending.get()?.cancel()
        waiting.get()?.cancel()
    }

    override fun isExecuted(): Boolean = enqueued.get()

    override fun isCanceled(): Boolean = canceled.get()

    override fun clone(): Call = BillingCall(original = original, delegate = delegate, rateLimiter = rateLimiter)

    /**
     * Reports a failure to the caller, but only where nothing has been reported yet.
     *
     * @param responseCallback Callback awaiting this call.
     * @param failure What went wrong.
     */
    private fun fail(responseCallback: Callback, failure: IOException) {
        if (answered.compareAndSet(false, true)) responseCallback.onFailure(this, failure)
    }

    /**
     * Sends the request on the tier the limiter admits it to, retrying on the subscription tier where the free tier
     * answers that its limit has been reached.
     *
     * @return The response the caller sees.
     */
    private suspend fun send(): Response {
        val firstTier = rateLimiter.acquire()
        val first = sendOn(firstTier)

        if (!rateLimiter.shouldRetryOnSubscriptionTier(firstTier, first.code)) {
            rateLimiter.release(tier = firstTier, statusCode = first.code, retryAfter = first.retryAfter())

            return first
        }

        rateLimiter.release(tier = firstTier, statusCode = first.code, retryAfter = first.retryAfter())
        first.closeQuietly()

        val retryTier = rateLimiter.acquire()
        val retry = sendOn(retryTier)
        rateLimiter.release(tier = retryTier, statusCode = retry.code, retryAfter = retry.retryAfter())

        return retry
    }

    /**
     * Sends the request on one tier, handing the slot back where it never gets a response.
     *
     * @param tier Tier to send on.
     * @return The response the API gave, whatever its status code.
     */
    private suspend fun sendOn(tier: BillingTier): Response =
        try {
            if (canceled.get()) throw IOException("Canceled")

            val call = delegate.newCall(rateLimiter.subscription.requestOn(tier, original))
            sending.set(call)
            if (canceled.get()) call.cancel()

            call.await()
        } catch (failure: Throwable) {
            rateLimiter.release(tier = tier, statusCode = null)
            throw failure
        }
}

/**
 * Awaits an OkHttp call without holding a thread, cancelling it where the coroutine awaiting it is cancelled.
 *
 * @return The response the call gave.
 */
private suspend fun Call.await(): Response = suspendCancellableCoroutine { continuation ->
    enqueue(
        object : Callback {
            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }

            override fun onFailure(call: Call, e: IOException) {
                if (!continuation.isCancelled) continuation.resumeWithException(e)
            }
        },
    )

    continuation.invokeOnCancellation { cancel() }
}

/**
 * How long a response asked the caller to wait, from its `Retry-After` header.
 *
 * Only the delta-seconds form is read, which is what OpenDota sends; an HTTP date is treated as no header at all.
 *
 * @return The wait the response asked for, or `null` where it asked for none.
 */
private fun Response.retryAfter(): Duration? = header("Retry-After")?.trim()?.toLongOrNull()?.seconds

/** Closes a response body that is being thrown away, without letting the close hide the reason it was discarded. */
private fun Response.closeQuietly() {
    try {
        close()
    } catch (_: IOException) {
        // A response that is already being discarded has nothing left to report.
    }
}
