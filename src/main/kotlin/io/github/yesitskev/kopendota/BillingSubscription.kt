package io.github.yesitskev.kopendota

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

/** Which of a subscription's two allowances a request is counted against. */
enum class BillingTier {
    /** The allowance anonymous calls are counted against, which carry no `api_key` at all. */
    FREE,

    /** The allowance calls carrying the subscription's `api_key` are counted against. */
    SUBSCRIPTION,
}

/**
 * How many requests one [BillingTier] allows in a period.
 *
 * A `null` limit is unlimited, and a limit of `0` allows nothing — which is how a caller switches a tier off, for
 * instance to send every request on the subscription tier by giving the free tier no allowance at all.
 *
 * @property perDay Requests allowed per UTC day, or `null` for an unlimited day.
 * @property perMinute Requests allowed in any sliding minute, or `null` for an unlimited minute.
 */
data class BillingLimits(val perDay: Long?, val perMinute: Long?) {

    init {
        require(perDay == null || perDay >= 0) { "perDay must be null or non-negative, but was $perDay" }
        require(perMinute == null || perMinute >= 0) { "perMinute must be null or non-negative, but was $perMinute" }
    }

    companion object {

        /** What the free allowance permits: 3,000 requests a day and 60 in any sliding minute. */
        val FREE_TIER: BillingLimits = BillingLimits(perDay = 3_000, perMinute = 60)

        /** What a subscription permits: an unlimited day, and 3,000 requests in any sliding minute. */
        val SUBSCRIPTION_TIER: BillingLimits = BillingLimits(perDay = null, perMinute = 3_000)
    }
}

/**
 * Where a [BillingSubscription] keeps its request counts.
 *
 * One count is stored per key, and [BillingSubscription] owns the keys: a count per UTC day, and a count per second
 * that the sliding minute is summed from. Implement this over a shared store — Redis, Memcached, a database table —
 * to hold one allowance across several processes, or leave it at [InMemoryBillingSubscriptionCache] to count within a
 * single client.
 *
 * Implementations are called from several coroutines at once and must be safe to share. They may drop a key once its
 * `ttl` has passed, and a dropped or missing key reads as no requests at all.
 */
interface BillingSubscriptionCache {

    /**
     * Reads the count stored for [key].
     *
     * @param key Key to read.
     * @return Count stored for the key, or `null` where nothing is stored or the entry has expired.
     */
    suspend fun get(key: String): Long?

    /**
     * Stores [count] against [key], replacing whatever was there.
     *
     * @param key Key to write.
     * @param count Count to store.
     * @param ttl How long the entry stays useful. An implementation may drop it once the ttl has passed.
     */
    suspend fun put(key: String, count: Long, ttl: Duration)

    /**
     * Reads the counts stored for [keys] in one go.
     *
     * The sliding minute is summed from sixty per-second keys, so a cache reached over a network should override this
     * with a batch read — a Redis `MGET`, say — rather than leave it to call [get] sixty times.
     *
     * @param keys Keys to read.
     * @return Counts of the keys that hold one. A key with nothing stored may be left out or mapped to `null`.
     */
    suspend fun getAll(keys: Collection<String>): Map<String, Long?> = keys.associateWith { get(it) }
}

/**
 * A [BillingSubscriptionCache] holding its counts in this process, in a map that forgets an entry once its ttl passes.
 *
 * This is the default, and it counts one client's requests only. Two clients each get the whole allowance, so share a
 * networked cache instead where several processes draw on one subscription.
 *
 * @param clock Clock the entry expiry is measured against.
 */
class InMemoryBillingSubscriptionCache(private val clock: Clock = Clock.systemUTC()) : BillingSubscriptionCache {

    private data class Entry(val count: Long, val expiresAt: Instant)

    /** Counts held against their keys, each with the instant it stops being worth reading. */
    private val entries = ConcurrentHashMap<String, Entry>()

