package io.github.yesitskev.kopendota

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import retrofit2.HttpException
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class OpenDotaClientBillingTest {

    private lateinit var server: MockWebServer

    private val clock = MutableClock(MutableClock.START)

    private val clients = mutableListOf<OpenDotaClient>()

    @BeforeEach
    fun startServer() {
        server = MockWebServer()
        server.start()
    }

    @AfterEach
    fun stopServer() {
        clients.forEach(OpenDotaClient::close)
        clients.clear()
        server.close()
    }

    private fun client(
        apiKey: String? = "secret-key",
        freeTierLimits: BillingLimits = BillingLimits.FREE_TIER,
        queueLimit: Int = 0,
    ): OpenDotaClient =
        OpenDotaClient(
            billingSubscription =
                BillingSubscription(
                    apiKey = apiKey,
                    cache = InMemoryBillingSubscriptionCache(clock),
                    freeTierLimits = freeTierLimits,
                    subscriptionTierLimits = BillingLimits.SUBSCRIPTION_TIER,
                    clock = clock,
                ),
            queueLimit = queueLimit,
            baseUrl = server.url("/").toString(),
        )
            .also(clients::add)

    private fun enqueue(code: Int = 200, body: String = "[]", headersDelay: Duration = Duration.ZERO) {
        server.enqueue(
            MockResponse.Builder()
                .code(code)
                .body(body)
                .headersDelay(headersDelay.inWholeMilliseconds, TimeUnit.MILLISECONDS)
                .build(),
        )
    }

    private fun enqueueRateLimited(retryAfter: Duration? = null) {
        server.enqueue(
            MockResponse.Builder()
                .code(RateLimiter.HTTP_TOO_MANY_REQUESTS)
                .body("""{"error": "rate limit exceeded"}""")
                .apply { retryAfter?.let { addHeader("Retry-After", it.inWholeSeconds.toString()) } }
                .build(),
        )
    }

    private fun takeRequest(): RecordedRequest = server.takeRequest(5, TimeUnit.SECONDS)!!

    private fun apiKeyOf(request: RecordedRequest): String? = request.url.queryParameter("api_key")

    /** Waits in real time for the client's statistics to satisfy [condition], which runs on the limiter's clock. */
    private suspend fun awaitStatistics(client: OpenDotaClient, condition: (OpenDotaStatistics) -> Boolean) =
        withContext(Dispatchers.Default) {
            withTimeout(5.seconds) {
                while (!condition(client.statistics())) delay(10.milliseconds)
            }
        }

    @Nested
    inner class OpenDotaClientTierRouting {

        @Test
        fun `sends the first request on the free allowance`() = runTest {
            enqueue()

            client().openDota.getHeroes()

            assertNull(apiKeyOf(takeRequest()), "The free allowance should be spent anonymously")
        }

        @Test
        fun `switches to the subscription once the free minute is spent`() = runTest {
            enqueue()
            enqueue()
            val client = client(freeTierLimits = BillingLimits(perDay = 3_000, perMinute = 1))

            client.openDota.getHeroes()
            client.openDota.getHeroes()

            assertNull(apiKeyOf(takeRequest()))
            assertEquals("secret-key", apiKeyOf(takeRequest()))
        }

        @Test
        fun `switches to the subscription once the free day is spent`() = runTest {
            enqueue()
            enqueue()
            val client = client(freeTierLimits = BillingLimits(perDay = 1, perMinute = 60))

            client.openDota.getHeroes()
            client.openDota.getHeroes()

            assertNull(apiKeyOf(takeRequest()))
            assertEquals("secret-key", apiKeyOf(takeRequest()))
        }

        @Test
        fun `spends the whole free allowance before reaching for the subscription`() = runTest {
            repeat(4) { enqueue() }
            val client = client(freeTierLimits = BillingLimits(perDay = 3_000, perMinute = 3))

            repeat(4) { client.openDota.getHeroes() }

            val keys = List(4) { apiKeyOf(takeRequest()) }
            assertEquals(listOf(null, null, null, "secret-key"), keys)
        }
    }

    @Nested
    inner class OpenDotaClientRateLimitRetry {

        @Test
        fun `retries a free tier rate limit on the subscription`() = runTest {
            enqueueRateLimited()
            enqueue(body = """[{"id": 1}]""")

            val heroes = client().openDota.getHeroes()

            assertEquals(1, heroes.size)
            assertNull(apiKeyOf(takeRequest()), "The first attempt should have been anonymous")
            assertEquals("secret-key", apiKeyOf(takeRequest()), "The retry should have carried the key")
        }

        @Test
        fun `bills the retry and not the rate limit it followed`() = runTest {
            enqueueRateLimited()
            enqueue()
            val client = client()

            client.openDota.getHeroes()

            val statistics = client.statistics()
            assertEquals(0L, statistics.freeTierDay.used)
            assertEquals(1L, statistics.subscriptionTierDay.used)
        }

        @Test
        fun `surfaces a rate limit it has no subscription to retry on`() = runTest {
            enqueueRateLimited()
            val client = client(apiKey = null)

            val failure = runCatching { client.openDota.getHeroes() }.exceptionOrNull()

            assertTrue(failure is HttpException, "Expected an HttpException, got $failure")
            assertEquals(RateLimiter.HTTP_TOO_MANY_REQUESTS, (failure as HttpException).code())
        }

        @Test
        fun `surfaces a rate limit the subscription itself hit`() = runTest {
            enqueueRateLimited()
            enqueueRateLimited()
            val client = client()

            val failure = runCatching { client.openDota.getHeroes() }.exceptionOrNull()

            assertTrue(failure is HttpException, "Expected an HttpException, got $failure")
            assertEquals(2, server.requestCount)
        }

        @Test
        fun `stops spending a free allowance that has answered with a rate limit`() = runTest {
            enqueueRateLimited()
            repeat(2) { enqueue() }
            val client = client()

            client.openDota.getHeroes()
            client.openDota.getHeroes()

            val keys = List(3) { apiKeyOf(takeRequest()) }
            assertEquals(listOf(null, "secret-key", "secret-key"), keys)
        }
    }

    @Nested
    inner class OpenDotaClientBilling {

        @Test
        fun `bills a successful response against the day and the minute`() = runTest {
            enqueue()
            val client = client()

            client.openDota.getHeroes()

            val statistics = client.statistics()
            assertEquals(1L, statistics.freeTierDay.used)
            assertEquals(1L, statistics.freeTierMinute.used)
        }

        @ParameterizedTest
        @ValueSource(ints = [404, 429, 500])
        fun `bills nothing for a response the api does not charge for`(code: Int) = runTest {
            enqueue(code = code, body = """{"error": "nope"}""")
            val client = client(apiKey = null)

            runCatching { client.openDota.getHeroes() }

            val statistics = client.statistics()
            assertEquals(0L, statistics.freeTierDay.used)
            assertEquals(0L, statistics.freeTierMinute.used)
        }

        @Test
        fun `bills a client error the api does charge for`() = runTest {
            enqueue(code = 400, body = """{"error": "bad request"}""")
            val client = client(apiKey = null)

            runCatching { client.openDota.getHeroes() }

            assertEquals(1L, client.statistics().freeTierDay.used)
        }

        @Test
        fun `bills nothing for a request that never reached the api`() = runTest {
            server.close()
            val client = client(apiKey = null)

            runCatching { client.openDota.getHeroes() }

            assertEquals(0L, client.statistics().freeTierDay.used)
        }
    }

    @Nested
    inner class OpenDotaClientStatistics {

        @Test
        fun `reports an idle client as having nothing outstanding`() = runTest {
            val statistics = client().statistics()

            assertEquals(0, statistics.queuedRequests)
            assertEquals(0, statistics.inFlightRequests)
        }

        @Test
        fun `reports a request that is out at the api as in flight`() = runTest {
            enqueue(headersDelay = 2.seconds)
            val client = client()

            val call = launch(Dispatchers.Default) { runCatching { client.openDota.getHeroes() } }
            awaitStatistics(client) { it.inFlightRequests == 1 }

            assertEquals(0, client.statistics().queuedRequests)
            call.cancel()
        }

        @Test
        fun `reports a request waiting on a limit as queued`() = runTest {
            val client = client(apiKey = null, freeTierLimits = BillingLimits(perDay = 0, perMinute = 0))

            val call = launch(Dispatchers.Default) { runCatching { client.openDota.getHeroes() } }
            awaitStatistics(client) { it.queuedRequests == 1 }

            assertEquals(0, client.statistics().inFlightRequests)
            call.cancel()
        }

        @Test
        fun `reports nothing outstanding once a call has finished`() = runTest {
            enqueue()
            val client = client()

            client.openDota.getHeroes()

            val statistics = client.statistics()
            assertEquals(0, statistics.queuedRequests)
            assertEquals(0, statistics.inFlightRequests)
        }

        @Test
        fun `reports the usage of each tier against its ceiling`() = runTest {
            repeat(3) { enqueue() }
            val client = client(freeTierLimits = BillingLimits(perDay = 3_000, perMinute = 2))

            repeat(3) { client.openDota.getHeroes() }

            val statistics = client.statistics()
            assertEquals(OpenDotaUsage(used = 2, limit = 2), statistics.freeTierMinute)
            assertEquals(0L, statistics.freeTierMinute.remaining)
            assertEquals(1.0, statistics.freeTierMinute.fractionUsed)
            assertEquals(OpenDotaUsage(used = 2, limit = 3_000), statistics.freeTierDay)
            assertEquals(OpenDotaUsage(used = 1, limit = 3_000), statistics.subscriptionTierMinute)
            assertEquals(OpenDotaUsage(used = 1, limit = null), statistics.subscriptionTierDay)
            assertNull(statistics.subscriptionTierDay.remaining)
        }
    }

    @Nested
    inner class OpenDotaClientQueueLimit {

        @Test
        fun `refuses a call once the queue is full`() = runTest {
            val client =
                client(apiKey = null, freeTierLimits = BillingLimits(perDay = 0, perMinute = 0), queueLimit = 1)
            val queued = launch(Dispatchers.Default) { runCatching { client.openDota.getHeroes() } }
            awaitStatistics(client) { it.queuedRequests == 1 }

            val failure = runCatching { client.openDota.getHeroes() }.exceptionOrNull()

            assertTrue(failure is OpenDotaQueueLimitExceeded, "Expected a queue refusal, got $failure")
            assertEquals(1, (failure as OpenDotaQueueLimitExceeded).queueLimit)
            assertEquals(2, failure.queueDepth)
            queued.cancel()
        }

        @Test
        fun `sends nothing to the api for a refused call`() = runTest {
            val client =
                client(apiKey = null, freeTierLimits = BillingLimits(perDay = 0, perMinute = 0), queueLimit = 1)
            val queued = launch(Dispatchers.Default) { runCatching { client.openDota.getHeroes() } }
            awaitStatistics(client) { it.queuedRequests == 1 }

            runCatching { client.openDota.getHeroes() }

            assertEquals(0, server.requestCount)
            queued.cancel()
        }

        @Test
        fun `refuses nothing while the queue has room`() = runTest {
            enqueue()
            val client = client(queueLimit = 1)

            val heroes = client.openDota.getHeroes()

            assertEquals(emptyList<Nothing>(), heroes)
        }

        @Test
        fun `lets a queued call through once a limit permits it`() = runTest {
            enqueue()
            val client = client(apiKey = null, freeTierLimits = BillingLimits(perDay = 3_000, perMinute = 1))
            client.openDota.getHeroes()
            enqueue(body = """[{"id": 7}]""")

            val waiting =
                withContext(Dispatchers.Default) {
                    val call = async { client.openDota.getHeroes() }
                    awaitStatistics(client) { it.queuedRequests == 1 }
                    clock.advance(60.seconds)
                    call.await()
                }

            assertEquals(7L, waiting.single().id)
            assertEquals(2, server.requestCount)
        }
    }

    @Nested
    inner class OpenDotaClientLifecycle {

        @Test
        fun `fails a call made after the client is closed`() = runTest {
            enqueue()
            val client = client()
            client.close()

            val failure = runCatching { client.openDota.getHeroes() }.exceptionOrNull()

            assertTrue(failure != null, "A closed client should not complete a call")
        }

        @Test
        fun `can be closed more than once`() {
            val client = client()

            client.close()
            client.close()
        }
    }
}
