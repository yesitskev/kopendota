package io.github.yesitskev.kopendota

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.time.Duration

/**
 * A clock the tests move by hand, so that a sliding minute and a day boundary can be crossed without waiting.
 *
 * @param start Instant the clock reads until it is advanced.
 * @param zone Zone the clock reports.
 */
internal class MutableClock(start: Instant, private val zone: ZoneId = ZoneOffset.UTC) : Clock() {

    @Volatile private var now: Instant = start

    override fun getZone(): ZoneId = zone

    override fun withZone(zone: ZoneId): Clock = MutableClock(start = now, zone = zone)

    override fun instant(): Instant = now

    /**
     * Moves the clock on.
     *
     * @param duration How far to move it.
     */
    fun advance(duration: Duration) {
        now = now.plusMillis(duration.inWholeMilliseconds)
    }

    companion object {

        /** A fixed instant the tests start from, a little past midnight UTC so a day does not turn by accident. */
        val START: Instant = Instant.parse("2026-10-25T12:00:00Z")
    }
}
