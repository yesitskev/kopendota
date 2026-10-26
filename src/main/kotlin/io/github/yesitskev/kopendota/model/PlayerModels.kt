package io.github.yesitskev.kopendota.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Returned by the `GET /players/{account_id}` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getPlayer
 */
@Serializable
data class PlayersResponse(
    /** The player's Dota medal/rank. */
    @SerialName("rank_tier") val rankTier: Double? = null,
    /** The player's rank on the Dota leaderboard (if Immortal). */
    @SerialName("leaderboard_rank") val leaderboardRank: Double? = null,
    /** Rating estimate based on ranked matches. */
    @SerialName("computed_mmr") val computedMmr: Double? = null,
    /** Rating estimate based on turbo matches. */
    @SerialName("computed_mmr_turbo") val computedMmrTurbo: Long? = null,
    /** List of names the player has used on Steam. */
    @SerialName("aliases") val aliases: List<PlayersResponseAliases>? = null,
    /** The player's Steam profile and OpenDota account details. */
    @SerialName("profile") val profile: PlayersResponseProfile? = null,
)

/**
 * List of names the player has used on Steam.
 *
 * Nested payload of [PlayersResponse].
 */
@Serializable
data class PlayersResponseAliases(
    /** The name used by the player. */
    @SerialName("personaname") val personaname: String? = null,
    /** The date the name was used since. */
    @SerialName("name_since") val nameSince: String? = null,
)

/**
 * Profile.
 *
 * Nested payload of [PlayersResponse].
 */
@Serializable
data class PlayersResponseProfile(
    /** The player account ID. */
    @SerialName("account_id") val accountId: Long? = null,
    /** Player's Steam name. */
    @SerialName("personaname") val personaname: String? = null,
    /** The player's professional name, when they are a pro player. */
    @SerialName("name") val name: String? = null,
    /** Boolean indicating status of current Dota Plus subscription. */
    @SerialName("plus") val plus: Boolean? = null,
    /** Legacy compendium cheese count for the player. */
    @SerialName("cheese") val cheese: Long? = null,
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
    /** Time the player last logged in to OpenDota. */
    @SerialName("last_login") val lastLogin: String? = null,
    /** Two letter country code reported on the player's Steam profile. */
    @SerialName("loccountrycode") val loccountrycode: String? = null,
    /** Boolean indicating if the user contributed to the development of OpenDota. */
    @SerialName("is_contributor") val isContributor: Boolean? = null,
    /** Boolean indicating if the user subscribed to OpenDota. */
    @SerialName("is_subscriber") val isSubscriber: Boolean? = null,
)

/**
 * Returned by the `GET /topPlayers`, `GET /proPlayers`, `GET /heroes/{hero_id}/players` endpoints.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getTopPlayers
 * @see io.github.yesitskev.kopendota.OpenDota.getProPlayers
 * @see io.github.yesitskev.kopendota.OpenDota.getHeroPlayers
 */
