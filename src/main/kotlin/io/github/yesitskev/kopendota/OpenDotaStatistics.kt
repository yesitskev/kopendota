package io.github.yesitskev.kopendota

/**
 * What one allowance has spent of one period, against the ceiling that period allows.
 *
 * [used] is the usage, [limit] the ceiling, and [remaining] the capacity still to be spent. A `null` [limit] is an
 * unlimited period, which leaves [remaining] and [fractionUsed] `null` too — there is no ceiling to measure against.
 *
 * @property used Requests counted against the period so far.
 * @property limit Requests the period allows, or `null` where it is unlimited.
 */
data class OpenDotaUsage(val used: Long, val limit: Long?) {

    /** Requests still to be spent in the period, or `null` where it is unlimited. Never negative. */
    val remaining: Long?
        get() = limit?.let { (it - used).coerceAtLeast(0L) }

    /**
     * Share of the ceiling spent, where `1.0` is the ceiling exactly, or `null` where the period is unlimited.
     *
     * A burst that is admitted before earlier requests are billed can carry this past `1.0`, which is reported as it
     * stands rather than capped, so that an overshoot is visible.
     */
    val fractionUsed: Double?
        get() = limit?.let { if (it == 0L) 1.0 else used.toDouble() / it.toDouble() }

    /** Whether the ceiling has been reached, which an unlimited period never is. */
    val isExhausted: Boolean
        get() = limit != null && used >= limit
}

/**
 * A snapshot of what a client is doing and what it has spent.
 *
 * Read it with [OpenDotaClient.statistics]. The counts are read one after another rather than under a single lock, so
 * a snapshot taken while calls are in flight describes a moment that is already passing — it is for reporting on a
 * client, not for deciding whether the next call will be admitted.
 *
 * ```kotlin
 * val statistics = client.statistics()
 *
 * println("${statistics.inFlightRequests} in flight, ${statistics.queuedRequests} waiting")
 * println("free day: ${statistics.freeTierDay.used}/${statistics.freeTierDay.limit}")
 * println("free minute: ${statistics.freeTierMinute.remaining} left of the sliding minute")
 * ```
 *
 * @property queuedRequests Requests waiting for a rate limit to let them through.
 * @property inFlightRequests Requests that have been admitted and are out at the API.
 * @property freeTierDay What the free allowance has spent of the current UTC day.
 * @property freeTierMinute What the free allowance has spent of the sliding minute that ends now.
 * @property subscriptionTierDay What the subscription has spent of the current UTC day.
 * @property subscriptionTierMinute What the subscription has spent of the sliding minute that ends now.
 */
data class OpenDotaStatistics(
    val queuedRequests: Int,
    val inFlightRequests: Int,
    val freeTierDay: OpenDotaUsage,
    val freeTierMinute: OpenDotaUsage,
    val subscriptionTierDay: OpenDotaUsage,
    val subscriptionTierMinute: OpenDotaUsage,
)
