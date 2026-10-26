package io.github.yesitskev.kopendota

import io.github.yesitskev.kopendota.model.*
import kotlinx.serialization.json.JsonElement
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service definition for the [OpenDota API](https://docs.opendota.com/).
 *
 * Each function maps to a single OpenDota endpoint and suspends until the response has been deserialised into the
 * matching model from [io.github.yesitskev.kopendota.model]. Endpoint paths are relative, so the base URL
 * `https://api.opendota.com/api/` is supplied when the service is created — which [OpenDotaClient] does for you,
 * along with the rate limiting, the API key, the serialiser, HTTP logging and the timeouts. Wiring it up by hand
 * takes:
 *
 * ```kotlin
 * val json = Json { ignoreUnknownKeys = true }
 * val openDota: OpenDota =
 *     Retrofit.Builder()
 *         .baseUrl("https://api.opendota.com/api/")
 *         .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
 *         .build()
 *         .create(OpenDota::class.java)
 *
 * val match = openDota.getMatch(matchId = 7108753629L)
 * ```
 *
 * A service wired up that way carries no rate limiting: nothing holds a call back, counts it or chooses the tier it
 * goes out on, and an API key is only sent if you put one on the request yourself. Build the service through
 * [OpenDotaClient] for any of that.
 *
 * Every model property is nullable because the API omits fields it has no data for, and nullable query parameters are
 * left out of the request when they keep their `null` default.
 *
 * The API is usable without credentials, at the 3,000 calls a day and 60 a minute that
 * [BillingLimits.FREE_TIER] describes. Registering an [API key](https://www.opendota.com/api-keys) raises that:
 * give it to a [BillingSubscription] and [OpenDotaClient] spends the free allowance first, then sends the key as the
 * `api_key` query parameter once the free limits are reached. Hero, item, ability and patch IDs in responses are
 * resolved through the [dotaconstants](https://github.com/odota/dotaconstants) repository, which [getConstants]
 * mirrors.
 */
interface OpenDota {

    /**
     * Match data.
     *
     * `GET /matches/{match_id}`
     *
     * @param matchId Match ID.
     * @return Match data.
     */
    @GET("matches/{match_id}")
    suspend fun getMatch(@Path("match_id") matchId: Long): MatchResponse

    /**
     * Player data.
     *
     * `GET /players/{account_id}`
     *
     * @param accountId Steam32 account ID.
     * @return Player data.
     */
    @GET("players/{account_id}")
    suspend fun getPlayer(@Path("account_id") accountId: Long): PlayersResponse

    /**
     * Win/Loss count.
     *
     * `GET /players/{account_id}/wl`
     *
     * @param accountId Steam32 account ID.
     * @param limit Number of matches to limit to.
     * @param offset Number of matches to offset start by.
     * @param win Whether the player won.
     * @param patch Patch ID, from dotaconstants.
     * @param gameMode Game Mode ID.
     * @param lobbyType Lobby type ID.
     * @param region Region ID.
     * @param date Days previous.
     * @param laneRole Lane Role ID.
     * @param heroId Hero ID.
     * @param isRadiant Whether the player was radiant.
     * @param includedAccountId Account IDs in the match (array).
     * @param excludedAccountId Account IDs not in the match (array).
     * @param withHeroId Hero IDs on the player's team (array).
     * @param againstHeroId Hero IDs against the player's team (array).
     * @param significant Whether the match was significant for aggregation purposes. Defaults to 1 (true), set this to
     *   0 to return data for non-standard modes/matches.
     * @param having The minimum number of games played, for filtering hero stats.
     * @param sort The field to return matches sorted by in descending order.
     * @return Win/Loss count.
     */
    @GET("players/{account_id}/wl")
    suspend fun getPlayerWinLoss(
        @Path("account_id") accountId: Long,
        @Query("limit") limit: Long? = null,
        @Query("offset") offset: Long? = null,
        @Query("win") win: Long? = null,
        @Query("patch") patch: Long? = null,
        @Query("game_mode") gameMode: Long? = null,
        @Query("lobby_type") lobbyType: Long? = null,
        @Query("region") region: Long? = null,
        @Query("date") date: Long? = null,
        @Query("lane_role") laneRole: Long? = null,
        @Query("hero_id") heroId: Long? = null,
        @Query("is_radiant") isRadiant: Long? = null,
        @Query("included_account_id") includedAccountId: Long? = null,
        @Query("excluded_account_id") excludedAccountId: Long? = null,
        @Query("with_hero_id") withHeroId: Long? = null,
        @Query("against_hero_id") againstHeroId: Long? = null,
        @Query("significant") significant: Long? = null,
        @Query("having") having: Long? = null,
        @Query("sort") sort: String? = null,
    ): PlayerWinLossResponse

    /**
     * Recent matches played (limited number of results).
     *
     * `GET /players/{account_id}/recentMatches`
     *
     * @param accountId Steam32 account ID.
     * @return Recent matches played (limited number of results).
     */
    @GET("players/{account_id}/recentMatches")
    suspend fun getPlayerRecentMatches(@Path("account_id") accountId: Long): List<PlayerRecentMatchesResponse>

    /**
     * Matches played (full history, and supports column selection).
     *
     * `GET /players/{account_id}/matches`
     *
     * @param accountId Steam32 account ID.
     * @param limit Number of matches to limit to.
     * @param offset Number of matches to offset start by.
     * @param win Whether the player won.
     * @param patch Patch ID, from dotaconstants.
     * @param gameMode Game Mode ID.
     * @param lobbyType Lobby type ID.
     * @param region Region ID.
     * @param date Days previous.
     * @param laneRole Lane Role ID.
     * @param heroId Hero ID.
     * @param isRadiant Whether the player was radiant.
     * @param includedAccountId Account IDs in the match (array).
     * @param excludedAccountId Account IDs not in the match (array).
     * @param withHeroId Hero IDs on the player's team (array).
     * @param againstHeroId Hero IDs against the player's team (array).
     * @param significant Whether the match was significant for aggregation purposes. Defaults to 1 (true), set this to
     *   0 to return data for non-standard modes/matches.
     * @param having The minimum number of games played, for filtering hero stats.
     * @param sort The field to return matches sorted by in descending order.
     * @param project Fields to project (array).
     * @return Matches played (full history, and supports column selection).
     */
    @GET("players/{account_id}/matches")
    suspend fun getPlayerMatches(
        @Path("account_id") accountId: Long,
        @Query("limit") limit: Long? = null,
        @Query("offset") offset: Long? = null,
        @Query("win") win: Long? = null,
        @Query("patch") patch: Long? = null,
        @Query("game_mode") gameMode: Long? = null,
        @Query("lobby_type") lobbyType: Long? = null,
        @Query("region") region: Long? = null,
        @Query("date") date: Long? = null,
        @Query("lane_role") laneRole: Long? = null,
        @Query("hero_id") heroId: Long? = null,
        @Query("is_radiant") isRadiant: Long? = null,
        @Query("included_account_id") includedAccountId: Long? = null,
        @Query("excluded_account_id") excludedAccountId: Long? = null,
        @Query("with_hero_id") withHeroId: Long? = null,
        @Query("against_hero_id") againstHeroId: Long? = null,
        @Query("significant") significant: Long? = null,
        @Query("having") having: Long? = null,
        @Query("sort") sort: String? = null,
        @Query("project") project: String? = null,
    ): List<PlayerMatchesResponse>

    /**
     * Heroes played.
     *
     * `GET /players/{account_id}/heroes`
     *
     * @param accountId Steam32 account ID.
     * @param limit Number of matches to limit to.
     * @param offset Number of matches to offset start by.
     * @param win Whether the player won.
     * @param patch Patch ID, from dotaconstants.
     * @param gameMode Game Mode ID.
     * @param lobbyType Lobby type ID.
     * @param region Region ID.
     * @param date Days previous.
     * @param laneRole Lane Role ID.
     * @param heroId Hero ID.
     * @param isRadiant Whether the player was radiant.
     * @param includedAccountId Account IDs in the match (array).
     * @param excludedAccountId Account IDs not in the match (array).
     * @param withHeroId Hero IDs on the player's team (array).
     * @param againstHeroId Hero IDs against the player's team (array).
     * @param significant Whether the match was significant for aggregation purposes. Defaults to 1 (true), set this to
     *   0 to return data for non-standard modes/matches.
     * @param having The minimum number of games played, for filtering hero stats.
     * @param sort The field to return matches sorted by in descending order.
     * @return Heroes played.
     */
    @GET("players/{account_id}/heroes")
    suspend fun getPlayerHeroes(
        @Path("account_id") accountId: Long,
        @Query("limit") limit: Long? = null,
        @Query("offset") offset: Long? = null,
        @Query("win") win: Long? = null,
        @Query("patch") patch: Long? = null,
        @Query("game_mode") gameMode: Long? = null,
        @Query("lobby_type") lobbyType: Long? = null,
        @Query("region") region: Long? = null,
        @Query("date") date: Long? = null,
        @Query("lane_role") laneRole: Long? = null,
        @Query("hero_id") heroId: Long? = null,
        @Query("is_radiant") isRadiant: Long? = null,
        @Query("included_account_id") includedAccountId: Long? = null,
        @Query("excluded_account_id") excludedAccountId: Long? = null,
        @Query("with_hero_id") withHeroId: Long? = null,
        @Query("against_hero_id") againstHeroId: Long? = null,
        @Query("significant") significant: Long? = null,
        @Query("having") having: Long? = null,
        @Query("sort") sort: String? = null,
    ): List<PlayerHeroesResponse>

    /**
     * Players played with.
     *
     * `GET /players/{account_id}/peers`
     *
     * @param accountId Steam32 account ID.
     * @param limit Number of matches to limit to.
     * @param offset Number of matches to offset start by.
     * @param win Whether the player won.
     * @param patch Patch ID, from dotaconstants.
     * @param gameMode Game Mode ID.
     * @param lobbyType Lobby type ID.
     * @param region Region ID.
     * @param date Days previous.
     * @param laneRole Lane Role ID.
     * @param heroId Hero ID.
     * @param isRadiant Whether the player was radiant.
     * @param includedAccountId Account IDs in the match (array).
     * @param excludedAccountId Account IDs not in the match (array).
     * @param withHeroId Hero IDs on the player's team (array).
     * @param againstHeroId Hero IDs against the player's team (array).
     * @param significant Whether the match was significant for aggregation purposes. Defaults to 1 (true), set this to
     *   0 to return data for non-standard modes/matches.
     * @param having The minimum number of games played, for filtering hero stats.
     * @param sort The field to return matches sorted by in descending order.
     * @return Players played with.
     */
    @GET("players/{account_id}/peers")
    suspend fun getPlayerPeers(
        @Path("account_id") accountId: Long,
        @Query("limit") limit: Long? = null,
        @Query("offset") offset: Long? = null,
        @Query("win") win: Long? = null,
        @Query("patch") patch: Long? = null,
        @Query("game_mode") gameMode: Long? = null,
        @Query("lobby_type") lobbyType: Long? = null,
        @Query("region") region: Long? = null,
        @Query("date") date: Long? = null,
        @Query("lane_role") laneRole: Long? = null,
        @Query("hero_id") heroId: Long? = null,
        @Query("is_radiant") isRadiant: Long? = null,
        @Query("included_account_id") includedAccountId: Long? = null,
        @Query("excluded_account_id") excludedAccountId: Long? = null,
        @Query("with_hero_id") withHeroId: Long? = null,
        @Query("against_hero_id") againstHeroId: Long? = null,
        @Query("significant") significant: Long? = null,
        @Query("having") having: Long? = null,
        @Query("sort") sort: String? = null,
    ): List<PlayerPeersResponse>

    /**
     * Pro players played with.
     *
     * `GET /players/{account_id}/pros`
     *
     * @param accountId Steam32 account ID.
     * @param limit Number of matches to limit to.
     * @param offset Number of matches to offset start by.
     * @param win Whether the player won.
     * @param patch Patch ID, from dotaconstants.
     * @param gameMode Game Mode ID.
     * @param lobbyType Lobby type ID.
     * @param region Region ID.
     * @param date Days previous.
     * @param laneRole Lane Role ID.
     * @param heroId Hero ID.
     * @param isRadiant Whether the player was radiant.
     * @param includedAccountId Account IDs in the match (array).
     * @param excludedAccountId Account IDs not in the match (array).
     * @param withHeroId Hero IDs on the player's team (array).
     * @param againstHeroId Hero IDs against the player's team (array).
     * @param significant Whether the match was significant for aggregation purposes. Defaults to 1 (true), set this to
     *   0 to return data for non-standard modes/matches.
     * @param having The minimum number of games played, for filtering hero stats.
     * @param sort The field to return matches sorted by in descending order.
     * @return Pro players played with.
     */
    @GET("players/{account_id}/pros")
    suspend fun getPlayerPros(
        @Path("account_id") accountId: Long,
        @Query("limit") limit: Long? = null,
        @Query("offset") offset: Long? = null,
        @Query("win") win: Long? = null,
        @Query("patch") patch: Long? = null,
        @Query("game_mode") gameMode: Long? = null,
        @Query("lobby_type") lobbyType: Long? = null,
        @Query("region") region: Long? = null,
        @Query("date") date: Long? = null,
        @Query("lane_role") laneRole: Long? = null,
        @Query("hero_id") heroId: Long? = null,
        @Query("is_radiant") isRadiant: Long? = null,
        @Query("included_account_id") includedAccountId: Long? = null,
        @Query("excluded_account_id") excludedAccountId: Long? = null,
        @Query("with_hero_id") withHeroId: Long? = null,
        @Query("against_hero_id") againstHeroId: Long? = null,
        @Query("significant") significant: Long? = null,
        @Query("having") having: Long? = null,
        @Query("sort") sort: String? = null,
    ): List<PlayerProsResponse>

    /**
     * Totals in stats.
     *
     * `GET /players/{account_id}/totals`
     *
     * @param accountId Steam32 account ID.
     * @param limit Number of matches to limit to.
     * @param offset Number of matches to offset start by.
     * @param win Whether the player won.
     * @param patch Patch ID, from dotaconstants.
     * @param gameMode Game Mode ID.
     * @param lobbyType Lobby type ID.
     * @param region Region ID.
     * @param date Days previous.
     * @param laneRole Lane Role ID.
     * @param heroId Hero ID.
     * @param isRadiant Whether the player was radiant.
     * @param includedAccountId Account IDs in the match (array).
     * @param excludedAccountId Account IDs not in the match (array).
     * @param withHeroId Hero IDs on the player's team (array).
     * @param againstHeroId Hero IDs against the player's team (array).
     * @param significant Whether the match was significant for aggregation purposes. Defaults to 1 (true), set this to
     *   0 to return data for non-standard modes/matches.
     * @param having The minimum number of games played, for filtering hero stats.
     * @param sort The field to return matches sorted by in descending order.
     * @return Totals in stats.
     */
    @GET("players/{account_id}/totals")
    suspend fun getPlayerTotals(
        @Path("account_id") accountId: Long,
        @Query("limit") limit: Long? = null,
        @Query("offset") offset: Long? = null,
        @Query("win") win: Long? = null,
        @Query("patch") patch: Long? = null,
        @Query("game_mode") gameMode: Long? = null,
        @Query("lobby_type") lobbyType: Long? = null,
        @Query("region") region: Long? = null,
        @Query("date") date: Long? = null,
        @Query("lane_role") laneRole: Long? = null,
        @Query("hero_id") heroId: Long? = null,
        @Query("is_radiant") isRadiant: Long? = null,
        @Query("included_account_id") includedAccountId: Long? = null,
        @Query("excluded_account_id") excludedAccountId: Long? = null,
        @Query("with_hero_id") withHeroId: Long? = null,
        @Query("against_hero_id") againstHeroId: Long? = null,
        @Query("significant") significant: Long? = null,
        @Query("having") having: Long? = null,
        @Query("sort") sort: String? = null,
    ): List<PlayerTotalsResponse>

    /**
     * Counts in categories.
     *
     * `GET /players/{account_id}/counts`
     *
     * @param accountId Steam32 account ID.
     * @param limit Number of matches to limit to.
     * @param offset Number of matches to offset start by.
     * @param win Whether the player won.
     * @param patch Patch ID, from dotaconstants.
     * @param gameMode Game Mode ID.
     * @param lobbyType Lobby type ID.
     * @param region Region ID.
     * @param date Days previous.
     * @param laneRole Lane Role ID.
     * @param heroId Hero ID.
     * @param isRadiant Whether the player was radiant.
     * @param includedAccountId Account IDs in the match (array).
     * @param excludedAccountId Account IDs not in the match (array).
     * @param withHeroId Hero IDs on the player's team (array).
     * @param againstHeroId Hero IDs against the player's team (array).
     * @param significant Whether the match was significant for aggregation purposes. Defaults to 1 (true), set this to
     *   0 to return data for non-standard modes/matches.
     * @param having The minimum number of games played, for filtering hero stats.
     * @param sort The field to return matches sorted by in descending order.
     * @return Counts in categories.
     */
    @GET("players/{account_id}/counts")
    suspend fun getPlayerCounts(
        @Path("account_id") accountId: Long,
        @Query("limit") limit: Long? = null,
        @Query("offset") offset: Long? = null,
        @Query("win") win: Long? = null,
        @Query("patch") patch: Long? = null,
        @Query("game_mode") gameMode: Long? = null,
        @Query("lobby_type") lobbyType: Long? = null,
        @Query("region") region: Long? = null,
        @Query("date") date: Long? = null,
        @Query("lane_role") laneRole: Long? = null,
        @Query("hero_id") heroId: Long? = null,
        @Query("is_radiant") isRadiant: Long? = null,
        @Query("included_account_id") includedAccountId: Long? = null,
        @Query("excluded_account_id") excludedAccountId: Long? = null,
        @Query("with_hero_id") withHeroId: Long? = null,
        @Query("against_hero_id") againstHeroId: Long? = null,
        @Query("significant") significant: Long? = null,
        @Query("having") having: Long? = null,
        @Query("sort") sort: String? = null,
    ): PlayerCountsResponse

    /**
     * Distribution of matches in a single stat.
     *
     * `GET /players/{account_id}/histograms/{field}`
     *
     * @param accountId Steam32 account ID.
     * @param field Field to aggregate on.
     * @param limit Number of matches to limit to.
     * @param offset Number of matches to offset start by.
     * @param win Whether the player won.
     * @param patch Patch ID, from dotaconstants.
     * @param gameMode Game Mode ID.
     * @param lobbyType Lobby type ID.
     * @param region Region ID.
     * @param date Days previous.
     * @param laneRole Lane Role ID.
     * @param heroId Hero ID.
     * @param isRadiant Whether the player was radiant.
     * @param includedAccountId Account IDs in the match (array).
     * @param excludedAccountId Account IDs not in the match (array).
     * @param withHeroId Hero IDs on the player's team (array).
     * @param againstHeroId Hero IDs against the player's team (array).
     * @param significant Whether the match was significant for aggregation purposes. Defaults to 1 (true), set this to
     *   0 to return data for non-standard modes/matches.
     * @param having The minimum number of games played, for filtering hero stats.
     * @param sort The field to return matches sorted by in descending order.
     * @return The raw JSON histogram buckets for the requested field.
     */
    @GET("players/{account_id}/histograms/{field}")
    suspend fun getPlayerHistograms(
        @Path("account_id") accountId: Long,
        @Path("field") field: String,
        @Query("limit") limit: Long? = null,
        @Query("offset") offset: Long? = null,
        @Query("win") win: Long? = null,
        @Query("patch") patch: Long? = null,
        @Query("game_mode") gameMode: Long? = null,
        @Query("lobby_type") lobbyType: Long? = null,
        @Query("region") region: Long? = null,
        @Query("date") date: Long? = null,
        @Query("lane_role") laneRole: Long? = null,
        @Query("hero_id") heroId: Long? = null,
        @Query("is_radiant") isRadiant: Long? = null,
        @Query("included_account_id") includedAccountId: Long? = null,
        @Query("excluded_account_id") excludedAccountId: Long? = null,
        @Query("with_hero_id") withHeroId: Long? = null,
        @Query("against_hero_id") againstHeroId: Long? = null,
        @Query("significant") significant: Long? = null,
        @Query("having") having: Long? = null,
        @Query("sort") sort: String? = null,
    ): List<JsonElement>

    /**
     * Wards placed in matches played.
     *
     * `GET /players/{account_id}/wardmap`
     *
     * @param accountId Steam32 account ID.
     * @param limit Number of matches to limit to.
     * @param offset Number of matches to offset start by.
     * @param win Whether the player won.
     * @param patch Patch ID, from dotaconstants.
     * @param gameMode Game Mode ID.
     * @param lobbyType Lobby type ID.
     * @param region Region ID.
     * @param date Days previous.
     * @param laneRole Lane Role ID.
     * @param heroId Hero ID.
     * @param isRadiant Whether the player was radiant.
     * @param includedAccountId Account IDs in the match (array).
     * @param excludedAccountId Account IDs not in the match (array).
     * @param withHeroId Hero IDs on the player's team (array).
     * @param againstHeroId Hero IDs against the player's team (array).
     * @param significant Whether the match was significant for aggregation purposes. Defaults to 1 (true), set this to
     *   0 to return data for non-standard modes/matches.
     * @param having The minimum number of games played, for filtering hero stats.
     * @param sort The field to return matches sorted by in descending order.
     * @return Wards placed in matches played.
     */
    @GET("players/{account_id}/wardmap")
    suspend fun getPlayerWardMap(
        @Path("account_id") accountId: Long,
        @Query("limit") limit: Long? = null,
        @Query("offset") offset: Long? = null,
        @Query("win") win: Long? = null,
        @Query("patch") patch: Long? = null,
        @Query("game_mode") gameMode: Long? = null,
        @Query("lobby_type") lobbyType: Long? = null,
        @Query("region") region: Long? = null,
        @Query("date") date: Long? = null,
        @Query("lane_role") laneRole: Long? = null,
        @Query("hero_id") heroId: Long? = null,
        @Query("is_radiant") isRadiant: Long? = null,
        @Query("included_account_id") includedAccountId: Long? = null,
        @Query("excluded_account_id") excludedAccountId: Long? = null,
        @Query("with_hero_id") withHeroId: Long? = null,
        @Query("against_hero_id") againstHeroId: Long? = null,
        @Query("significant") significant: Long? = null,
        @Query("having") having: Long? = null,
        @Query("sort") sort: String? = null,
    ): PlayerWardMapResponse

    /**
     * Words said/read in matches played.
     *
     * `GET /players/{account_id}/wordcloud`
     *
     * @param accountId Steam32 account ID.
     * @param limit Number of matches to limit to.
     * @param offset Number of matches to offset start by.
     * @param win Whether the player won.
     * @param patch Patch ID, from dotaconstants.
     * @param gameMode Game Mode ID.
     * @param lobbyType Lobby type ID.
     * @param region Region ID.
     * @param date Days previous.
     * @param laneRole Lane Role ID.
     * @param heroId Hero ID.
     * @param isRadiant Whether the player was radiant.
     * @param includedAccountId Account IDs in the match (array).
     * @param excludedAccountId Account IDs not in the match (array).
     * @param withHeroId Hero IDs on the player's team (array).
     * @param againstHeroId Hero IDs against the player's team (array).
     * @param significant Whether the match was significant for aggregation purposes. Defaults to 1 (true), set this to
     *   0 to return data for non-standard modes/matches.
     * @param having The minimum number of games played, for filtering hero stats.
     * @param sort The field to return matches sorted by in descending order.
     * @return Words said/read in matches played.
     */
    @GET("players/{account_id}/wordcloud")
    suspend fun getPlayerWordCloud(
        @Path("account_id") accountId: Long,
        @Query("limit") limit: Long? = null,
        @Query("offset") offset: Long? = null,
        @Query("win") win: Long? = null,
        @Query("patch") patch: Long? = null,
        @Query("game_mode") gameMode: Long? = null,
        @Query("lobby_type") lobbyType: Long? = null,
        @Query("region") region: Long? = null,
        @Query("date") date: Long? = null,
        @Query("lane_role") laneRole: Long? = null,
        @Query("hero_id") heroId: Long? = null,
        @Query("is_radiant") isRadiant: Long? = null,
        @Query("included_account_id") includedAccountId: Long? = null,
        @Query("excluded_account_id") excludedAccountId: Long? = null,
        @Query("with_hero_id") withHeroId: Long? = null,
        @Query("against_hero_id") againstHeroId: Long? = null,
        @Query("significant") significant: Long? = null,
        @Query("having") having: Long? = null,
        @Query("sort") sort: String? = null,
    ): PlayerWordCloudResponse

    /**
     * Returns a history of the player rank tier/medal changes (replaces MMR).
     *
     * `GET /players/{account_id}/ratings`
     *
     * @param accountId Steam32 account ID.
     * @return History of the player rank tier/medal changes (replaces MMR).
     */
    @GET("players/{account_id}/ratings")
    suspend fun getPlayerRatings(@Path("account_id") accountId: Long): List<PlayerRatingsResponse>

    /**
     * Player hero rankings.
     *
     * `GET /players/{account_id}/rankings`
     *
     * @param accountId Steam32 account ID.
     * @return Player hero rankings.
     */
    @GET("players/{account_id}/rankings")
    suspend fun getPlayerRankings(@Path("account_id") accountId: Long): List<PlayerRankingsResponse>

    /**
     * Refresh player match history (up to 500), medal (rank), and profile name.
     *
     * `POST /players/{account_id}/refresh`
     *
     * @param accountId Steam32 account ID.
     * @return The raw JSON acknowledgement returned by the API.
     */
    @POST("players/{account_id}/refresh")
    suspend fun refreshPlayer(@Path("account_id") accountId: Long): JsonElement

    /**
     * Get list of highly ranked players.
     *
     * `GET /topPlayers`
     *
     * @param turbo Get ratings based on turbo matches.
     * @return The list of highly ranked players.
     */
    @GET("topPlayers")
    suspend fun getTopPlayers(@Query("turbo") turbo: Long? = null): List<PlayerObjectResponse>

    /**
     * Get list of pro players.
     *
     * `GET /proPlayers`
     *
     * @return The list of pro players.
     */
    @GET("proPlayers")
    suspend fun getProPlayers(): List<PlayerObjectResponse>

    /**
     * Get list of pro matches.
     *
     * `GET /proMatches`
     *
     * @param lessThanMatchId Get matches with a match ID lower than this value.
     * @return The list of pro matches.
     */
    @GET("proMatches")
    suspend fun getProMatches(@Query("less_than_match_id") lessThanMatchId: Long? = null): List<MatchObjectResponse>

    /**
     * Get list of randomly sampled public matches.
     *
     * `GET /publicMatches`
     *
     * @param lessThanMatchId Get matches with a match ID lower than this value.
     * @param minRank Minimum rank for the matches. Ranks are represented by integers (10-15: Herald, 20-25: Guardian,
     *   30-35: Crusader, 40-45: Archon, 50-55: Legend, 60-65: Ancient, 70-75: Divine, 80: Immortal). Each increment
     *   represents an additional star.
     * @param maxRank Maximum rank for the matches. Ranks are represented by integers (10-15: Herald, 20-25: Guardian,
     *   30-35: Crusader, 40-45: Archon, 50-55: Legend, 60-65: Ancient, 70-75: Divine, 80: Immortal). Each increment
     *   represents an additional star.
     * @return The list of randomly sampled public matches.
     */
    @GET("publicMatches")
    suspend fun getPublicMatches(
        @Query("less_than_match_id") lessThanMatchId: Long? = null,
        @Query("min_rank") minRank: Long? = null,
        @Query("max_rank") maxRank: Long? = null,
    ): List<PublicMatchesResponse>

    /**
     * Get list of parsed match IDs.
     *
     * `GET /parsedMatches`
     *
     * @param lessThanMatchId Get matches with a match ID lower than this value.
     * @return The list of parsed match IDs.
     */
    @GET("parsedMatches")
    suspend fun getParsedMatches(@Query("less_than_match_id") lessThanMatchId: Long? = null): List<ParsedMatchesResponse>

    /**
     * Submit arbitrary SQL queries to the database.
     *
     * `GET /explorer`
     *
     * @param sql The PostgreSQL query as percent-encoded string.
     * @return The raw JSON result of the query.
     */
    @GET("explorer")
    suspend fun getExplorer(@Query("sql") sql: String? = null): JsonElement

    /**
     * Site metadata.
     *
     * `GET /metadata`
     *
     * @return Site metadata.
     */
    @GET("metadata")
    suspend fun getMetadata(): MetadataResponse

    /**
     * Distributions of MMR data by bracket and country.
     *
     * `GET /distributions`
     *
     * @return Distributions of MMR data by bracket and country.
     */
    @GET("distributions")
    suspend fun getDistributions(): DistributionsResponse

    /**
     * Search players by personaname.
     *
     * `GET /search`
     *
     * @param q Search string.
     * @return Search players by personaname.
     */
    @GET("search")
    suspend fun search(@Query("q") q: String): List<SearchResponse>

    /**
     * Top players by hero.
     *
     * `GET /rankings`
     *
     * @param heroId Hero ID.
     * @return Top players by hero.
     */
    @GET("rankings")
    suspend fun getRankings(@Query("hero_id") heroId: String): RankingsResponse

    /**
     * Benchmarks of average stat values for a hero.
     *
     * `GET /benchmarks`
     *
     * @param heroId Hero ID.
     * @return Benchmarks of average stat values for a hero.
     */
    @GET("benchmarks")
    suspend fun getBenchmarks(@Query("hero_id") heroId: String): BenchmarksResponse

    /**
     * Get service health data.
     *
     * `GET /health`
     *
     * @return The raw JSON service health report.
     */
    @GET("health")
    suspend fun getHealth(): JsonElement

    /**
     * Get parse request state.
     *
     * `GET /request/{jobId}`
     *
     * @param jobId The job ID to query.
     * @return The raw JSON state of the parse job.
     */
    @GET("request/{jobId}")
    suspend fun getRequestStatus(@Path("jobId") jobId: String): JsonElement

    /**
     * Submit a new parse request. This call counts as 10 calls for rate limit (but not billing) purposes.
     *
     * `POST /request/{match_id}`
     *
     * @param matchId Match ID of the replay to parse.
     * @return The raw JSON description of the queued parse job.
     */
    @POST("request/{match_id}")
    suspend fun submitRequest(@Path("match_id") matchId: Long): JsonElement

    /**
     * Finds recent matches by heroes played.
     *
     * `GET /findMatches`
     *
     * @param teamA Hero IDs on first team (array).
     * @param teamB Hero IDs on second team (array).
     * @return The raw JSON list of matching matches.
     */
    @GET("findMatches")
    suspend fun findMatches(
        @Query("teamA") teamA: List<Long>? = null,
        @Query("teamB") teamB: List<Long>? = null,
    ): List<JsonElement>

    /**
     * Get hero data.
     *
     * `GET /heroes`
     *
     * @return Hero data.
     */
    @GET("heroes")
    suspend fun getHeroes(): List<HeroObjectResponse>

    /**
     * Get stats about hero performance in recent matches.
     *
     * `GET /heroStats`
     *
     * @return Stats about hero performance in recent matches.
     */
    @GET("heroStats")
    suspend fun getHeroStats(): List<HeroStatsResponse>

    /**
     * Get recent matches with a hero.
     *
     * `GET /heroes/{hero_id}/matches`
     *
     * @param heroId Hero ID.
     * @return Recent matches with a hero.
     */
    @GET("heroes/{hero_id}/matches")
    suspend fun getHeroMatches(@Path("hero_id") heroId: Long): List<MatchObjectResponse>

    /**
     * Get results against other heroes for a hero.
     *
     * `GET /heroes/{hero_id}/matchups`
     *
     * @param heroId Hero ID.
     * @return Results against other heroes for a hero.
     */
    @GET("heroes/{hero_id}/matchups")
    suspend fun getHeroMatchups(@Path("hero_id") heroId: Long): List<HeroMatchupsResponse>

    /**
     * Get hero performance over a range of match durations.
     *
     * `GET /heroes/{hero_id}/durations`
     *
     * @param heroId Hero ID.
     * @return Hero performance over a range of match durations.
     */
    @GET("heroes/{hero_id}/durations")
    suspend fun getHeroDurations(@Path("hero_id") heroId: Long): List<HeroDurationsResponse>

    /**
     * Get players who have played this hero.
     *
     * `GET /heroes/{hero_id}/players`
     *
     * @param heroId Hero ID.
     * @return Players who have played this hero.
     */
    @GET("heroes/{hero_id}/players")
    suspend fun getHeroPlayers(@Path("hero_id") heroId: Long): List<List<PlayerObjectResponse>>

    /**
     * Get item popularity of hero categoried by start, early, mid and late game, analyzed from professional games.
     *
     * `GET /heroes/{hero_id}/itemPopularity`
     *
     * @param heroId Hero ID.
     * @return Item popularity of hero categoried by start, early, mid and late game, analyzed from professional games.
     */
    @GET("heroes/{hero_id}/itemPopularity")
    suspend fun getHeroItemPopularity(@Path("hero_id") heroId: Long): HeroItemPopularityResponse

    /**
     * Get league data.
     *
     * `GET /leagues`
     *
     * @return League data.
     */
    @GET("leagues")
    suspend fun getLeagues(): List<LeagueObjectResponse>

    /**
     * Get data for a league.
     *
     * `GET /leagues/{league_id}`
     *
     * @param leagueId League ID.
     * @return Data for a league.
     */
    @GET("leagues/{league_id}")
    suspend fun getLeague(@Path("league_id") leagueId: Long): List<LeagueObjectResponse>

    /**
     * Get matches for a league (excluding amateur leagues).
     *
     * `GET /leagues/{league_id}/matches`
     *
     * @param leagueId League ID.
     * @return Matches for a league (excluding amateur leagues).
     */
    @GET("leagues/{league_id}/matches")
    suspend fun getLeagueMatches(@Path("league_id") leagueId: Long): MatchObjectResponse

    /**
     * Get match IDs for a league (including amateur leagues).
     *
     * `GET /leagues/{league_id}/matchIds`
     *
     * @param leagueId League ID.
     * @return Match IDs for a league (including amateur leagues).
     */
    @GET("leagues/{league_id}/matchIds")
    suspend fun getLeagueMatchIds(@Path("league_id") leagueId: Long): List<String>

    /**
     * Get teams for a league.
     *
     * `GET /leagues/{league_id}/teams`
     *
     * @param leagueId League ID.
     * @return Teams for a league.
     */
    @GET("leagues/{league_id}/teams")
    suspend fun getLeagueTeams(@Path("league_id") leagueId: Long): TeamObjectResponse

    /**
     * Get team data.
     *
     * `GET /teams`
     *
     * @param page Page number, zero indexed. Each page returns up to 1000 entries.
     * @return Team data.
     */
    @GET("teams")
    suspend fun getTeams(@Query("page") page: Long? = null): List<TeamObjectResponse>

    /**
     * Get data for a team.
     *
     * `GET /teams/{team_id}`
     *
     * @param teamId Team ID.
     * @return Data for a team.
     */
    @GET("teams/{team_id}")
    suspend fun getTeam(@Path("team_id") teamId: Long): TeamObjectResponse

    /**
     * Get matches for a team.
     *
     * `GET /teams/{team_id}/matches`
     *
     * @param teamId Team ID.
     * @return Matches for a team.
     */
    @GET("teams/{team_id}/matches")
    suspend fun getTeamMatches(@Path("team_id") teamId: Long): TeamMatchObjectResponse

    /**
     * Get players who have played for a team.
     *
     * `GET /teams/{team_id}/players`
     *
     * @param teamId Team ID.
     * @return Players who have played for a team.
     */
    @GET("teams/{team_id}/players")
    suspend fun getTeamPlayers(@Path("team_id") teamId: Long): TeamPlayersResponse

    /**
     * Get heroes for a team.
     *
     * `GET /teams/{team_id}/heroes`
     *
     * @param teamId Team ID.
     * @return Heroes for a team.
     */
    @GET("teams/{team_id}/heroes")
    suspend fun getTeamHeroes(@Path("team_id") teamId: Long): TeamHeroesResponse

    /**
     * Get top performances in a stat.
     *
     * `GET /records/{field}`
     *
     * @param field Field name to query.
     * @return Top performances in a stat.
     */
    @GET("records/{field}")
    suspend fun getRecords(@Path("field") field: String): List<RecordsResponse>

    /**
     * Get top currently ongoing live games.
     *
     * `GET /live`
     *
     * @return The raw JSON list of ongoing games.
     */
    @GET("live")
    suspend fun getLive(): List<JsonElement>

    /**
     * Win rates for certain item timings on a hero for items that cost at least 1400 gold.
     *
     * `GET /scenarios/itemTimings`
     *
     * @param item Filter by item name e.g. "spirit_vessel".
     * @param heroId Hero ID.
     * @return Win rates for certain item timings on a hero for items that cost at least 1400 gold.
     */
    @GET("scenarios/itemTimings")
    suspend fun getItemTimings(
        @Query("item") item: String? = null,
        @Query("hero_id") heroId: Long? = null,
    ): List<ScenarioItemTimingsResponse>

    /**
     * Win rates for heroes in certain lane roles.
     *
     * `GET /scenarios/laneRoles`
     *
     * @param laneRole Filter by lane role 1-4 (Safe, Mid, Off, Jungle).
     * @param heroId Hero ID.
     * @return Win rates for heroes in certain lane roles.
     */
    @GET("scenarios/laneRoles")
    suspend fun getLaneRoles(
        @Query("lane_role") laneRole: String? = null,
        @Query("hero_id") heroId: Long? = null,
    ): List<ScenarioLaneRolesResponse>

    /**
     * Miscellaneous team scenarios.
     *
     * `GET /scenarios/misc`
     *
     * @param scenario Name of the scenario (see teamScenariosQueryParams).
     * @return Miscellaneous team scenarios.
     */
    @GET("scenarios/misc")
    suspend fun getMiscScenarios(@Query("scenario") scenario: String? = null): List<ScenarioMiscResponse>

    /**
     * Get database schema.
     *
     * `GET /schema`
     *
     * @return Database schema.
     */
    @GET("schema")
    suspend fun getSchema(): List<SchemaResponse>

    /**
     * Get static game data mirrored from the dotaconstants repository.
     *
     * `GET /constants/{resource}`
     *
     * @param resource Resource name e.g. `heroes`. [List of
     *   resources](https://github.com/odota/dotaconstants/tree/master/build).
     * @return The raw JSON contents of the requested dotaconstants resource.
     */
    @GET("constants/{resource}")
    suspend fun getConstants(@Path("resource") resource: String): JsonElement

}