@Serializable
data class PlayerObjectResponse(
    /** The player account ID. */
    @SerialName("account_id") val accountId: Long? = null,
    /** Player's steam identifier. */
    @SerialName("steamid") val steamid: String? = null,
    /** Steam picture URL (small picture). */
    @SerialName("avatar") val avatar: String? = null,
    /** Steam picture URL (medium picture). */
    @SerialName("avatarmedium") val avatarmedium: String? = null,
    /** Steam picture URL (full picture). */
    @SerialName("avatarfull") val avatarfull: String? = null,
    /** Steam profile URL. */
    @SerialName("profileurl") val profileurl: String? = null,
    /** Player's Steam name. */
    @SerialName("personaname") val personaname: String? = null,
    /** Date and time of last login to OpenDota. */
    @SerialName("last_login") val lastLogin: String? = null,
    /** Date and time of last request to refresh player's match history. */
    @SerialName("full_history_time") val fullHistoryTime: String? = null,
    /** Amount of dollars the player has donated to OpenDota. */
    @SerialName("cheese") val cheese: Long? = null,
    /** Whether the refresh of player' match history failed. */
    @SerialName("fh_unavailable") val fhUnavailable: Boolean? = null,
    /** Player's country identifier, e.g. US. */
    @SerialName("loccountrycode") val loccountrycode: String? = null,
    /** Verified player name, e.g. 'Miracle-'. */
    @SerialName("name") val name: String? = null,
    /** Player's country code. */
    @SerialName("country_code") val countryCode: String? = null,
    /** Player's ingame role (core: 1 or support: 2). */
    @SerialName("fantasy_role") val fantasyRole: Long? = null,
    /** Player's team identifier. */
    @SerialName("team_id") val teamId: Long? = null,
    /** Name of the team the player belongs to. */
    @SerialName("team_name") val teamName: String? = null,
    /** Player's team shorthand tag, e.g. 'EG'. */
    @SerialName("team_tag") val teamTag: String? = null,
    /** Whether the roster lock is active. */
    @SerialName("is_locked") val isLocked: Boolean? = null,
    /** Whether the player is professional or not. */
    @SerialName("is_pro") val isPro: Boolean? = null,
    /** When the roster lock will end. */
    @SerialName("locked_until") val lockedUntil: Long? = null,
    /** Rating estimate based on ranked matches. */
    @SerialName("computed_mmr") val computedMmr: Long? = null,
)

/**
 * Object containing information on the match.
 *
 * Returned by the `GET /players/{account_id}/matches` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getPlayerMatches
 */
@Serializable
data class PlayerMatchesResponse(
    /** The ID number of the match assigned by Valve. */
    @SerialName("match_id") val matchId: Long? = null,
    /** Which slot the player is in. 0-127 are Radiant, 128-255 are Dire. */
    @SerialName("player_slot") val playerSlot: Long? = null,
    /** Boolean indicating whether Radiant won the match. */
    @SerialName("radiant_win") val radiantWin: Boolean? = null,
    /** Duration of the game in seconds. */
    @SerialName("duration") val duration: Long? = null,
    /**
     * Integer corresponding to game mode played. List of constants can be found here:
     * https://github.com/odota/dotaconstants/blob/master/json/game_mode.json.
     */
    @SerialName("game_mode") val gameMode: Long? = null,
    /**
     * Integer corresponding to lobby type of match. List of constants can be found here:
     * https://github.com/odota/dotaconstants/blob/master/json/lobby_type.json.
     */
    @SerialName("lobby_type") val lobbyType: Long? = null,
    /** The ID value of the hero played. */
    @SerialName("hero_id") val heroId: Long? = null,
    /** The Unix timestamp at which the game started. */
    @SerialName("start_time") val startTime: Long? = null,
    /** Parse version of the match, or `null` when the match has not been parsed. */
    @SerialName("version") val version: Long? = null,
    /** Total kills the player had at the end of the game. */
    @SerialName("kills") val kills: Long? = null,
    /** Total deaths the player had at the end of the game. */
    @SerialName("deaths") val deaths: Long? = null,
    /** Total assists the player had at the end of the game. */
    @SerialName("assists") val assists: Long? = null,
    /** Skill bracket assigned by Valve (Normal, High, Very High). */
    @SerialName("skill") val skill: Long? = null,
    /** Average rank of players with public match data. */
    @SerialName("average_rank") val averageRank: Long? = null,
    /** Integer describing whether or not the player left the game. 0: didn't leave. 1: left safely. 2+: Abandoned. */
    @SerialName("leaver_status") val leaverStatus: Long? = null,
    /** Size of the player's party. */
    @SerialName("party_size") val partySize: Long? = null,
    /** 1-indexed facet, see https://github.com/odota/dotaconstants/blob/master/build/hero_abilities.json. */
    @SerialName("hero_variant") val heroVariant: Long? = null,
)

/**
 * Match.
 *
 * Returned by the `GET /players/{account_id}/recentMatches` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getPlayerRecentMatches
 */
