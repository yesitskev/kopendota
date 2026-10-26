package io.github.yesitskev.kopendota.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned by the `GET /leagues/{league_id}/teams`, `GET /teams`, `GET /teams/{team_id}` endpoints.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getLeagueTeams
 * @see io.github.yesitskev.kopendota.OpenDota.getTeams
 * @see io.github.yesitskev.kopendota.OpenDota.getTeam
 */
@Serializable
data class TeamObjectResponse(
    /** Team's identifier. */
    @SerialName("team_id") val teamId: Long? = null,
    /** The Elo rating of the team. */
    @SerialName("rating") val rating: Double? = null,
    /** The number of games won by this team. */
    @SerialName("wins") val wins: Long? = null,
    /** The number of losses by this team. */
    @SerialName("losses") val losses: Long? = null,
    /** The Unix timestamp of the last match played by this team. */
    @SerialName("last_match_time") val lastMatchTime: Long? = null,
    /** Team name. */
    @SerialName("name") val name: String? = null,
    /** The team tag/abbreviation. */
    @SerialName("tag") val tag: String? = null,
)

/**
 * Returned by the `GET /teams/{team_id}/matches` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getTeamMatches
 */
@Serializable
data class TeamMatchObjectResponse(
    /** The ID number of the match assigned by Valve. */
    @SerialName("match_id") val matchId: Long? = null,
    /** Whether the team/player/hero was on Radiant. */
    @SerialName("radiant") val radiant: Boolean? = null,
    /** Boolean indicating whether Radiant won the match. */
    @SerialName("radiant_win") val radiantWin: Boolean? = null,
    /** Number of kills the Radiant team had when the match ended. */
    @SerialName("radiant_score") val radiantScore: Long? = null,
    /** Number of kills the Dire team had when the match ended. */
    @SerialName("dire_score") val direScore: Long? = null,
    /** Duration of the game in seconds. */
    @SerialName("duration") val duration: Long? = null,
    /** The Unix timestamp at which the game started. */
    @SerialName("start_time") val startTime: Long? = null,
    /** Identifier for the league the match took place in. */
    @SerialName("leagueid") val leagueid: Long? = null,
    /** Name of league the match took place in. */
    @SerialName("league_name") val leagueName: String? = null,
    /** Server cluster the match was played on. */
    @SerialName("cluster") val cluster: Long? = null,
    /** Opposing team identifier. */
    @SerialName("opposing_team_id") val opposingTeamId: Long? = null,
    /** Opposing team name, e.g. 'Evil Geniuses'. */
    @SerialName("opposing_team_name") val opposingTeamName: String? = null,
    /** Opposing team logo url. */
    @SerialName("opposing_team_logo") val opposingTeamLogo: String? = null,
)

/**
 * Returned by the `GET /teams/{team_id}/players` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getTeamPlayers
 */
@Serializable
data class TeamPlayersResponse(
    /** The player account ID. */
    @SerialName("account_id") val accountId: Long? = null,
    /** The player's professional name. */
    @SerialName("name") val name: String? = null,
    /** Number of games played. */
    @SerialName("games_played") val gamesPlayed: Long? = null,
    /** Number of wins. */
    @SerialName("wins") val wins: Long? = null,
    /** If this player is on the current roster. */
    @SerialName("is_current_team_member") val isCurrentTeamMember: Boolean? = null,
)

/**
 * Returned by the `GET /teams/{team_id}/heroes` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getTeamHeroes
 */
@Serializable
data class TeamHeroesResponse(
    /** The ID value of the hero played. */
    @SerialName("hero_id") val heroId: Long? = null,
    /** Hero name. */
    @SerialName("name") val name: String? = null,
    /** Number of games played. */
    @SerialName("games_played") val gamesPlayed: Long? = null,
    /** Number of wins. */
    @SerialName("wins") val wins: Long? = null,
)
