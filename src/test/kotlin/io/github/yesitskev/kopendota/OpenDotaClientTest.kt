package io.github.yesitskev.kopendota

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.Request
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import java.io.InterruptedIOException
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class OpenDotaClientTest {

    private lateinit var server: MockWebServer

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
        billingSubscription: BillingSubscription = BillingSubscription(),
        json: Json = OpenDotaClient.DEFAULT_JSON,
        logging: OpenDotaLogging = OpenDotaLogging(),
        timeouts: OpenDotaTimeouts = OpenDotaTimeouts(),
        path: String = "/",
    ): OpenDotaClient =
        OpenDotaClient(
            billingSubscription = billingSubscription,
            json = json,
            logging = logging,
            timeouts = timeouts,
            baseUrl = server.url(path).toString(),
        ).also(clients::add)

    /** A subscription that never spends the free allowance, so every call carries the key. */
    private fun subscriptionOnly(apiKey: String?): BillingSubscription =
        BillingSubscription(apiKey = apiKey, freeTierLimits = BillingLimits(perDay = 0, perMinute = 0))

    private fun enqueue(body: String = "[]", code: Int = 200, headersDelay: Duration = Duration.ZERO) {
        server.enqueue(
            MockResponse.Builder()
                .code(code)
                .body(body)
                .headersDelay(headersDelay.inWholeMilliseconds, TimeUnit.MILLISECONDS)
                .build(),
        )
    }

    private fun takeRequest(): RecordedRequest = server.takeRequest(5, TimeUnit.SECONDS)!!

    @Nested
    inner class OpenDotaClientApiKey {

        @Test
        fun `sends the api key as a query parameter on a subscription tier request`() = runTest {
            enqueue()

            client(billingSubscription = subscriptionOnly("secret-key")).openDota.getHeroes()

            assertEquals("secret-key", takeRequest().url.queryParameter("api_key"))
        }

        @Test
        fun `sends no api key on a free tier request`() = runTest {
            enqueue()

            client(billingSubscription = BillingSubscription(apiKey = "secret-key")).openDota.getHeroes()

            assertNull(takeRequest().url.queryParameter("api_key"))
        }

        @Test
        fun `sends no api key when none is supplied`() = runTest {
            enqueue()

            client().openDota.getHeroes()

            assertNull(takeRequest().url.queryParameter("api_key"))
        }

        @ParameterizedTest
        @ValueSource(strings = ["", " ", "\t", "   "])
        fun `treats a blank api key as no subscription at all`(apiKey: String) = runTest {
            enqueue()
            val subscription = BillingSubscription(apiKey = apiKey)

            client(billingSubscription = subscription).openDota.getHeroes()

            assertFalse(subscription.hasSubscriptionTier, "A blank key should leave no subscription tier")
            assertNull(takeRequest().url.queryParameter("api_key"))
        }

        @Test
        fun `sends the api key on every request made through the client`() = runTest {
            enqueue()
            enqueue(body = "{}")
            val client = client(billingSubscription = subscriptionOnly("secret-key"))

            client.openDota.getHeroes()
            client.openDota.getPlayer(accountId = 88367253L)

            assertEquals("secret-key", takeRequest().url.queryParameter("api_key"))
            assertEquals("secret-key", takeRequest().url.queryParameter("api_key"))
        }

        @Test
        fun `replaces an api key already present on the request`() {
            val subscription = BillingSubscription(apiKey = "new-key")
            val request = Request.Builder().url(server.url("/heroes?api_key=old-key")).build()

            val sent = subscription.requestOn(BillingTier.SUBSCRIPTION, request)

            assertEquals(listOf("new-key"), sent.url.queryParameterValues("api_key"))
        }

        @Test
        fun `strips an api key already present on a free tier request`() {
            val subscription = BillingSubscription(apiKey = "new-key")
            val request = Request.Builder().url(server.url("/heroes?api_key=old-key")).build()

            val sent = subscription.requestOn(BillingTier.FREE, request)

            assertEquals(emptyList<String>(), sent.url.queryParameterValues("api_key"))
        }

        @Test
        fun `leaves the other query parameters of a request untouched`() = runTest {
            enqueue(body = """{"win": 1, "lose": 2}""")
            val client = client(billingSubscription = subscriptionOnly("secret-key"))

            client.openDota.getPlayerWinLoss(accountId = 88367253L, heroId = 1L)

            val url = takeRequest().url
            assertEquals("secret-key", url.queryParameter("api_key"))
            assertEquals("1", url.queryParameter("hero_id"))
        }
    }

    @Nested
    inner class OpenDotaClientSerialisation {

        @Test
        fun `deserialises a response into the model of the endpoint`() = runTest {
            enqueue(body = """[{"id": 1, "localized_name": "Anti-Mage", "roles": ["Carry"]}]""")

            val heroes = client().openDota.getHeroes()

            assertEquals(1, heroes.size)
            assertEquals(1L, heroes.single().id)
            assertEquals("Anti-Mage", heroes.single().localizedName)
            assertEquals(listOf("Carry"), heroes.single().roles)
        }

        @Test
        fun `ignores keys the models do not know with the default serialiser`() = runTest {
            enqueue(body = """[{"id": 1, "an_unknown_field": "surprise"}]""")

            val heroes = client().openDota.getHeroes()

            assertEquals(1L, heroes.single().id)
        }

        @Test
        fun `fails on keys the models do not know when the supplied serialiser rejects them`() = runTest {
            enqueue(body = """[{"id": 1, "an_unknown_field": "surprise"}]""")
            val client = client(json = Json { ignoreUnknownKeys = false })

            assertThrows<SerializationException> { client.openDota.getHeroes() }
        }

        @Test
        fun `uses the supplied serialiser over the default`() = runTest {
            enqueue(body = """[{"id": 1, "localized_name": null}]""")
            val client = client(json = Json(from = OpenDotaClient.DEFAULT_JSON) { coerceInputValues = true })

            val heroes = client.openDota.getHeroes()

            assertNull(heroes.single().localizedName)
        }
    }

    @Nested
    inner class OpenDotaClientLogging {

        @Test
        fun `logs nothing by default`() = runTest {
            val messages = mutableListOf<String>()
            enqueue()

            client(logging = OpenDotaLogging(logger = { messages += it })).openDota.getHeroes()

            assertTrue(messages.isEmpty(), "Expected no log lines, but got $messages")
        }

        @Test
        fun `logs the request and the response once logging is switched on`() = runTest {
            val messages = mutableListOf<String>()
            val logging = OpenDotaLogging(level = OpenDotaLogLevel.BASIC, logger = { messages += it })
            enqueue()

            client(logging = logging).openDota.getHeroes()

            assertTrue(messages.any { it.startsWith("-->") && it.contains("heroes") }, "No request line in $messages")
            assertTrue(messages.any { it.startsWith("<--") && it.contains("200") }, "No response line in $messages")
        }

        @Test
        fun `logs the response body at the body level`() = runTest {
            val messages = mutableListOf<String>()
            val logging = OpenDotaLogging(level = OpenDotaLogLevel.BODY, logger = { messages += it })
            enqueue(body = """[{"id": 1}]""")

            client(logging = logging).openDota.getHeroes()

            assertTrue(messages.any { it.contains("""[{"id": 1}]""") }, "No response body in $messages")
        }

        @Test
        fun `redacts the api key from the logged url`() = runTest {
            val messages = mutableListOf<String>()
            val logging = OpenDotaLogging(level = OpenDotaLogLevel.BASIC, logger = { messages += it })
            enqueue()

            client(billingSubscription = subscriptionOnly("secret-key"), logging = logging).openDota.getHeroes()

            assertTrue(messages.any { it.contains("api_key=") }, "No redacted api key in $messages")
            assertTrue(messages.none { it.contains("secret-key") }, "API key leaked into $messages")
        }

        @ParameterizedTest
        @EnumSource(OpenDotaLogLevel::class)
        fun `never logs the api key`(level: OpenDotaLogLevel) = runTest {
            val messages = mutableListOf<String>()
            val logging = OpenDotaLogging(level = level, logger = { messages += it })
            enqueue()

            client(billingSubscription = subscriptionOnly("secret-key"), logging = logging).openDota.getHeroes()

            assertTrue(messages.none { it.contains("secret-key") }, "API key leaked into $messages")
        }
    }

    @Nested
    inner class OpenDotaClientTimeouts {

        @Test
        fun `fails a call that outlives the read timeout`() = runTest {
            enqueue(headersDelay = 2.seconds)
            val client = client(timeouts = OpenDotaTimeouts(read = 100.milliseconds))

            assertThrows<InterruptedIOException> { client.openDota.getHeroes() }
        }

        @Test
        fun `completes a call that finishes inside the read timeout`() = runTest {
            enqueue(headersDelay = 50.milliseconds)
            val client = client(timeouts = OpenDotaTimeouts(read = 5.seconds))

            assertEquals(emptyList<Nothing>(), client.openDota.getHeroes())
        }

        @Test
        fun `applies each configured timeout to the http client`() {
            val timeouts = OpenDotaTimeouts(connect = 1.seconds, read = 2.seconds, write = 3.seconds, call = 4.seconds)

            val httpClient = client(timeouts = timeouts).httpClient

            assertEquals(1_000, httpClient.connectTimeoutMillis)
            assertEquals(2_000, httpClient.readTimeoutMillis)
            assertEquals(3_000, httpClient.writeTimeoutMillis)
            assertEquals(4_000, httpClient.callTimeoutMillis)
        }

        @Test
        fun `leaves a call unbounded by default`() {
            assertEquals(0, client().httpClient.callTimeoutMillis)
        }

        @TestFactory
        fun `rejects a timeout okhttp cannot hold`(): List<DynamicTest> =
            listOf<Pair<String, () -> OpenDotaTimeouts>>(
                "negative connect" to { OpenDotaTimeouts(connect = (-1).seconds) },
                "negative read" to { OpenDotaTimeouts(read = (-1).milliseconds) },
                "infinite write" to { OpenDotaTimeouts(write = Duration.INFINITE) },
                "call beyond int milliseconds" to
                    { OpenDotaTimeouts(call = Int.MAX_VALUE.milliseconds + 1.milliseconds) },
            ).map { (name, create) ->
                DynamicTest.dynamicTest(name) { assertThrows<IllegalArgumentException> { create() } }
            }
    }

    @Nested
    inner class OpenDotaClientBaseUrl {

        @Test
        fun `resolves an endpoint path against the base url`() = runTest {
            enqueue()

            client(path = "/api/").openDota.getHeroes()

            assertEquals("/api/heroes", takeRequest().url.encodedPath)
        }

        @Test
        fun `rejects a base url retrofit cannot resolve paths against`() {
            assertThrows<IllegalArgumentException> {
                OpenDotaClient(baseUrl = server.url("/api").toString())
            }
        }

        @Test
        fun `points at the opendota api by default`() {
            assertEquals("https://api.opendota.com/api/", OpenDotaClient.BASE_URL)
        }
    }
}