@Serializable
data class PlayerRecentMatchesResponse(
    /** The ID number of the match assigned by Valve. */
    @SerialName("match_id") val matchId: Long? = null,
    /** Which slot the player is in. 0-127 are Radiant, 128-255 are Dire. */
    @SerialName("player_slot") val playerSlot: Long? = null,
    /** Boolean indicating whether Radiant won the match. */
    @SerialName("radiant_win") val radiantWin: Boolean? = null,
    /** Duration of the game in seconds. */
    @SerialName("duration") val duration: Long? = null,
    /**
     * Integer corresponding to game mode played. List of constants can be found here:
     * https://github.com/odota/dotaconstants/blob/master/json/game_mode.json.
     */
    @SerialName("game_mode") val gameMode: Long? = null,
    /**
     * Integer corresponding to lobby type of match. List of constants can be found here:
     * https://github.com/odota/dotaconstants/blob/master/json/lobby_type.json.
     */
    @SerialName("lobby_type") val lobbyType: Long? = null,
    /** The ID value of the hero played. */
    @SerialName("hero_id") val heroId: Long? = null,
    /** The Unix timestamp at which the game started. */
    @SerialName("start_time") val startTime: Long? = null,
    /** Parse version of the match, or `null` when the match has not been parsed. */
    @SerialName("version") val version: Long? = null,
    /** Total kills the player had at the end of the match. */
    @SerialName("kills") val kills: Long? = null,
    /** Total deaths the player had at the end of the match. */
    @SerialName("deaths") val deaths: Long? = null,
    /** Total assists the player had at the end of the match. */
    @SerialName("assists") val assists: Long? = null,
    /** Skill bracket assigned by Valve (Normal, High, Very High). If the skill is unknown, will return null. */
    @SerialName("skill") val skill: Long? = null,
    /** Average rank of players with public match data. */
    @SerialName("average_rank") val averageRank: Long? = null,
    /** Experience Per Minute obtained by the player. */
    @SerialName("xp_per_min") val xpPerMin: Long? = null,
    /** Average gold per minute of the player. */
    @SerialName("gold_per_min") val goldPerMin: Long? = null,
    /** Total hero damage to enemy heroes. */
    @SerialName("hero_damage") val heroDamage: Long? = null,
    /** Total healing of ally heroes. */
    @SerialName("hero_healing") val heroHealing: Long? = null,
    /** Total last hits the player had at the end of the match. */
    @SerialName("last_hits") val lastHits: Long? = null,
    /** Integer corresponding to which lane the player laned in for the match. */
    @SerialName("lane") val lane: Long? = null,
    /** Lane role ID. */
    @SerialName("lane_role") val laneRole: Long? = null,
    /** Boolean describing whether or not the player roamed. */
    @SerialName("is_roaming") val isRoaming: Boolean? = null,
    /** Server cluster the match was played on. */
    @SerialName("cluster") val cluster: Long? = null,
    /** Integer describing whether or not the player left the game. 0: didn't leave. 1: left safely. 2+: Abandoned. */
    @SerialName("leaver_status") val leaverStatus: Long? = null,
    /** Size of the players party. If not in a party, will return 1. */
    @SerialName("party_size") val partySize: Long? = null,
    /** 1-indexed facet, see https://github.com/odota/dotaconstants/blob/master/build/hero_abilities.json. */
    @SerialName("hero_variant") val heroVariant: Long? = null,
)

/**
 * Hero.
 *
 * Returned by the `GET /players/{account_id}/heroes` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getPlayerHeroes
 */
@Serializable
data class PlayerHeroesResponse(
    /** The ID value of the hero played. */
    @SerialName("hero_id") val heroId: Long? = null,
    /** Unix timestamp of the last match played as the hero. */
    @SerialName("last_played") val lastPlayed: Long? = null,
    /** Number of matches played as the hero. */
    @SerialName("games") val games: Long? = null,
    /** Number of matches won as the hero. */
    @SerialName("win") val win: Long? = null,
    /** Number of matches played alongside the hero. */
    @SerialName("with_games") val withGames: Long? = null,
    /** Number of matches won alongside the hero. */
    @SerialName("with_win") val withWin: Long? = null,
    /** Number of matches played against the hero. */
    @SerialName("against_games") val againstGames: Long? = null,
    /** Number of matches won against the hero. */
    @SerialName("against_win") val againstWin: Long? = null,
)

