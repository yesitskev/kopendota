package io.github.yesitskev.kopendota.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Returned by the `GET /benchmarks` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getBenchmarks
 */
@Serializable
data class BenchmarksResponse(
    /** The ID value of the hero played. */
    @SerialName("hero_id") val heroId: Long? = null,
    /** Benchmark percentiles for each tracked stat. */
    @SerialName("result") val result: BenchmarksResponseResult? = null,
)

/**
 * Result.
 *
 * Nested payload of [BenchmarksResponse].
 */
@Serializable
data class BenchmarksResponseResult(
    /** Gold per minute percentiles for the hero. */
    @SerialName("gold_per_min") val goldPerMin: List<BenchmarksResponseResultGoldPerMin>? = null,
    /** Experience per minute percentiles for the hero. */
    @SerialName("xp_per_min") val xpPerMin: List<BenchmarksResponseResultXpPerMin>? = null,
    /** Kills per minute percentiles for the hero. */
    @SerialName("kills_per_min") val killsPerMin: List<BenchmarksResponseResultKillsPerMin>? = null,
    /** Deaths per minute percentiles for the hero. */
    @SerialName("deaths_per_min") val deathsPerMin: List<BenchmarksResponseResultDeathsPerMin>? = null,
    /** Assists per minute percentiles for the hero. */
    @SerialName("assists_per_min") val assistsPerMin: List<BenchmarksResponseResultAssistsPerMin>? = null,
    /** Last hits per minute percentiles for the hero. */
    @SerialName("last_hits_per_min") val lastHitsPerMin: List<BenchmarksResponseResultLastHitsPerMin>? = null,
    /** Denies per minute percentiles for the hero. */
    @SerialName("denies_per_min") val deniesPerMin: List<BenchmarksResponseResultDeniesPerMin>? = null,
    /** Hero damage per minute percentiles for the hero. */
    @SerialName("hero_damage_per_min") val heroDamagePerMin: List<BenchmarksResponseResultHeroDamagePerMin>? = null,
    /** Hero healing per minute percentiles for the hero. */
    @SerialName("hero_healing_per_min") val heroHealingPerMin: List<BenchmarksResponseResultHeroHealingPerMin>? = null,
    /** Tower damage percentiles for the hero. */
    @SerialName("tower_damage") val towerDamage: List<BenchmarksResponseResultTowerDamage>? = null,
)

/** Nested payload of [BenchmarksResponse]. */
@Serializable
data class BenchmarksResponseResultGoldPerMin(
    /** Percentile this benchmark bucket represents, from 0.0 to 1.0. */
    @SerialName("percentile") val percentile: Double? = null,
    /** Value of the stat at this percentile. */
    @SerialName("value") val value: Double? = null,
)

/** Nested payload of [BenchmarksResponse]. */
@Serializable
data class BenchmarksResponseResultXpPerMin(
    /** Percentile this benchmark bucket represents, from 0.0 to 1.0. */
    @SerialName("percentile") val percentile: Double? = null,
    /** Value of the stat at this percentile. */
    @SerialName("value") val value: Double? = null,
)

/** Nested payload of [BenchmarksResponse]. */
@Serializable
data class BenchmarksResponseResultKillsPerMin(
    /** Percentile this benchmark bucket represents, from 0.0 to 1.0. */
    @SerialName("percentile") val percentile: Double? = null,
    /** Value of the stat at this percentile. */
    @SerialName("value") val value: Double? = null,
)

/** Nested payload of [BenchmarksResponse]. */
@Serializable
data class BenchmarksResponseResultDeathsPerMin(
    /** Percentile this benchmark bucket represents, from 0.0 to 1.0. */
    @SerialName("percentile") val percentile: Double? = null,
    /** Value of the stat at this percentile. */
    @SerialName("value") val value: Double? = null,
)

/** Nested payload of [BenchmarksResponse]. */
@Serializable
data class BenchmarksResponseResultAssistsPerMin(
    /** Percentile this benchmark bucket represents, from 0.0 to 1.0. */
    @SerialName("percentile") val percentile: Double? = null,
    /** Value of the stat at this percentile. */
    @SerialName("value") val value: Double? = null,
)

/** Nested payload of [BenchmarksResponse]. */
@Serializable
data class BenchmarksResponseResultLastHitsPerMin(
    /** Percentile this benchmark bucket represents, from 0.0 to 1.0. */
    @SerialName("percentile") val percentile: Double? = null,
    /** Value of the stat at this percentile. */
    @SerialName("value") val value: Double? = null,
)

/** Nested payload of [BenchmarksResponse]. */
@Serializable
data class BenchmarksResponseResultDeniesPerMin(
    /** Percentile this benchmark bucket represents, from 0.0 to 1.0. */
    @SerialName("percentile") val percentile: Double? = null,
    /** Value of the stat at this percentile. */
    @SerialName("value") val value: Double? = null,
)

/** Nested payload of [BenchmarksResponse]. */
@Serializable
data class BenchmarksResponseResultHeroDamagePerMin(
    /** Percentile this benchmark bucket represents, from 0.0 to 1.0. */
    @SerialName("percentile") val percentile: Double? = null,
    /** Value of the stat at this percentile. */
    @SerialName("value") val value: Double? = null,
)

/** Nested payload of [BenchmarksResponse]. */
@Serializable
data class BenchmarksResponseResultHeroHealingPerMin(
    /** Percentile this benchmark bucket represents, from 0.0 to 1.0. */
    @SerialName("percentile") val percentile: Double? = null,
    /** Value of the stat at this percentile. */
    @SerialName("value") val value: Double? = null,
)

