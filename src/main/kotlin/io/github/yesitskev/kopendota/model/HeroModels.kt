package io.github.yesitskev.kopendota.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned by the `GET /heroes` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getHeroes
 */
@Serializable
data class HeroObjectResponse(
    /** The ID value of the hero played. */
    @SerialName("id") val id: Long? = null,
    /** Dota hero command name. */
    @SerialName("name") val name: String? = null,
    /** Hero name. */
    @SerialName("localized_name") val localizedName: String? = null,
    /** Hero primary shorthand attribute name, e.g. 'agi'. */
    @SerialName("primary_attr") val primaryAttr: String? = null,
    /** Hero attack type, either 'Melee' or 'Ranged'. */
    @SerialName("attack_type") val attackType: String? = null,
    /** Roles the hero fills, for example `Carry`, `Support` or `Nuker`. */
    @SerialName("roles") val roles: List<String>? = null,
)

/**
 * Returned by the `GET /heroStats` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getHeroStats
 */
@Serializable
data class HeroStatsResponse(
    /** The ID value of the hero played. */
    @SerialName("id") val id: Long? = null,
    /** Dota hero command name. */
    @SerialName("name") val name: String? = null,
    /** Hero name. */
    @SerialName("localized_name") val localizedName: String? = null,
    /** Primary attribute of the hero, for example `str`, `agi`, `int` or `all`. */
    @SerialName("primary_attr") val primaryAttr: String? = null,
    /** Attack type of the hero, either `Melee` or `Ranged`. */
    @SerialName("attack_type") val attackType: String? = null,
    /** Roles the hero fills, for example `Carry`, `Support` or `Nuker`. */
    @SerialName("roles") val roles: List<String>? = null,
    /** Path to the hero's portrait image on the Steam CDN. */
    @SerialName("img") val img: String? = null,
    /** Path to the hero's icon image on the Steam CDN. */
    @SerialName("icon") val icon: String? = null,
    /** Base health before attribute bonuses. */
    @SerialName("base_health") val baseHealth: Long? = null,
    /** Base health regeneration per second. */
    @SerialName("base_health_regen") val baseHealthRegen: Double? = null,
    /** Base mana before attribute bonuses. */
    @SerialName("base_mana") val baseMana: Long? = null,
    /** Base mana regeneration per second. */
    @SerialName("base_mana_regen") val baseManaRegen: Long? = null,
    /** Base armour before attribute bonuses. */
    @SerialName("base_armor") val baseArmor: Long? = null,
    /** Base magic resistance, as a percentage. */
    @SerialName("base_mr") val baseMr: Long? = null,
    /** Minimum base attack damage. */
    @SerialName("base_attack_min") val baseAttackMin: Long? = null,
    /** Maximum base attack damage. */
    @SerialName("base_attack_max") val baseAttackMax: Long? = null,
    /** Base strength. */
    @SerialName("base_str") val baseStr: Long? = null,
    /** Base agility. */
    @SerialName("base_agi") val baseAgi: Long? = null,
    /** Base intelligence. */
    @SerialName("base_int") val baseInt: Long? = null,
    /** Strength gained per level. */
    @SerialName("str_gain") val strGain: Double? = null,
    /** Agility gained per level. */
    @SerialName("agi_gain") val agiGain: Double? = null,
    /** Intelligence gained per level. */
    @SerialName("int_gain") val intGain: Double? = null,
    /** Attack range in game units. */
    @SerialName("attack_range") val attackRange: Long? = null,
    /** Attack projectile speed in game units per second. */
    @SerialName("projectile_speed") val projectileSpeed: Long? = null,
    /** Seconds between attacks at base attack speed. */
    @SerialName("attack_rate") val attackRate: Double? = null,
    /** Base attack time used to derive attack speed. */
    @SerialName("base_attack_time") val baseAttackTime: Long? = null,
    /** Attack animation point in seconds. */
    @SerialName("attack_point") val attackPoint: Double? = null,
    /** Base movement speed in game units per second. */
    @SerialName("move_speed") val moveSpeed: Long? = null,
    /** Turn rate of the hero, or `null` when the hero uses the default. */
    @SerialName("turn_rate") val turnRate: Double? = null,
    /** Whether the hero is available in Captains Mode. */
    @SerialName("cm_enabled") val cmEnabled: Boolean? = null,
    /** Number of legs on the hero's model. */
    @SerialName("legs") val legs: Long? = null,
    /** Daytime vision range in game units. */
    @SerialName("day_vision") val dayVision: Long? = null,
    /** Night time vision range in game units. */
    @SerialName("night_vision") val nightVision: Long? = null,
    /** The ID value of the hero played. */
    @SerialName("hero_id") val heroId: Long? = null,
    /** Picks in Turbo mode this month. */
    @SerialName("turbo_picks") val turboPicks: Long? = null,
    /** Wins in Turbo mode this month. */
    @SerialName("turbo_wins") val turboWins: Long? = null,
    /** Number of times the hero was banned in professional matches. */
    @SerialName("pro_ban") val proBan: Long? = null,
    /** Number of professional matches won with the hero. */
    @SerialName("pro_win") val proWin: Long? = null,
    /** Number of times the hero was picked in professional matches. */
    @SerialName("pro_pick") val proPick: Long? = null,
    /** Herald picks. */
    @SerialName("1_pick") val pick1: Long? = null,
    /** Herald wins. */
    @SerialName("1_win") val win1: Long? = null,
    /** Guardian picks. */
    @SerialName("2_pick") val pick2: Long? = null,
    /** Guardian wins. */
    @SerialName("2_win") val win2: Long? = null,
    /** Crusader picks. */
    @SerialName("3_pick") val pick3: Long? = null,
    /** Crusader wins. */
    @SerialName("3_win") val win3: Long? = null,
    /** Archon picks. */
    @SerialName("4_pick") val pick4: Long? = null,
    /** Archon wins. */
    @SerialName("4_win") val win4: Long? = null,
    /** Legend picks. */
    @SerialName("5_pick") val pick5: Long? = null,
    /** Legend wins. */
    @SerialName("5_win") val win5: Long? = null,
    /** Ancient picks. */
    @SerialName("6_pick") val pick6: Long? = null,
    /** Ancient wins. */
    @SerialName("6_win") val win6: Long? = null,
    /** Divine picks. */
    @SerialName("7_pick") val pick7: Long? = null,
    /** Divine wins. */
    @SerialName("7_win") val win7: Long? = null,
    /** Immortal picks. */
    @SerialName("8_pick") val pick8: Long? = null,
    /** Immortal wins. */
    @SerialName("8_win") val win8: Long? = null,
)

