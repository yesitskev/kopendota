package io.github.yesitskev.kopendota

import java.io.IOException

/**
 * Base failure type raised when a call to the OpenDota API cannot be completed.
 *
 * It extends [IOException] because that is the only failure OkHttp will carry from a call back to whoever made it, so
 * anything else would reach a caller wrapped in one. A call that fails this way therefore surfaces the failure
 * itself rather than something standing in for it.
 *
 * Only [OpenDotaQueueLimitExceeded] extends it so far. The failures below are still placeholders, so an HTTP error
 * surfaces as the [retrofit2.HttpException] Retrofit raises itself.
 *
 * @param message What went wrong.
 * @param cause What this failure followed from, where it followed from another.
 */
open class OpenDotaClientException(message: String, cause: Throwable? = null) : IOException(message, cause)

/**
 * Raised when a request cannot be queued because [RateLimiter.queueLimit] requests are already waiting for a rate
 * limit to let them through.
 *
 * A queue limit of `0` or less is unlimited, so a client configured that way never raises this.
 *
 * @property queueLimit How many requests the limiter allows to wait at once.
 * @property queueDepth How many were waiting when this request arrived.
 */
class OpenDotaQueueLimitExceeded(val queueLimit: Int, val queueDepth: Int) :
    OpenDotaClientException(
        "Cannot queue the request: $queueDepth requests are waiting for a rate limit, " +
            "which is beyond the queue limit of $queueLimit",
    )

/**
 * Placeholder for the failure that will represent an HTTP 404 from the OpenDota API, returned for an unknown match,
 * player, team or league.
 *
 * It does not extend [OpenDotaClientException] yet and nothing throws it.
 */
class OpenDotaNotFoundException

/**
 * Placeholder for the failure that will represent an HTTP 429 from the OpenDota API, returned once the rate limit for
 * the current API key — or for anonymous access — has been exhausted.
 *
 * It does not extend [OpenDotaClientException] yet and nothing throws it.
 */
class OpenDotaRateLimitException

/**
 * Placeholder for the failure that will represent an HTTP 5xx from the OpenDota API.
 *
 * It does not extend [OpenDotaClientException] yet and nothing throws it.
 */
class OpenDotaServerException
