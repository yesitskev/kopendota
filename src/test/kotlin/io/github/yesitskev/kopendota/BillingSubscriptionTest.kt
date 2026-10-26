package io.github.yesitskev.kopendota

import kotlinx.coroutines.test.runTest
import okhttp3.Request
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class BillingSubscriptionTest {

    private val clock = MutableClock(MutableClock.START)

    private fun subscription(
        apiKey: String? = "secret-key",
        cache: BillingSubscriptionCache = InMemoryBillingSubscriptionCache(clock),
        freeTierLimits: BillingLimits = BillingLimits.FREE_TIER,
        subscriptionTierLimits: BillingLimits = BillingLimits.SUBSCRIPTION_TIER,
    ): BillingSubscription =
        BillingSubscription(
            apiKey = apiKey,
            cache = cache,
            freeTierLimits = freeTierLimits,
            subscriptionTierLimits = subscriptionTierLimits,
            clock = clock,
        )

    @Nested
    inner class BillingSubscriptionLimits {

        @Test
        fun `allows what the free tier is documented to allow`() {
            assertEquals(3_000L, BillingLimits.FREE_TIER.perDay)
            assertEquals(60L, BillingLimits.FREE_TIER.perMinute)
        }

        @Test
        fun `allows an unlimited day and three thousand a minute on a subscription`() {
            assertNull(BillingLimits.SUBSCRIPTION_TIER.perDay)
            assertEquals(3_000L, BillingLimits.SUBSCRIPTION_TIER.perMinute)
        }

        @ParameterizedTest
        @CsvSource("-1,60", "3000,-1", "-5,-5")
        fun `rejects a negative limit`(perDay: Long, perMinute: Long) {
            assertThrows<IllegalArgumentException> { BillingLimits(perDay = perDay, perMinute = perMinute) }
        }

        @Test
        fun `allows a zero limit, which switches a tier off`() {
            val limits = BillingLimits(perDay = 0, perMinute = 0)

            assertEquals(0L, limits.perDay)
            assertEquals(0L, limits.perMinute)
        }

        @Test
        fun `returns the limits of the tier asked for`() {
            val subscription =
                subscription(
                    freeTierLimits = BillingLimits(perDay = 1, perMinute = 2),
                    subscriptionTierLimits = BillingLimits(perDay = 3, perMinute = 4),
                )

            assertEquals(BillingLimits(perDay = 1, perMinute = 2), subscription.limitsFor(BillingTier.FREE))
            assertEquals(BillingLimits(perDay = 3, perMinute = 4), subscription.limitsFor(BillingTier.SUBSCRIPTION))
        }
    }

    @Nested
    inner class BillingSubscriptionApiKey {

        @Test
        fun `has a subscription tier when a key is supplied`() {
            assertTrue(subscription(apiKey = "secret-key").hasSubscriptionTier)
        }

        @ParameterizedTest
        @ValueSource(strings = ["", " ", "\t", "   "])
        fun `has no subscription tier for a blank key`(apiKey: String) {
            assertFalse(subscription(apiKey = apiKey).hasSubscriptionTier)
        }

        @Test
        fun `has no subscription tier without a key`() {
            assertFalse(subscription(apiKey = null).hasSubscriptionTier)
        }

        @Test
        fun `puts the key on a subscription tier request`() {
            val request = Request.Builder().url("https://api.opendota.com/api/heroes").build()

            val sent = subscription().requestOn(BillingTier.SUBSCRIPTION, request)

            assertEquals("secret-key", sent.url.queryParameter("api_key"))
        }

        @Test
        fun `leaves a free tier request anonymous`() {
            val request = Request.Builder().url("https://api.opendota.com/api/heroes").build()

            val sent = subscription().requestOn(BillingTier.FREE, request)

            assertNull(sent.url.queryParameter("api_key"))
        }

        @Test
        fun `sends no key on a subscription tier request when there is no key to send`() {
            val request = Request.Builder().url("https://api.opendota.com/api/heroes").build()

            val sent = subscription(apiKey = null).requestOn(BillingTier.SUBSCRIPTION, request)

            assertNull(sent.url.queryParameter("api_key"))
        }

        @Test
        fun `keeps the rest of the url intact`() {
            val request = Request.Builder().url("https://api.opendota.com/api/players/1/wl?hero_id=1").build()

            val sent = subscription().requestOn(BillingTier.SUBSCRIPTION, request)

            assertEquals("/api/players/1/wl", sent.url.encodedPath)
            assertEquals("1", sent.url.queryParameter("hero_id"))
        }
    }

    @Nested
    inner class BillingSubscriptionCounting {

        @Test
        fun `counts no requests before any are made`() = runTest {
            val usage = subscription().usage(BillingTier.FREE)

            assertEquals(0L, usage.perDay)
            assertEquals(0L, usage.perMinute)
        }

        @Test
        fun `counts a request against the day and the minute`() = runTest {
            val subscription = subscription()

            subscription.record(BillingTier.FREE)

            val usage = subscription.usage(BillingTier.FREE)
            assertEquals(1L, usage.perDay)
            assertEquals(1L, usage.perMinute)
        }

        @Test
        fun `counts each tier separately`() = runTest {
            val subscription = subscription()

            subscription.record(BillingTier.FREE)
            subscription.record(BillingTier.FREE)
            subscription.record(BillingTier.SUBSCRIPTION)

            assertEquals(2L, subscription.usage(BillingTier.FREE).perDay)
            assertEquals(1L, subscription.usage(BillingTier.SUBSCRIPTION).perDay)
        }

        @Test
        fun `keeps a request in the minute across the seconds it slides over`() = runTest {
            val subscription = subscription()
            subscription.record(BillingTier.FREE)

            clock.advance(59.seconds)

            assertEquals(1L, subscription.usage(BillingTier.FREE).perMinute)
        }

        @Test
        fun `drops a request out of the minute once it has slid past`() = runTest {
            val subscription = subscription()
            subscription.record(BillingTier.FREE)

            clock.advance(60.seconds)

            assertEquals(0L, subscription.usage(BillingTier.FREE).perMinute)
            assertEquals(1L, subscription.usage(BillingTier.FREE).perDay)
        }

        @Test
        fun `slides the minute one request at a time rather than resetting it`() = runTest {
            val subscription = subscription()
            subscription.record(BillingTier.FREE)
            clock.advance(30.seconds)
            subscription.record(BillingTier.FREE)

            assertEquals(2L, subscription.usage(BillingTier.FREE).perMinute)

            clock.advance(31.seconds)

            assertEquals(1L, subscription.usage(BillingTier.FREE).perMinute)
        }

        @Test
        fun `sums every second of the minute`() = runTest {
            val subscription = subscription()

            repeat(60) {
                subscription.record(BillingTier.FREE)
                clock.advance(1.seconds)
            }

            assertEquals(59L, subscription.usage(BillingTier.FREE).perMinute)
            assertEquals(60L, subscription.usage(BillingTier.FREE).perDay)
        }

        @Test
        fun `starts the day count again on the next day`() = runTest {
            val subscription = subscription()
            subscription.record(BillingTier.FREE)

            clock.advance(1.days)

            assertEquals(0L, subscription.usage(BillingTier.FREE).perDay)
        }

        @Test
        fun `reads the counts back through a cache it does not own`() = runTest {
            val cache = RecordingCache()
            val subscription = subscription(cache = cache)

            subscription.record(BillingTier.FREE)
            subscription.record(BillingTier.FREE)

            assertEquals(2L, subscription.usage(BillingTier.FREE).perDay)
            assertTrue(cache.keys.any { it.contains(":free:day:") }, "No day key written: ${cache.keys}")
            assertTrue(cache.keys.any { it.contains(":free:second:") }, "No second key written: ${cache.keys}")
        }

        @Test
        fun `reads the sliding minute in one batch where the cache offers one`() = runTest {
            val cache = RecordingCache()
            val subscription = subscription(cache = cache)

            subscription.usage(BillingTier.FREE)

            assertEquals(1, cache.batchReads)
            assertEquals(60, cache.batchedKeyCount)
        }
    }

    @Nested
    inner class InMemoryCache {

        @Test
        fun `reads back nothing for a key never written`() = runTest {
            assertNull(InMemoryBillingSubscriptionCache(clock).get("absent"))
        }

        @Test
        fun `reads back what was written`() = runTest {
            val cache = InMemoryBillingSubscriptionCache(clock)

            cache.put(key = "counted", count = 7, ttl = 1.minutes)

            assertEquals(7L, cache.get("counted"))
        }

        @Test
        fun `replaces what was written`() = runTest {
            val cache = InMemoryBillingSubscriptionCache(clock)

            cache.put(key = "counted", count = 7, ttl = 1.minutes)
            cache.put(key = "counted", count = 8, ttl = 1.minutes)

            assertEquals(8L, cache.get("counted"))
        }

        @Test
        fun `forgets an entry once its ttl has passed`() = runTest {
            val cache = InMemoryBillingSubscriptionCache(clock)
            cache.put(key = "counted", count = 7, ttl = 1.minutes)

            clock.advance(1.minutes)

            assertNull(cache.get("counted"))
        }

        @Test
        fun `keeps an entry up to the moment its ttl passes`() = runTest {
            val cache = InMemoryBillingSubscriptionCache(clock)
            cache.put(key = "counted", count = 7, ttl = 1.minutes)

            clock.advance(59.seconds)

            assertEquals(7L, cache.get("counted"))
        }

        @Test
        fun `reads several keys at once`() = runTest {
            val cache = InMemoryBillingSubscriptionCache(clock)
            cache.put(key = "one", count = 1, ttl = 1.minutes)
            cache.put(key = "two", count = 2, ttl = 1.minutes)

            val counts = cache.getAll(listOf("one", "two", "three"))

            assertEquals(1L, counts["one"])
            assertEquals(2L, counts["two"])
            assertNull(counts["three"])
        }

        @Test
        fun `sweeps expired entries out rather than growing without bound`() = runTest {
            val cache = InMemoryBillingSubscriptionCache(clock)
            repeat(300) { cache.put(key = "second-$it", count = 1, ttl = 1.seconds) }

            clock.advance(2.seconds)
            cache.put(key = "fresh", count = 1, ttl = 1.minutes)

            assertEquals(1L, cache.get("fresh"))
            assertNull(cache.get("second-0"))
        }
    }

    /** A cache that remembers which keys were asked for, to show what [BillingSubscription] does with it. */
    private class RecordingCache : BillingSubscriptionCache {

        private val counts = mutableMapOf<String, Long>()

        val keys = mutableSetOf<String>()
        var batchReads = 0
        var batchedKeyCount = 0

        override suspend fun get(key: String): Long? = counts[key]

        override suspend fun put(key: String, count: Long, ttl: kotlin.time.Duration) {
            keys += key
            counts[key] = count
        }

        override suspend fun getAll(keys: Collection<String>): Map<String, Long?> {
            batchReads++
            batchedKeyCount = keys.size

            return keys.associateWith { counts[it] }
        }
    }
}
