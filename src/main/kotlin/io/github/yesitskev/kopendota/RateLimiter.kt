package io.github.yesitskev.kopendota

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration

/**
 * Holds requests to what a [BillingSubscription] allows, picking the tier each one goes out on.
 *
 * A request is admitted as soon as [RequestStrategy] finds a tier with room. Where neither tier has room the request
 * waits, and [queueLimit] bounds how many may wait at once. The limiter counts the requests it has admitted but not
 * yet billed as spent, so a burst of callers cannot all read an empty allowance and overshoot it together.
 *
 * Only a billed response moves a count: an HTTP 404, 429 or 500 is not billed, which matches what OpenDota charges
 * for. An HTTP 429 says a limit has been reached whatever the counts say, so the tier that answered it backs off for
 * the `Retry-After` it gave, or for a minute where it gave none.
 *
 * A client builds one of these, and [OpenDotaClient.rateLimiter] reaches it. Close the client to shut down the scope
 * queued requests run on.
 *
 * @property subscription Subscription whose counts and limits the requests are held to.
 * @property queueLimit How many requests may wait for a limit at once. `0` or less is unlimited.
 * @property strategy Which tier each request goes out on. Free allowance first by default.
 */
class RateLimiter(
    val subscription: BillingSubscription,
    val queueLimit: Int = 0,
    val strategy: RequestStrategy = FreeTierFirstRequestStrategy,
) : AutoCloseable {

    /** Serialises capacity checks, so two callers cannot be admitted into one slot. */
    private val mutex = Mutex()

    /** Requests waiting for a limit to let them through. */
    private val queued = AtomicInteger()

    /** Requests admitted and out at the API. */
    private val inFlight = AtomicInteger()

    /** Requests out on each tier, counted as spent until the response says whether it is billed. */
    private val pending = BillingTier.entries.associateWith { AtomicInteger() }

    /** When each tier may be tried again, for a tier that has answered an HTTP 429. */
    private val backOffUntil = ConcurrentHashMap<BillingTier, Instant>()

    /** Scope the queued requests wait on, cancelled by [close]. */
    internal val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Waits for a tier with room and takes a slot on it.
     *
     * The slot is held until [release] hands it back, so every call that returns from here must be released.
     *
     * @return Tier the request is to go out on.
     * @throws OpenDotaQueueLimitExceeded Where [queueLimit] requests are already waiting.
     */
    suspend fun acquire(): BillingTier {
        val depth = queued.incrementAndGet()

        try {
            if (queueLimit > 0 && depth > queueLimit) {
                throw OpenDotaQueueLimitExceeded(queueLimit = queueLimit, queueDepth = depth)
            }

            while (true) {
                mutex.withLock { reserve() }?.let { return it }
                delay(waitBeforeRetry())
            }
        } finally {
            queued.decrementAndGet()
        }
    }

    /**
     * Hands a slot back, billing the request where the response was one OpenDota charges for.
     *
     * @param tier Tier the request went out on, as [acquire] returned it.
     * @param statusCode Status code the API answered with, or `null` where the request never got a response.
     * @param retryAfter How long the response asked the caller to wait, where it carried a `Retry-After`.
     */
    suspend fun release(tier: BillingTier, statusCode: Int?, retryAfter: Duration? = null) {
        try {
            if (statusCode == HTTP_TOO_MANY_REQUESTS) backOff(tier, retryAfter)
            if (statusCode != null && statusCode !in UNBILLED_STATUS_CODES) subscription.record(tier)
        } finally {
            pending.getValue(tier).decrementAndGet()
            inFlight.decrementAndGet()
        }
    }

    /**
     * Whether a response should be tried again on the subscription tier.
     *
     * An HTTP 429 on the free allowance means the limit was reached ahead of the counts, so the request is worth
     * sending again on the subscription — where there is one to send it on.
     *
     * @param tier Tier the request went out on.
     * @param statusCode Status code the API answered with.
     * @return `true` where the request should be sent again on [BillingTier.SUBSCRIPTION].
     */
    fun shouldRetryOnSubscriptionTier(tier: BillingTier, statusCode: Int): Boolean =
        tier == BillingTier.FREE && statusCode == HTTP_TOO_MANY_REQUESTS && subscription.hasSubscriptionTier

    /**
     * What one tier has left to spend right now.
     *
     * @param tier Tier to read.
     * @return The tier's usage, and whether a request can go out on it.
     */
    suspend fun capacity(tier: BillingTier): BillingTierCapacity {
        val limits = subscription.limitsFor(tier)
        val usage = subscription.usage(tier)
        val outstanding = pending.getValue(tier).get().toLong()
        val exists = tier == BillingTier.FREE || subscription.hasSubscriptionTier

        return BillingTierCapacity(
            tier = tier,
            day = OpenDotaUsage(used = usage.perDay, limit = limits.perDay),
            minute = OpenDotaUsage(used = usage.perMinute, limit = limits.perMinute),
            isAvailable =
                exists &&
                    !isBackingOff(tier) &&
                    hasRoom(spent = usage.perDay + outstanding, limit = limits.perDay) &&
                    hasRoom(spent = usage.perMinute + outstanding, limit = limits.perMinute),
        )
    }

    /**
     * A snapshot of what the limiter is doing and what each tier has spent.
     *
     * @return The current statistics.
     */
    suspend fun statistics(): OpenDotaStatistics {
        val freeTier = capacity(BillingTier.FREE)
        val subscriptionTier = capacity(BillingTier.SUBSCRIPTION)

        return OpenDotaStatistics(
            queuedRequests = queued.get(),
            inFlightRequests = inFlight.get(),
            freeTierDay = freeTier.day,
            freeTierMinute = freeTier.minute,
            subscriptionTierDay = subscriptionTier.day,
            subscriptionTierMinute = subscriptionTier.minute,
        )
    }

    /** Cancels the scope queued requests wait on. A cancelled limiter admits nothing further. */
    override fun close() {
        scope.cancel()
    }

    /**
     * Takes a slot on whichever tier the strategy picks, counting it as spent until the response arrives.
     *
     * @return Tier a slot was taken on, or `null` where neither tier has room.
     */
    private suspend fun reserve(): BillingTier? {
        val tier =
            strategy.select(
                freeTier = capacity(BillingTier.FREE),
                subscriptionTier = capacity(BillingTier.SUBSCRIPTION),
            ) ?: return null

        pending.getValue(tier).incrementAndGet()
        inFlight.incrementAndGet()

        return tier
    }

    /**
     * How long to wait before looking for room again.
     *
     * A per-second count ages out of the sliding minute every second, so a second is the longest wait that can miss
     * nothing. A tier backing off from an HTTP 429 shortens that where it comes back sooner.
     *
     * @return How long to wait.
     */
    private fun waitBeforeRetry(): Duration {
        val now = subscription.clock.instant()
        val soonest = backOffUntil.values.filter { it.isAfter(now) }.minOrNull() ?: return RETRY_INTERVAL

        return minOf(RETRY_INTERVAL, java.time.Duration.between(now, soonest).toKotlinDuration())
            .coerceAtLeast(MINIMUM_RETRY_INTERVAL)
    }

    /**
     * Holds a tier back after it has answered that its limit is reached, whatever its counts say.
     *
     * @param tier Tier that answered with an HTTP 429.
     * @param retryAfter How long the response asked the caller to wait, or `null` where it asked for nothing, in
     *   which case the tier waits out [DEFAULT_BACK_OFF].
     */
    private fun backOff(tier: BillingTier, retryAfter: Duration?) {
        val wait = retryAfter ?: DEFAULT_BACK_OFF
        backOffUntil[tier] = subscription.clock.instant().plusMillis(wait.inWholeMilliseconds)
    }

    /**
     * Whether a tier is still waiting out an HTTP 429, forgetting the wait once it has passed.
     *
     * @param tier Tier to check.
     * @return `true` where the tier is not to be sent on yet.
     */
    private fun isBackingOff(tier: BillingTier): Boolean {
        val until = backOffUntil[tier] ?: return false
        if (subscription.clock.instant().isBefore(until)) return true

        backOffUntil.remove(tier, until)

        return false
    }

    /**
     * Whether one more request fits under a limit.
     *
     * @param spent What has been counted against the period, including the requests still in flight on it.
     * @param limit What the period allows, or `null` where it is unlimited.
     * @return `true` where one more request fits.
     */
    private fun hasRoom(spent: Long, limit: Long?): Boolean = limit == null || spent < limit

    companion object {

        /** HTTP 429, which OpenDota answers with once a rate limit has been reached. */
        const val HTTP_TOO_MANY_REQUESTS: Int = 429

        /**
         * Status codes OpenDota does not bill, so a response carrying one moves no count: 404 for something it does
         * not hold, 429 for a limit already reached, and 500 for a failure of its own.
         */
        val UNBILLED_STATUS_CODES: Set<Int> = setOf(404, HTTP_TOO_MANY_REQUESTS, 500)

        /** How long a tier backs off after an HTTP 429 that carried no `Retry-After`, which is the minute window. */
        private val DEFAULT_BACK_OFF: Duration = 60.seconds

        /** How long a queued request waits between looking for room, which is how often the sliding minute moves. */
        private val RETRY_INTERVAL: Duration = 1.seconds

        /** Floor on the wait, so that a back-off ending in a moment cannot spin the queue. */
        private val MINIMUM_RETRY_INTERVAL: Duration = 10.milliseconds
    }
}
