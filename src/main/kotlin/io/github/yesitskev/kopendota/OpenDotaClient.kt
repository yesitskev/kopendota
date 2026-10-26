package io.github.yesitskev.kopendota

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Assembles a ready-to-use [OpenDota] service, owning the Retrofit, OkHttp, JSON and rate limiting wiring that goes
 * with it.
 *
 * ```kotlin
 * val client = OpenDotaClient(
 *     billingSubscription = BillingSubscription(apiKey = System.getenv("OPEN_DOTA_API_KEY")),
 * )
 *
 * val match = client.openDota.getMatch(matchId = 7108753629L)
 * val wins = client.openDota.getPlayerWinLoss(accountId = 88367253L, heroId = 1L)
 *
 * println(client.statistics().freeTierDay.remaining)
 * ```
 *
 * A client owns an OkHttp connection pool, the threads that serve it, and the coroutine scope queued requests wait
 * on, so build one and share it rather than creating one per call. [close] releases all three.
 *
 * Every call is held to what [billingSubscription] allows. By default that is the free allowance alone: calls go out
 * anonymously, up to 3,000 a day and 60 in any sliding minute. Give the subscription an API key and calls spend the
 * free allowance first, then carry the key once the free limits are reached. The key is redacted from the HTTP log at
 * every [OpenDotaLogLevel].
 *
 * @param billingSubscription Allowances every call is counted against, and the key subscription-tier calls carry.
 *   The free allowance alone by default, with the counts kept in this process.
 * @param queueLimit How many calls may wait for a rate limit at once, beyond which a call raises
 *   [OpenDotaQueueLimitExceeded]. `0`, the default, is unlimited.
 * @param requestStrategy Which tier each call goes out on. Free allowance first by default.
 * @param json Serialiser the responses are deserialised with. Defaults to [DEFAULT_JSON].
 * @param logging How much of each request and response is logged, and where. Logs nothing by default.
 * @param timeouts Timeouts applied to the underlying OkHttp client.
 * @param baseUrl Base URL every endpoint path is resolved against. Must end with `/`, as Retrofit requires.
 */
class OpenDotaClient(
    billingSubscription: BillingSubscription = BillingSubscription(),
    queueLimit: Int = 0,
    requestStrategy: RequestStrategy = FreeTierFirstRequestStrategy,
    json: Json = DEFAULT_JSON,
    logging: OpenDotaLogging = OpenDotaLogging(),
    timeouts: OpenDotaTimeouts = OpenDotaTimeouts(),
    baseUrl: String = BASE_URL,
) : AutoCloseable {

    /** Holds every call to what the subscription allows, and picks the tier it goes out on. */
    val rateLimiter: RateLimiter =
        RateLimiter(subscription = billingSubscription, queueLimit = queueLimit, strategy = requestStrategy)

    /** OkHttp client carrying the configured timeouts and the log interceptor. */
    internal val httpClient: OkHttpClient = httpClient(logging = logging, timeouts = timeouts)

    /** The OpenDota API, as a service every call is made through. */
    val openDota: OpenDota =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .callFactory(BillingCallFactory(delegate = httpClient, rateLimiter = rateLimiter))
            .addConverterFactory(json.asConverterFactory(JSON_MEDIA_TYPE))
            .build()
            .create(OpenDota::class.java)

    /**
     * A snapshot of what the client is doing and what each tier has spent.
     *
     * @return Requests queued and in flight, and the day and sliding minute usage of both tiers.
     */
    suspend fun statistics(): OpenDotaStatistics = rateLimiter.statistics()

    /** Releases the connection pool, the threads serving it and the scope queued calls wait on. */
    override fun close() {
        rateLimiter.close()
        httpClient.dispatcher.executorService.shutdown()
        httpClient.connectionPool.evictAll()
    }

    companion object {

        /** Base URL of the OpenDota API, used when no other base URL is given. */
        const val BASE_URL: String = "https://api.opendota.com/api/"

        /**
         * Serialiser used when none is supplied.
         *
         * Unknown keys are ignored, because OpenDota adds fields over time while the models here track a single
         * snapshot of the schema. Pass `Json(from = DEFAULT_JSON) { … }` to keep that and configure the rest.
         */
        val DEFAULT_JSON: Json = Json { ignoreUnknownKeys = true }

        /** Query parameter the API key travels in. */
        internal const val API_KEY_QUERY_PARAMETER: String = "api_key"

        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}

/**
 * Builds the OkHttp client the service runs on.
 *
 * The API key is put on a request before it reaches OkHttp, by the tier the rate limiter admitted it to, so the log
 * interceptor sees it and redacts it.
 *
 * @param logging Logging configuration.
 * @param timeouts Timeout configuration.
 * @return Configured OkHttp client.
 */
private fun httpClient(logging: OpenDotaLogging, timeouts: OpenDotaTimeouts): OkHttpClient =
    OkHttpClient.Builder()
        .connectTimeout(timeouts.connect.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        .readTimeout(timeouts.read.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        .writeTimeout(timeouts.write.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        .callTimeout(timeouts.call.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        .apply { if (logging.level != OpenDotaLogLevel.NONE) addInterceptor(loggingInterceptor(logging)) }
        .build()

/**
 * Builds the log interceptor for a logging configuration, with the API key redacted from every line.
 *
 * @param logging Logging configuration.
 * @return Configured logging interceptor.
 */
private fun loggingInterceptor(logging: OpenDotaLogging): HttpLoggingInterceptor =
    HttpLoggingInterceptor { message -> logging.logger(message) }.apply {
        setLevel(logging.level.toHttpLoggingLevel())
        redactQueryParams(OpenDotaClient.API_KEY_QUERY_PARAMETER)
    }

/**
 * Maps a library log level onto the OkHttp level it mirrors.
 *
 * @return Matching OkHttp log level.
 */
private fun OpenDotaLogLevel.toHttpLoggingLevel(): HttpLoggingInterceptor.Level =
    when (this) {
        OpenDotaLogLevel.NONE -> HttpLoggingInterceptor.Level.NONE
        OpenDotaLogLevel.BASIC -> HttpLoggingInterceptor.Level.BASIC
        OpenDotaLogLevel.HEADERS -> HttpLoggingInterceptor.Level.HEADERS
        OpenDotaLogLevel.BODY -> HttpLoggingInterceptor.Level.BODY
    }
