package io.github.yesitskev.kopendota.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned by the `GET /leagues`, `GET /leagues/{league_id}` endpoints.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getLeagues
 * @see io.github.yesitskev.kopendota.OpenDota.getLeague
 */
@Serializable
data class LeagueObjectResponse(
    /** League ID. */
    @SerialName("leagueid") val leagueid: Long? = null,
    /** Ticket product required to watch the league, when one is required. */
    @SerialName("ticket") val ticket: String? = null,
    /** Path to the league's banner image. */
    @SerialName("banner") val banner: String? = null,
    /** League tier, for example `premium`, `professional` or `amateur`. */
    @SerialName("tier") val tier: String? = null,
    /** League name. */
    @SerialName("name") val name: String? = null,
)
