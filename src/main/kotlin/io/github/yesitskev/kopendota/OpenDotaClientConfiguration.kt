package io.github.yesitskev.kopendota

import okhttp3.logging.HttpLoggingInterceptor
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * How much detail [OpenDotaClient] writes to the HTTP log.
 *
 * The levels mirror the ones OkHttp's logging interceptor offers, so logging can be configured without OkHttp on the
 * compile classpath.
 */
enum class OpenDotaLogLevel {
    /** Log nothing. The default, and the only level that leaves the logging interceptor off the client entirely. */
    NONE,

    /** Log the method and URL of each request, and the status code, size and duration of each response. */
    BASIC,

    /** Log everything [BASIC] does, plus the request and response headers. */
    HEADERS,

    /**
     * Log everything [HEADERS] does, plus the request and response bodies.
     *
     * OpenDota responses run to hundreds of kilobytes — a parsed match or the constants endpoint more still — so this
     * level is best kept to debugging a single call.
     */
    BODY,
}

/**
 * HTTP logging configuration for [OpenDotaClient].
 *
 * Nothing is logged by default. The API key is never logged: [OpenDotaClient] redacts the `api_key` query parameter
 * before a line reaches [logger], whatever the level.
 *
 * @property level How much detail each request and response is logged with.
 * @property logger Where each log line is written. Defaults to OkHttp's own logger, which writes through
 *   `java.util.logging`.
 */
data class OpenDotaLogging(
    val level: OpenDotaLogLevel = OpenDotaLogLevel.NONE,
    val logger: (message: String) -> Unit = DEFAULT_LOGGER,
)

/**
 * Timeouts applied to the OkHttp client [OpenDotaClient] builds.
 *
 * Each timeout is rounded down to whole milliseconds, and [Duration.ZERO] disables one. The defaults are generous
 * enough for the slower OpenDota endpoints — the explorer and constants endpoints in particular — while still failing
 * a request that will never arrive.
 *
 * @property connect Time allowed to establish a TCP, and where applicable TLS, connection to the API.
 * @property read Time allowed between consecutive bytes arriving in a response.
 * @property write Time allowed between consecutive bytes being sent in a request body.
 * @property call Time allowed for a complete call, including connecting, redirects and retries. [Duration.ZERO] by
 *   default, which leaves a call bounded only by the other three timeouts.
 */
data class OpenDotaTimeouts(
    val connect: Duration = 10.seconds,
    val read: Duration = 30.seconds,
    val write: Duration = 10.seconds,
    val call: Duration = Duration.ZERO,
) {
    init {
        requireSupportedTimeout(connect, "connect")
        requireSupportedTimeout(read, "read")
        requireSupportedTimeout(write, "write")
        requireSupportedTimeout(call, "call")
    }
}

/** OkHttp's own logger, wrapped so that no OkHttp type appears in [OpenDotaLogging]'s signature. */
private val DEFAULT_LOGGER: (String) -> Unit = { message -> HttpLoggingInterceptor.Logger.DEFAULT.log(message) }

/**
 * Rejects a timeout OkHttp cannot hold: it stores every timeout as a count of whole milliseconds in an `Int`.
 *
 * @param timeout Configured timeout.
 * @param name Name of the timeout, used in the failure message.
 */
private fun requireSupportedTimeout(timeout: Duration, name: String) {
    require(timeout.isFinite() && !timeout.isNegative()) {
        "$name timeout must be a finite, non-negative duration, but was $timeout"
    }
    require(timeout.inWholeMilliseconds <= Int.MAX_VALUE) {
        "$name timeout must be at most ${Int.MAX_VALUE} ms, but was $timeout"
    }
}