/** Nested payload of [BenchmarksResponse]. */
@Serializable
data class BenchmarksResponseResultTowerDamage(
    /** Percentile this benchmark bucket represents, from 0.0 to 1.0. */
    @SerialName("percentile") val percentile: Double? = null,
    /** Value of the stat at this percentile. */
    @SerialName("value") val value: Long? = null,
)

/**
 * Returned by the `GET /distributions` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getDistributions
 */
@Serializable
data class DistributionsResponse(
    /** Distribution of players across rank tiers. */
    @SerialName("ranks") val ranks: DistributionsResponseRanks? = null,
)

/**
 * Ranks.
 *
 * Nested payload of [DistributionsResponse].
 */
@Serializable
data class DistributionsResponseRanks(
    /** One row per rank tier bin. */
    @SerialName("rows") val rows: List<DistributionsResponseRanksRows>? = null,
    /** Totals across every bin. */
    @SerialName("sum") val sum: DistributionsResponseRanksSum? = null,
)

/**
 * Rows.
 *
 * Nested payload of [DistributionsResponse].
 */
@Serializable
data class DistributionsResponseRanksRows(
    /** Rank tier bin. */
    @SerialName("bin") val bin: Long? = null,
    /** Name of the rank tier bin. */
    @SerialName("bin_name") val binName: Long? = null,
    /** Number of players in the bin. */
    @SerialName("count") val count: Long? = null,
    /** Running total of players up to and including this bin. */
    @SerialName("cumulative_sum") val cumulativeSum: Long? = null,
)

/**
 * Sum.
 *
 * Nested payload of [DistributionsResponse].
 */
@Serializable
data class DistributionsResponseRanksSum(
    /** Total number of players across every bin. */
    @SerialName("count") val count: Long? = null,
)

/**
 * Returned by the `GET /metadata` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getMetadata
 */
@Serializable
data class MetadataResponse(
    /** Site wide banner message, or `null` when none is set. */
    @SerialName("banner") val banner: JsonElement? = null,
)

/**
 * Returned by the `GET /rankings` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getRankings
 */
@Serializable
data class RankingsResponse(
    /** The ID value of the hero played. */
    @SerialName("hero_id") val heroId: Long? = null,
    /** Top ranked players for the hero, best first. */
    @SerialName("rankings") val rankings: List<RankingsResponseRankings>? = null,
)

/**
 * Rankings.
 *
 * Nested payload of [RankingsResponse].
 */
@Serializable
data class RankingsResponseRankings(
    /** The player account ID. */
    @SerialName("account_id") val accountId: Long? = null,
    /** Ranking score the player has achieved with the hero. */
    @SerialName("score") val score: Double? = null,
    /** The player's 64 bit Steam ID. */
    @SerialName("steamid") val steamid: String? = null,
    /** URL of the player's small Steam avatar. */
    @SerialName("avatar") val avatar: String? = null,
    /** URL of the player's medium Steam avatar. */
    @SerialName("avatarmedium") val avatarmedium: String? = null,
    /** URL of the player's full size Steam avatar. */
    @SerialName("avatarfull") val avatarfull: String? = null,
    /** URL of the player's Steam community profile. */
    @SerialName("profileurl") val profileurl: String? = null,
    /** Player's Steam name. */
    @SerialName("personaname") val personaname: String? = null,
    /** Time the player last logged in to OpenDota. */
    @SerialName("last_login") val lastLogin: String? = null,
    /** Time the player's full match history was last fetched. */
    @SerialName("full_history_time") val fullHistoryTime: String? = null,
    /** Legacy compendium cheese count for the player. */
    @SerialName("cheese") val cheese: Long? = null,
    /** Whether the player's full match history could not be fetched from Steam. */
    @SerialName("fh_unavailable") val fhUnavailable: Boolean? = null,
    /** Two letter country code reported on the player's Steam profile. */
    @SerialName("loccountrycode") val loccountrycode: String? = null,
    /** The player's Dota medal and rank. */
    @SerialName("rank_tier") val rankTier: Long? = null,
)

/**
 * Returned by the `GET /records/{field}` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getRecords
 */
@Serializable
data class RecordsResponse(
    /** The ID number of the match assigned by Valve. */
    @SerialName("match_id") val matchId: Long? = null,
    /** The Unix timestamp at which the game started. */
    @SerialName("start_time") val startTime: Long? = null,
    /** The ID value of the hero played. */
    @SerialName("hero_id") val heroId: Long? = null,
    /** Record score. */
    @SerialName("score") val score: Long? = null,
)

/**
 * Returned by the `GET /schema` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getSchema
 */
@Serializable
data class SchemaResponse(
    /** Name of the database table. */
    @SerialName("table_name") val tableName: String? = null,
    /** Name of the column. */
    @SerialName("column_name") val columnName: String? = null,
    /** SQL data type of the column. */
    @SerialName("data_type") val dataType: String? = null,
)

/**
 * Returned by the `GET /search` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.search
 */
@Serializable
data class SearchResponse(
    /** The player account ID. */
    @SerialName("account_id") val accountId: Long? = null,
    /** URL of the player's full size Steam avatar. */
    @SerialName("avatarfull") val avatarfull: String? = null,
    /** Player's Steam name. */
    @SerialName("personaname") val personaname: String? = null,
    /** Last_match_time. May not be present or null. */
    @SerialName("last_match_time") val lastMatchTime: String? = null,
    /** Similarity of the player's name to the search query, from 0.0 to 1.0. */
    @SerialName("similarity") val similarity: Double? = null,
)