/**
 * Returned by the `GET /players/{account_id}/peers` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getPlayerPeers
 */
@Serializable
data class PlayerPeersResponse(
    /** The player account ID. */
    @SerialName("account_id") val accountId: Long? = null,
    /** Unix timestamp of the most recent match counted here. */
    @SerialName("last_played") val lastPlayed: Long? = null,
    /** Number of those matches the queried player won. */
    @SerialName("win") val win: Long? = null,
    /** Number of matches played with the other player, on either team. */
    @SerialName("games") val games: Long? = null,
    /** Number of matches won on the same team as the other player. */
    @SerialName("with_win") val withWin: Long? = null,
    /** Number of matches played on the same team as the other player. */
    @SerialName("with_games") val withGames: Long? = null,
    /** Number of matches won against the other player. */
    @SerialName("against_win") val againstWin: Long? = null,
    /** Number of matches played against the other player. */
    @SerialName("against_games") val againstGames: Long? = null,
    /** Sum of the queried player's gold per minute across shared team matches. */
    @SerialName("with_gpm_sum") val withGpmSum: Long? = null,
    /** Sum of the queried player's experience per minute across shared team matches. */
    @SerialName("with_xpm_sum") val withXpmSum: Long? = null,
    /** Player's Steam name. */
    @SerialName("personaname") val personaname: String? = null,
    /** The peer's professional name, when they are a pro player. */
    @SerialName("name") val name: String? = null,
    /** Whether the player contributes to OpenDota. */
    @SerialName("is_contributor") val isContributor: Boolean? = null,
    /** Whether the player subscribes to OpenDota Plus. */
    @SerialName("is_subscriber") val isSubscriber: Boolean? = null,
    /** Time the player last logged in to OpenDota. */
    @SerialName("last_login") val lastLogin: String? = null,
    /** URL of the player's small Steam avatar. */
    @SerialName("avatar") val avatar: String? = null,
    /** URL of the player's full size Steam avatar. */
    @SerialName("avatarfull") val avatarfull: String? = null,
)

/**
 * Returned by the `GET /players/{account_id}/pros` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getPlayerPros
 */
@Serializable
data class PlayerProsResponse(
    /** The player account ID. */
    @SerialName("account_id") val accountId: Long? = null,
    /** The pro player's name. */
    @SerialName("name") val name: String? = null,
    /** Two letter country code of the pro player. */
    @SerialName("country_code") val countryCode: String? = null,
    /** Fantasy role ID of the pro player. */
    @SerialName("fantasy_role") val fantasyRole: Long? = null,
    /** ID of the team the player belongs to. */
    @SerialName("team_id") val teamId: Long? = null,
    /** Name of the team the player belongs to. */
    @SerialName("team_name") val teamName: String? = null,
    /** Short tag of the team the player belongs to. */
    @SerialName("team_tag") val teamTag: String? = null,
    /** Whether the player is locked to their current team roster. */
    @SerialName("is_locked") val isLocked: Boolean? = null,
    /** Whether the player is a registered pro player. */
    @SerialName("is_pro") val isPro: Boolean? = null,
    /** Unix timestamp until which the roster lock applies. */
    @SerialName("locked_until") val lockedUntil: Long? = null,
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
    /** Unix timestamp of the most recent match counted here. */
    @SerialName("last_played") val lastPlayed: Long? = null,
    /** Number of those matches the queried player won. */
    @SerialName("win") val win: Long? = null,
    /** Number of matches played with the other player, on either team. */
    @SerialName("games") val games: Long? = null,
    /** Number of matches won on the same team as the other player. */
    @SerialName("with_win") val withWin: Long? = null,
    /** Number of matches played on the same team as the other player. */
    @SerialName("with_games") val withGames: Long? = null,
    /** Number of matches won against the other player. */
    @SerialName("against_win") val againstWin: Long? = null,
    /** Number of matches played against the other player. */
    @SerialName("against_games") val againstGames: Long? = null,
    /** Sum of the queried player's gold per minute across shared team matches. */
    @SerialName("with_gpm_sum") val withGpmSum: Long? = null,
    /** Sum of the queried player's experience per minute across shared team matches. */
    @SerialName("with_xpm_sum") val withXpmSum: Long? = null,
)

