package io.github.yesitskev.kopendota.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned by the `GET /publicMatches` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getPublicMatches
 */
@Serializable
data class PublicMatchesResponse(
    /** The ID number of the match assigned by Valve. */
    @SerialName("match_id") val matchId: Long? = null,
    /** Sequence number ordering the match within Valve's match history. */
    @SerialName("match_seq_num") val matchSeqNum: Long? = null,
    /** Boolean indicating whether Radiant won the match. */
    @SerialName("radiant_win") val radiantWin: Boolean? = null,
    /** The Unix timestamp at which the game started. */
    @SerialName("start_time") val startTime: Long? = null,
    /** Duration of the game in seconds. */
    @SerialName("duration") val duration: Long? = null,
    /** Lobby type ID. */
    @SerialName("lobby_type") val lobbyType: Long? = null,
    /** Game mode ID. */
    @SerialName("game_mode") val gameMode: Long? = null,
    /** Average rank tier of the players in the match. */
    @SerialName("avg_rank_tier") val avgRankTier: Long? = null,
    /** Number of players in the match with a known rank tier. */
    @SerialName("num_rank_tier") val numRankTier: Long? = null,
    /** Server cluster the match was played on. */
    @SerialName("cluster") val cluster: Long? = null,
    /** Hero IDs picked by the Radiant team. */
    @SerialName("radiant_team") val radiantTeam: List<Long>? = null,
    /** Hero IDs picked by the Dire team. */
    @SerialName("dire_team") val direTeam: List<Long>? = null,
)

/**
 * Returned by the `GET /parsedMatches` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getParsedMatches
 */
@Serializable
data class ParsedMatchesResponse(
    /** The ID number of the match assigned by Valve. */
    @SerialName("match_id") val matchId: Long? = null,
)