/**
 * Returned by the `GET /heroes/{hero_id}/matchups` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getHeroMatchups
 */
@Serializable
data class HeroMatchupsResponse(
    /** The ID value of the hero played. */
    @SerialName("hero_id") val heroId: Long? = null,
    /** Number of games played. */
    @SerialName("games_played") val gamesPlayed: Long? = null,
    /** Number of games won. */
    @SerialName("wins") val wins: Long? = null,
)

/**
 * Returned by the `GET /heroes/{hero_id}/durations` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getHeroDurations
 */
@Serializable
data class HeroDurationsResponse(
    /** Lower bound of number of seconds the match lasted. */
    @SerialName("duration_bin") val durationBin: String? = null,
    /** Number of games played. */
    @SerialName("games_played") val gamesPlayed: Long? = null,
    /** Number of wins. */
    @SerialName("wins") val wins: Long? = null,
)

/**
 * Returned by the `GET /heroes/{hero_id}/itemPopularity` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getHeroItemPopularity
 */
@Serializable
data class HeroItemPopularityResponse(
    /** Items bought before game started. */
    @SerialName("start_game_items") val startGameItems: HeroItemPopularityResponseStartGameItems? = null,
    /** Items bought in the first 10 min of the game, with cost at least 700. */
    @SerialName("early_game_items") val earlyGameItems: HeroItemPopularityResponseEarlyGameItems? = null,
    /** Items bought between 10 and 25 min of the game, with cost at least 2000. */
    @SerialName("mid_game_items") val midGameItems: HeroItemPopularityResponseMidGameItems? = null,
    /** Items bought at least 25 min after game started, with cost at least 4000. */
    @SerialName("late_game_items") val lateGameItems: HeroItemPopularityResponseLateGameItems? = null,
)

/**
 * Items bought before game started.
 *
 * Nested payload of [HeroItemPopularityResponse].
 */
@Serializable
data class HeroItemPopularityResponseStartGameItems(
    /** Number of item bought. */
    @SerialName("item") val item: Long? = null,
)

/**
 * Items bought in the first 10 min of the game, with cost at least 700.
 *
 * Nested payload of [HeroItemPopularityResponse].
 */
@Serializable
data class HeroItemPopularityResponseEarlyGameItems(
    /** Number of item bought. */
    @SerialName("item") val item: Long? = null,
)

/**
 * Items bought between 10 and 25 min of the game, with cost at least 2000.
 *
 * Nested payload of [HeroItemPopularityResponse].
 */
@Serializable
data class HeroItemPopularityResponseMidGameItems(
    /** Number of item bought. */
    @SerialName("item") val item: Long? = null,
)

/**
 * Items bought at least 25 min after game started, with cost at least 4000.
 *
 * Nested payload of [HeroItemPopularityResponse].
 */
@Serializable
data class HeroItemPopularityResponseLateGameItems(
    /** Number of item bought. */
    @SerialName("item") val item: Long? = null,
)
