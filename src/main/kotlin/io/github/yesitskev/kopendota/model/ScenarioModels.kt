package io.github.yesitskev.kopendota.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned by the `GET /scenarios/itemTimings` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getItemTimings
 */
@Serializable
data class ScenarioItemTimingsResponse(
    /** The ID value of the hero played. */
    @SerialName("hero_id") val heroId: Long? = null,
    /** Purchased item. */
    @SerialName("item") val item: String? = null,
    /** Ingame time in seconds before the item was purchased. */
    @SerialName("time") val time: Long? = null,
    /** The number of games where the hero bought this item before this time. */
    @SerialName("games") val games: String? = null,
    /** The number of games won where the hero bought this item before this time. */
    @SerialName("wins") val wins: String? = null,
)

/**
 * Returned by the `GET /scenarios/laneRoles` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getLaneRoles
 */
@Serializable
data class ScenarioLaneRolesResponse(
    /** The ID value of the hero played. */
    @SerialName("hero_id") val heroId: Long? = null,
    /** The hero's lane role. */
    @SerialName("lane_role") val laneRole: Long? = null,
    /** Maximum game length in seconds. */
    @SerialName("time") val time: Long? = null,
    /** The number of games where the hero played in this lane role. */
    @SerialName("games") val games: String? = null,
    /** The number of games won where the hero played in this lane role. */
    @SerialName("wins") val wins: String? = null,
)

/**
 * Returned by the `GET /scenarios/misc` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getMiscScenarios
 */
@Serializable
data class ScenarioMiscResponse(
    /** The scenario's name or description. */
    @SerialName("scenario") val scenario: String? = null,
    /** Boolean indicating whether Radiant executed this scenario. */
    @SerialName("is_radiant") val isRadiant: Boolean? = null,
    /** Region the game was played in. */
    @SerialName("region") val region: Long? = null,
    /** The number of games where this scenario occurred. */
    @SerialName("games") val games: String? = null,
    /** The number of games won where this scenario occured. */
    @SerialName("wins") val wins: String? = null,
)
