package io.github.yesitskev.kopendota

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class RateLimiterTest {

    private val clock = MutableClock(MutableClock.START)

    private val limiters = mutableListOf<RateLimiter>()

    @AfterEach
    fun closeLimiters() {
        limiters.forEach(RateLimiter::close)
        limiters.clear()
    }

    private fun limiter(
        apiKey: String? = "secret-key",
        freeTierLimits: BillingLimits = BillingLimits.FREE_TIER,
        subscriptionTierLimits: BillingLimits = BillingLimits.SUBSCRIPTION_TIER,
        queueLimit: Int = 0,
        strategy: RequestStrategy = FreeTierFirstRequestStrategy,
    ): RateLimiter =
        RateLimiter(
            subscription =
                BillingSubscription(
                    apiKey = apiKey,
                    cache = InMemoryBillingSubscriptionCache(clock),
                    freeTierLimits = freeTierLimits,
                    subscriptionTierLimits = subscriptionTierLimits,
                    clock = clock,
                ),
            queueLimit = queueLimit,
            strategy = strategy,
        )
            .also(limiters::add)

    /** Admits a request and hands the slot straight back as a billed one, to move the counts along. */
    private suspend fun RateLimiter.spend(times: Int = 1) {
        repeat(times) { release(tier = acquire(), statusCode = 200) }
    }

    @Nested
    inner class RateLimiterTierSelection {

        @Test
        fun `spends the free allowance first`() = runTest {
            assertEquals(BillingTier.FREE, limiter().acquire())
        }

        @Test
        fun `switches to the subscription once the free minute is spent`() = runTest {
            val limiter = limiter(freeTierLimits = BillingLimits(perDay = 3_000, perMinute = 2))

            limiter.spend(times = 2)

            assertEquals(BillingTier.SUBSCRIPTION, limiter.acquire())
        }

        @Test
        fun `switches to the subscription once the free day is spent`() = runTest {
            val limiter = limiter(freeTierLimits = BillingLimits(perDay = 2, perMinute = 60))

            limiter.spend(times = 2)

            assertEquals(BillingTier.SUBSCRIPTION, limiter.acquire())
        }

        @Test
        fun `returns to the free allowance once the minute has slid past`() = runTest {
            val limiter = limiter(freeTierLimits = BillingLimits(perDay = 3_000, perMinute = 1))
            limiter.spend()
            assertEquals(BillingTier.SUBSCRIPTION, limiter.acquire().also { limiter.release(it, 200) })

            clock.advance(60.seconds)

            assertEquals(BillingTier.FREE, limiter.acquire())
        }

        @Test
        fun `counts a request in flight as spent, so a burst cannot overshoot the limit`() = runTest {
            val limiter = limiter(freeTierLimits = BillingLimits(perDay = 3_000, perMinute = 1))

            val first = limiter.acquire()
            val second = limiter.acquire()

            assertEquals(BillingTier.FREE, first)
            assertEquals(BillingTier.SUBSCRIPTION, second)
        }

        @Test
        fun `never picks the subscription tier when there is no key to send`() = runTest {
            val limiter = limiter(apiKey = null, freeTierLimits = BillingLimits(perDay = 1, perMinute = 60))
            limiter.spend()

            assertFalse(limiter.capacity(BillingTier.SUBSCRIPTION).isAvailable)
        }

        @Test
        fun `sends everything on the subscription where the free allowance is switched off`() = runTest {
            val limiter = limiter(freeTierLimits = BillingLimits(perDay = 0, perMinute = 0))

            assertEquals(BillingTier.SUBSCRIPTION, limiter.acquire())
        }

        @Test
        fun `uses the strategy it was given`() = runTest {
            val subscriptionFirst = RequestStrategy { _, subscriptionTier -> subscriptionTier.tier }

            assertEquals(BillingTier.SUBSCRIPTION, limiter(strategy = subscriptionFirst).acquire())
        }
    }

    @Nested
    inner class RateLimiterQueueing {

        @Test
        fun `holds a request until a limit lets it through`() = runTest {
            val limiter = limiter(apiKey = null, freeTierLimits = BillingLimits(perDay = 3_000, perMinute = 1))
            limiter.spend()
            var admitted: BillingTier? = null
            val waiting = launch { admitted = limiter.acquire() }

            runCurrent()
            assertNull(admitted, "The request should still be waiting on the spent minute")
            assertEquals(1, limiter.statistics().queuedRequests)

            clock.advance(60.seconds)
            advanceUntilIdle()
            waiting.join()

            assertEquals(BillingTier.FREE, admitted)
            assertEquals(0, limiter.statistics().queuedRequests)
        }

        @Test
        fun `reports what is waiting and what is in flight`() = runTest {
            val limiter = limiter(apiKey = null, freeTierLimits = BillingLimits(perDay = 3_000, perMinute = 1))
            limiter.acquire()
            val waiting = List(3) { launch { limiter.acquire() } }
            runCurrent()

            val statistics = limiter.statistics()

            assertEquals(3, statistics.queuedRequests)
            assertEquals(1, statistics.inFlightRequests)
            waiting.forEach(Job::cancel)
        }

        @Test
        fun `refuses a request once the queue is full`() = runTest {
            val limiter =
                limiter(apiKey = null, freeTierLimits = BillingLimits(perDay = 0, perMinute = 0), queueLimit = 2)
            val waiting = List(2) { launch { limiter.acquire() } }
            runCurrent()

            val refused = checkNotNull(limiter.acquireOrNull()) { "A third request should have been refused" }

            assertEquals(2, refused.queueLimit)
            assertEquals(3, refused.queueDepth)
            waiting.forEach(Job::cancel)
        }

        @Test
        fun `leaves the queue no deeper for a refused request`() = runTest {
            val limiter =
                limiter(apiKey = null, freeTierLimits = BillingLimits(perDay = 0, perMinute = 0), queueLimit = 1)
            val waiting = launch { limiter.acquire() }
            runCurrent()

            limiter.acquireOrNull()

            assertEquals(1, limiter.statistics().queuedRequests)
            waiting.cancel()
        }

        @ParameterizedTest
        @ValueSource(ints = [0, -1, -100])
        fun `queues without bound for a queue limit of zero or less`(queueLimit: Int) = runTest {
            val limiter =
                limiter(
                    apiKey = null,
                    freeTierLimits = BillingLimits(perDay = 0, perMinute = 0),
                    queueLimit = queueLimit,
                )
            val refusals = mutableListOf<OpenDotaQueueLimitExceeded>()

            val waiting =
                List(50) {
                    launch {
                        try {
                            limiter.acquire()
                        } catch (refused: OpenDotaQueueLimitExceeded) {
                            refusals += refused
                        }
                    }
                }
            runCurrent()

            assertEquals(emptyList<OpenDotaQueueLimitExceeded>(), refusals)
            assertEquals(50, limiter.statistics().queuedRequests)
            waiting.forEach(Job::cancel)
        }

        @Test
        fun `refuses only what overflows the queue`() = runTest {
            val limiter =
                limiter(apiKey = null, freeTierLimits = BillingLimits(perDay = 0, perMinute = 0), queueLimit = 3)
            val refusals = mutableListOf<OpenDotaQueueLimitExceeded>()

            val waiting =
                List(5) {
                    launch {
                        try {
                            limiter.acquire()
                        } catch (refused: OpenDotaQueueLimitExceeded) {
                            refusals += refused
                        }
                    }
                }
            runCurrent()

            assertEquals(2, refusals.size)
            assertEquals(3, limiter.statistics().queuedRequests)
            waiting.forEach(Job::cancel)
        }

        /** Takes a slot, returning the refusal instead of throwing it so a test can read it. */
        private suspend fun RateLimiter.acquireOrNull(): OpenDotaQueueLimitExceeded? =
            try {
                acquire()
                null
            } catch (refused: OpenDotaQueueLimitExceeded) {
                refused
            }
    }

    @Nested
    inner class RateLimiterBilling {

        @ParameterizedTest
        @ValueSource(ints = [200, 201, 304, 400, 401, 403, 502, 503])
        fun `bills a response the api charges for`(statusCode: Int) = runTest {
            val limiter = limiter()

            limiter.release(tier = limiter.acquire(), statusCode = statusCode)

            assertEquals(1L, limiter.capacity(BillingTier.FREE).day.used)
            assertEquals(1L, limiter.capacity(BillingTier.FREE).minute.used)
        }

        @ParameterizedTest
        @ValueSource(ints = [404, 429, 500])
        fun `bills nothing for a response the api does not charge for`(statusCode: Int) = runTest {
            val limiter = limiter()

            limiter.release(tier = limiter.acquire(), statusCode = statusCode)

            assertEquals(0L, limiter.capacity(BillingTier.FREE).day.used)
            assertEquals(0L, limiter.capacity(BillingTier.FREE).minute.used)
        }

        @Test
        fun `lists the status codes the api does not charge for`() {
            assertEquals(setOf(404, 429, 500), RateLimiter.UNBILLED_STATUS_CODES)
        }

        @Test
        fun `bills nothing for a request that never got a response`() = runTest {
            val limiter = limiter()

            limiter.release(tier = limiter.acquire(), statusCode = null)

            assertEquals(0L, limiter.capacity(BillingTier.FREE).day.used)
        }

        @Test
        fun `hands the slot back whether or not the response was billed`() = runTest {
            val limiter = limiter()

            limiter.release(tier = limiter.acquire(), statusCode = 429)

            assertEquals(0, limiter.statistics().inFlightRequests)
        }
    }

    @Nested
    inner class RateLimiterRateLimitResponses {

        @Test
        fun `retries a free tier rate limit on the subscription`() = runTest {
            assertTrue(limiter().shouldRetryOnSubscriptionTier(BillingTier.FREE, 429))
        }

        @Test
        fun `does not retry a free tier rate limit without a subscription to retry on`() = runTest {
            assertFalse(limiter(apiKey = null).shouldRetryOnSubscriptionTier(BillingTier.FREE, 429))
        }

        @Test
        fun `does not retry a rate limit the subscription itself hit`() = runTest {
            assertFalse(limiter().shouldRetryOnSubscriptionTier(BillingTier.SUBSCRIPTION, 429))
        }

        @ParameterizedTest
        @ValueSource(ints = [200, 404, 500])
        fun `does not retry a response that is not a rate limit`(statusCode: Int) = runTest {
            assertFalse(limiter().shouldRetryOnSubscriptionTier(BillingTier.FREE, statusCode))
        }

        @Test
        fun `backs a tier off after it answers with a rate limit`() = runTest {
            val limiter = limiter()

            limiter.release(tier = limiter.acquire(), statusCode = 429)

            assertFalse(limiter.capacity(BillingTier.FREE).isAvailable)
            assertEquals(BillingTier.SUBSCRIPTION, limiter.acquire())
        }

        @Test
        fun `backs off for as long as the response asked`() = runTest {
            val limiter = limiter()
            limiter.release(tier = limiter.acquire(), statusCode = 429, retryAfter = 30.seconds)

            clock.advance(29.seconds)
            assertFalse(limiter.capacity(BillingTier.FREE).isAvailable)

            clock.advance(1.seconds)
            assertTrue(limiter.capacity(BillingTier.FREE).isAvailable)
        }

        @Test
        fun `backs off for a minute where the response asked for nothing`() = runTest {
            val limiter = limiter()
            limiter.release(tier = limiter.acquire(), statusCode = 429)

            clock.advance(59.seconds)
            assertFalse(limiter.capacity(BillingTier.FREE).isAvailable)

            clock.advance(1.seconds)
            assertTrue(limiter.capacity(BillingTier.FREE).isAvailable)
        }
    }

    @Nested
    inner class RateLimiterStatistics {

        @Test
        fun `reports nothing spent on a fresh limiter`() = runTest {
            val statistics = limiter().statistics()

            assertEquals(0, statistics.queuedRequests)
            assertEquals(0, statistics.inFlightRequests)
            assertEquals(0L, statistics.freeTierDay.used)
            assertEquals(0L, statistics.freeTierMinute.used)
            assertEquals(0L, statistics.subscriptionTierDay.used)
            assertEquals(0L, statistics.subscriptionTierMinute.used)
        }

        @Test
        fun `reports the usage against the ceiling of each period`() = runTest {
            val limiter = limiter()

            limiter.spend(times = 30)

            val statistics = limiter.statistics()
            assertEquals(OpenDotaUsage(used = 30, limit = 3_000), statistics.freeTierDay)
            assertEquals(OpenDotaUsage(used = 30, limit = 60), statistics.freeTierMinute)
            assertEquals(2_970L, statistics.freeTierDay.remaining)
            assertEquals(30L, statistics.freeTierMinute.remaining)
            assertEquals(0.5, statistics.freeTierMinute.fractionUsed)
        }

        @Test
        fun `reports an unlimited period as having no ceiling to spend against`() = runTest {
            val limiter = limiter(freeTierLimits = BillingLimits(perDay = 0, perMinute = 0))

            limiter.spend(times = 5)

            val day = limiter.statistics().subscriptionTierDay
            assertEquals(5L, day.used)
            assertNull(day.limit)
            assertNull(day.remaining)
            assertNull(day.fractionUsed)
            assertFalse(day.isExhausted)
        }

        @Test
        fun `reports each tier separately`() = runTest {
            val limiter = limiter(freeTierLimits = BillingLimits(perDay = 3_000, perMinute = 2))

            limiter.spend(times = 3)

            val statistics = limiter.statistics()
            assertEquals(2L, statistics.freeTierMinute.used)
            assertEquals(1L, statistics.subscriptionTierMinute.used)
        }

        @Test
        fun `reports the sliding minute falling away while the day holds`() = runTest {
            val limiter = limiter()
            limiter.spend(times = 10)

            clock.advance(1.minutes)

            val statistics = limiter.statistics()
            assertEquals(10L, statistics.freeTierDay.used)
            assertEquals(0L, statistics.freeTierMinute.used)
        }
    }

    @Nested
    inner class UsageArithmetic {

        @Test
        fun `reports what is left of a ceiling`() {
            val usage = OpenDotaUsage(used = 2_940, limit = 3_000)

            assertEquals(60L, usage.remaining)
            assertEquals(0.98, usage.fractionUsed)
            assertFalse(usage.isExhausted)
        }

        @Test
        fun `reports a ceiling reached exactly as exhausted`() {
            val usage = OpenDotaUsage(used = 3_000, limit = 3_000)

            assertEquals(0L, usage.remaining)
            assertEquals(1.0, usage.fractionUsed)
            assertTrue(usage.isExhausted)
        }

        @Test
        fun `never reports less than nothing left`() {
            val usage = OpenDotaUsage(used = 3_010, limit = 3_000)

            assertEquals(0L, usage.remaining)
            assertTrue(usage.isExhausted)
        }

        @Test
        fun `shows an overshoot rather than capping it`() {
            assertTrue(OpenDotaUsage(used = 60, limit = 50).fractionUsed!! > 1.0)
        }

        @Test
        fun `reports a ceiling of nothing as exhausted from the start`() {
            val usage = OpenDotaUsage(used = 0, limit = 0)

            assertEquals(0L, usage.remaining)
            assertEquals(1.0, usage.fractionUsed)
            assertTrue(usage.isExhausted)
        }

        @Test
        fun `reports no ceiling for an unlimited period`() {
            val usage = OpenDotaUsage(used = 10_000, limit = null)

            assertNull(usage.remaining)
            assertNull(usage.fractionUsed)
            assertFalse(usage.isExhausted)
        }
    }
}