    override suspend fun get(key: String): Long? {
        val entry = entries[key] ?: return null
        if (entry.hasExpired()) {
            entries.remove(key, entry)
            return null
        }

        return entry.count
    }

    override suspend fun put(key: String, count: Long, ttl: Duration) {
        if (entries.size >= SWEEP_THRESHOLD) entries.values.removeIf { it.hasExpired() }
        entries[key] = Entry(count = count, expiresAt = clock.instant().plusMillis(ttl.inWholeMilliseconds))
    }

    /**
     * Whether an entry has outlived the ttl it was written with.
     *
     * @return `true` where the entry is no longer worth reading.
     */
    private fun Entry.hasExpired(): Boolean = !clock.instant().isBefore(expiresAt)

    private companion object {

        /** Entry count that a write sweeps the expired entries out at, so per-second keys cannot pile up. */
        private const val SWEEP_THRESHOLD = 256
    }
}

/**
 * The counts and limits of one OpenDota subscription, and the key its subscription-tier requests travel with.
 *
 * Every request a client makes is counted twice: once against the UTC day, and once against the second it was made
 * in, so that the sliding minute can be summed from the last sixty seconds. Both counts live in [cache], which is why
 * one subscription can be shared across processes.
 *
 * ```kotlin
 * // Free allowance first, then the subscription, counted within this process.
 * val subscription = BillingSubscription(apiKey = System.getenv("OPEN_DOTA_API_KEY"))
 *
 * // The same, with the counts shared by every process that reaches the same Redis.
 * val shared = BillingSubscription(apiKey = key, cache = RedisBillingSubscriptionCache(redis))
 *
 * // Never use the free allowance: send every request on the subscription.
 * val always = BillingSubscription(apiKey = key, freeTierLimits = BillingLimits(perDay = 0, perMinute = 0))
 * ```
 *
 * @property apiKey Subscription [API key](https://www.opendota.com/api-keys) that subscription-tier requests carry,
 *   or `null` for a client that only ever uses the free allowance. A blank key counts as no key at all.
 * @property cache Where the request counts are kept. Counts within this process by default.
 * @property freeTierLimits What the free allowance permits. [BillingLimits.FREE_TIER] by default.
 * @property subscriptionTierLimits What the subscription permits. [BillingLimits.SUBSCRIPTION_TIER] by default.
 * @property clock Clock the day and second the counts are keyed by are read from.
 */