/**
 * Returned by the `GET /players/{account_id}/totals` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getPlayerTotals
 */
@Serializable
data class PlayerTotalsResponse(
    /** Name of the field being totalled. */
    @SerialName("field") val field: String? = null,
    /** Number. */
    @SerialName("n") val n: Long? = null,
    /** Sum of the field across the matched games. */
    @SerialName("sum") val sum: Double? = null,
)

/**
 * Returned by the `GET /players/{account_id}/counts` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getPlayerCounts
 */
@Serializable
data class PlayerCountsResponse(
    /** Integer describing whether or not the player left the game. 0: didn't leave. 1: left safely. 2+: Abandoned. */
    @SerialName("leaver_status") val leaverStatus: JsonElement? = null,
    /**
     * Integer corresponding to game mode played. List of constants can be found here:
     * https://github.com/odota/dotaconstants/blob/master/json/game_mode.json.
     */
    @SerialName("game_mode") val gameMode: JsonElement? = null,
    /**
     * Integer corresponding to lobby type of match. List of constants can be found here:
     * https://github.com/odota/dotaconstants/blob/master/json/lobby_type.json.
     */
    @SerialName("lobby_type") val lobbyType: JsonElement? = null,
    /** Match counts grouped by lane role ID. */
    @SerialName("lane_role") val laneRole: JsonElement? = null,
    /** Integer corresponding to the region the game was played on. */
    @SerialName("region") val region: JsonElement? = null,
    /** Patch ID, from dotaconstants. */
    @SerialName("patch") val patch: JsonElement? = null,
)

/**
 * Returned by the `GET /players/{account_id}/wl` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getPlayerWinLoss
 */
@Serializable
data class PlayerWinLossResponse(
    /** Number of wins. */
    @SerialName("win") val win: Long? = null,
    /** Number of loses. */
    @SerialName("lose") val lose: Long? = null,
)

/**
 * Returned by the `GET /players/{account_id}/wardmap` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getPlayerWardMap
 */
@Serializable
data class PlayerWardMapResponse(
    /** Observer ward placements, keyed by map coordinate. */
    @SerialName("obs") val obs: JsonElement? = null,
    /** Sentry ward placements, keyed by map coordinate. */
    @SerialName("sen") val sen: JsonElement? = null,
)

/**
 * Returned by the `GET /players/{account_id}/wordcloud` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getPlayerWordCloud
 */
@Serializable
data class PlayerWordCloudResponse(
    /** Counts of words said by the player, keyed by word. */
    @SerialName("my_word_counts") val myWordCounts: JsonElement? = null,
    /** Counts of words said by everyone in the player's matches, keyed by word. */
    @SerialName("all_word_counts") val allWordCounts: JsonElement? = null,
)

/**
 * Returned by the `GET /players/{account_id}/ratings` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getPlayerRatings
 */
@Serializable
data class PlayerRatingsResponse(
    /** The player account ID. */
    @SerialName("account_id") val accountId: Long? = null,
    /** The ID number of the match assigned by Valve. */
    @SerialName("match_id") val matchId: Long? = null,
    /** Rank tier/medal of the player. */
    @SerialName("rank_tier") val rankTier: Long? = null,
    /** Unix timestamp of the rating change. */
    @SerialName("time") val time: Long? = null,
)

/**
 * Returned by the `GET /players/{account_id}/rankings` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getPlayerRankings
 */
@Serializable
data class PlayerRankingsResponse(
    /** The ID value of the hero played. */
    @SerialName("hero_id") val heroId: Long? = null,
    /** Hero score. */
    @SerialName("score") val score: Double? = null,
    /** The player's percentile ranking with the hero, from 0.0 to 1.0. */
    @SerialName("percent_rank") val percentRank: Double? = null,
    /** Numeric_rank. */
    @SerialName("card") val card: Long? = null,
)
