package io.github.yesitskev.kopendota

/**
 * What one [BillingTier] has left to spend, and whether a request can go out on it right now.
 *
 * @property tier Tier this describes.
 * @property day What the tier has spent of the current UTC day.
 * @property minute What the tier has spent of the sliding minute that ends now.
 * @property isAvailable Whether a request can go out on the tier: it exists, neither period is spent once the
 *   requests already in flight on it are counted, and it is not backing off from an HTTP 429.
 */
data class BillingTierCapacity(
    val tier: BillingTier,
    val day: OpenDotaUsage,
    val minute: OpenDotaUsage,
    val isAvailable: Boolean,
)

/**
 * Chooses which [BillingTier] a request goes out on, or holds it back until one has room.
 *
 * [FreeTierFirstRequestStrategy] is the one a client uses unless another is given.
 */
fun interface RequestStrategy {

    /**
     * Picks the tier the next request goes out on.
     *
     * @param freeTier What the free allowance has left.
     * @param subscriptionTier What the subscription has left.
     * @return Tier to send on, or `null` to queue the request until a tier has room.
     */
    fun select(freeTier: BillingTierCapacity, subscriptionTier: BillingTierCapacity): BillingTier?
}

/**
 * Spends the free allowance first and falls back to the subscription once the free limits are reached.
 *
 * A request waits only where neither tier has room — a spent free allowance is not a reason to queue while the
 * subscription still has capacity.
 */
object FreeTierFirstRequestStrategy : RequestStrategy {

    override fun select(freeTier: BillingTierCapacity, subscriptionTier: BillingTierCapacity): BillingTier? =
        when {
            freeTier.isAvailable -> freeTier.tier
            subscriptionTier.isAvailable -> subscriptionTier.tier
            else -> null
        }
}