class BillingSubscription(
    val apiKey: String? = null,
    val cache: BillingSubscriptionCache = InMemoryBillingSubscriptionCache(),
    val freeTierLimits: BillingLimits = BillingLimits.FREE_TIER,
    val subscriptionTierLimits: BillingLimits = BillingLimits.SUBSCRIPTION_TIER,
    internal val clock: Clock = Clock.systemUTC(),
) {

    /** The key subscription-tier requests carry, or `null` where there is no subscription to fall back on. */
    private val subscriptionKey: String? = apiKey?.takeIf(String::isNotBlank)

    /** Serialises the read-modify-write of a count, so two requests in one process cannot lose an increment. */
    private val mutex = Mutex()

    /** Whether there is a subscription tier to spend at all, which there is not without an API key. */
    val hasSubscriptionTier: Boolean
        get() = subscriptionKey != null

    /**
     * The limits of one tier.
     *
     * @param tier Tier to read the limits of.
     * @return Limits the tier allows.
     */
    fun limitsFor(tier: BillingTier): BillingLimits =
        when (tier) {
            BillingTier.FREE -> freeTierLimits
            BillingTier.SUBSCRIPTION -> subscriptionTierLimits
        }

    /**
     * Counts one request against a tier, in the UTC day and in the second it was made in.
     *
     * @param tier Tier the request was sent on.
     */
    internal suspend fun record(tier: BillingTier) {
        val now = clock.instant()

        mutex.withLock {
            increment(key = dayKey(tier, now), ttl = DAY_TTL)
            increment(key = secondKey(tier, now), ttl = SECOND_TTL)
        }
    }

    /**
     * What a tier has spent of the current day and of the sliding minute that ends now.
     *
     * @param tier Tier to read the usage of.
     * @return Requests counted against the tier's day and sliding minute.
     */
    internal suspend fun usage(tier: BillingTier): BillingUsage {
        val now = clock.instant()
        val secondKeys = (0 until SLIDING_MINUTE_SECONDS).map { secondKey(tier, now.minusSeconds(it.toLong())) }
        val seconds = cache.getAll(secondKeys)

        return BillingUsage(
            perDay = cache.get(dayKey(tier, now)) ?: 0L,
            perMinute = secondKeys.sumOf { seconds[it] ?: 0L },
        )
    }

    /**
     * Puts a request on a tier, adding the subscription key for the subscription tier and stripping any key for the
     * free tier, so that the tier a request is counted against is the tier OpenDota bills it on.
     *
     * @param tier Tier the request is going out on.
     * @param request Request as Retrofit built it.
     * @return The request to send.
     */
    internal fun requestOn(tier: BillingTier, request: okhttp3.Request): okhttp3.Request {
        val key = subscriptionKey?.takeIf { tier == BillingTier.SUBSCRIPTION }
        val url =
            request.url
                .newBuilder()
                .apply {
                    if (key == null) {
                        removeAllQueryParameters(OpenDotaClient.API_KEY_QUERY_PARAMETER)
                    } else {
                        setQueryParameter(OpenDotaClient.API_KEY_QUERY_PARAMETER, key)
                    }
                }
                .build()

        return request.newBuilder().url(url).build()
    }

    /**
     * Adds one to the count held against a key.
     *
     * The cache stores counts rather than offering an atomic increment, so this reads and writes it back. [mutex]
     * holds the two halves together within a process; across processes two clients writing the same key in the same
     * moment can still lose a count, which leaves an allowance read as very slightly under-spent.
     *
     * @param key Key to add to.
     * @param ttl How long the count stays worth reading.
     */
    private suspend fun increment(key: String, ttl: Duration) {
        cache.put(key = key, count = (cache.get(key) ?: 0L) + 1, ttl = ttl)
    }

    /**
     * The key one tier's count for a UTC day is held against.
     *
     * @param tier Tier the count belongs to.
     * @param now Instant whose UTC date names the day.
     * @return Cache key for the day, such as `kopendota:free:day:2026-10-26`.
     */
    private fun dayKey(tier: BillingTier, now: Instant): String =
        "$KEY_PREFIX:${tier.keyName}:day:${LocalDate.ofInstant(now, ZoneOffset.UTC)}"

    /**
     * The key one tier's count for a single second is held against, sixty of which make the sliding minute.
     *
     * @param tier Tier the count belongs to.
     * @param now Instant whose second names the key.
     * @return Cache key for the second, such as `kopendota:free:second:1792000000`.
     */
    private fun secondKey(tier: BillingTier, now: Instant): String =
        "$KEY_PREFIX:${tier.keyName}:second:${now.epochSecond}"

    /** How a tier is spelled inside a cache key. */
    private val BillingTier.keyName: String
        get() = name.lowercase()

    private companion object {

        /** Prefix every cache key carries, so a shared cache can hold other things beside these counts. */
        private const val KEY_PREFIX = "kopendota"

        /** How many per-second counts the sliding minute is summed from. */
        private const val SLIDING_MINUTE_SECONDS = 60

        /** How long a daily count stays useful, with a day's slack so that a clock skew cannot drop a live count. */
        private val DAY_TTL: Duration = 2.days

        /** How long a per-second count stays useful, which is the sliding minute plus slack. */
        private val SECOND_TTL: Duration = 90.seconds
    }
}

/**
 * What one [BillingTier] has spent of the current period.
 *
 * @property perDay Requests counted against the current UTC day.
 * @property perMinute Requests counted against the sliding minute that ends now.
 */
internal data class BillingUsage(val perDay: Long, val perMinute: Long)
