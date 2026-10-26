# Module kopendota

A non-official Kotlin client for the [OpenDota API](https://docs.opendota.com/), which serves Dota 2 match, player,
hero, team and league data — including the detailed statistics OpenDota extracts from match replays.

The library is a Retrofit service definition plus the models its endpoints return, with a client that assembles the
two. Every call is a `suspend` function, and every response is deserialised with kotlinx.serialization.

## Getting started

[OpenDotaClient][io.github.yesitskev.kopendota.OpenDotaClient] owns the Retrofit, OkHttp, JSON and rate limiting
wiring, including the base URL every endpoint path is resolved against:

```kotlin
val client = OpenDotaClient()

val match = client.openDota.getMatch(matchId = 7108753629L)
val wins = client.openDota.getPlayerWinLoss(accountId = 88367253L, heroId = 1L)
```

Every argument is optional. A subscription carries the API key and the allowances each call is counted against, and
the queue bound, serialiser, HTTP logging and timeouts are configured alongside it:

```kotlin
val client =
    OpenDotaClient(
        billingSubscription = BillingSubscription(apiKey = System.getenv("OPEN_DOTA_API_KEY")),
        queueLimit = 500,
        json = Json(from = OpenDotaClient.DEFAULT_JSON) { prettyPrint = true },
        logging = OpenDotaLogging(level = OpenDotaLogLevel.BASIC, logger = { message -> log.debug(message) }),
        timeouts = OpenDotaTimeouts(connect = 5.seconds, read = 60.seconds),
    )
```

The default serialiser ignores unknown keys, and one of your own should too: OpenDota adds fields over time, and the
models here track a single snapshot of the schema.

A client owns an OkHttp connection pool, the threads that serve it and the scope queued calls wait on, so build one
and share it rather than creating one per call; `close()` releases all three. Building the service with Retrofit
directly stays an option, and [OpenDota][io.github.yesitskev.kopendota.OpenDota] carries a worked example — though a
service built that way is not rate limited, since nothing holds a call back or counts it.

## What the API covers

| Area | Entry points |
| --- | --- |
| Matches | [getMatch][io.github.yesitskev.kopendota.OpenDota.getMatch], [getProMatches][io.github.yesitskev.kopendota.OpenDota.getProMatches], [getPublicMatches][io.github.yesitskev.kopendota.OpenDota.getPublicMatches], [getParsedMatches][io.github.yesitskev.kopendota.OpenDota.getParsedMatches] |
| Players | [getPlayer][io.github.yesitskev.kopendota.OpenDota.getPlayer], [getPlayerMatches][io.github.yesitskev.kopendota.OpenDota.getPlayerMatches], [getPlayerHeroes][io.github.yesitskev.kopendota.OpenDota.getPlayerHeroes], [getPlayerPeers][io.github.yesitskev.kopendota.OpenDota.getPlayerPeers] |
| Heroes | [getHeroes][io.github.yesitskev.kopendota.OpenDota.getHeroes], [getHeroStats][io.github.yesitskev.kopendota.OpenDota.getHeroStats], [getHeroMatchups][io.github.yesitskev.kopendota.OpenDota.getHeroMatchups], [getBenchmarks][io.github.yesitskev.kopendota.OpenDota.getBenchmarks] |
| Teams and leagues | [getTeams][io.github.yesitskev.kopendota.OpenDota.getTeams], [getTeamMatches][io.github.yesitskev.kopendota.OpenDota.getTeamMatches], [getLeagues][io.github.yesitskev.kopendota.OpenDota.getLeagues], [getLeagueMatches][io.github.yesitskev.kopendota.OpenDota.getLeagueMatches] |
| Aggregates | [getDistributions][io.github.yesitskev.kopendota.OpenDota.getDistributions], [getRankings][io.github.yesitskev.kopendota.OpenDota.getRankings], [getRecords][io.github.yesitskev.kopendota.OpenDota.getRecords], [getItemTimings][io.github.yesitskev.kopendota.OpenDota.getItemTimings] |
| Replay parsing | [submitRequest][io.github.yesitskev.kopendota.OpenDota.submitRequest], [getRequestStatus][io.github.yesitskev.kopendota.OpenDota.getRequestStatus] |
| Raw access | [getExplorer][io.github.yesitskev.kopendota.OpenDota.getExplorer], [getSchema][io.github.yesitskev.kopendota.OpenDota.getSchema], [getConstants][io.github.yesitskev.kopendota.OpenDota.getConstants] |

## Things to know

- **Nullability.** Every model property is nullable, because OpenDota omits fields it has no data for. A parsed match
  carries far more detail than an unparsed one, so the replay-derived fields on
  [MatchResponsePlayers][io.github.yesitskev.kopendota.model.MatchResponsePlayers] are frequently absent.
- **Filters.** The player endpoints share a large set of optional query parameters — `limit`, `win`, `heroId`,
  `laneRole` and friends. Leaving one at its `null` default omits it from the request.
- **Rate limits.** Every call is held to what a
  [BillingSubscription][io.github.yesitskev.kopendota.BillingSubscription] allows, by a
  [RateLimiter][io.github.yesitskev.kopendota.RateLimiter] that spends the free allowance — 3,000 requests a day and
  60 a minute — before reaching for the subscription, which allows an unlimited day and 3,000 a minute. A call waits
  where neither allowance has room, and
  [statistics][io.github.yesitskev.kopendota.OpenDotaClient.statistics] reports what is queued, what is in flight,
  and what each allowance has spent of its ceiling.
  [submitRequest][io.github.yesitskev.kopendota.OpenDota.submitRequest] counts as ten calls against it.
- **IDs.** Hero, item, ability, patch, region, game mode and lobby type IDs are resolved through the
  [dotaconstants](https://github.com/odota/dotaconstants) repository, mirrored by
  [getConstants][io.github.yesitskev.kopendota.OpenDota.getConstants].
- **Untyped payloads.** A few endpoints return shapes that are keyed dynamically or vary by request, so they are
  exposed as `JsonElement` rather than a model.

# Package io.github.yesitskev.kopendota

[OpenDotaClient], which builds a ready-to-use service, and the [OpenDota] service definition it builds, covering
every endpoint of the OpenDota API. [OpenDotaLogging] and [OpenDotaTimeouts] configure the client, and the error types
a client will raise live here too — of which [OpenDotaQueueLimitExceeded] is the one anything throws so far.

Rate limiting lives here as well. A [BillingSubscription] holds the API key, the [BillingLimits] of each
[BillingTier], and the [BillingSubscriptionCache] its request counts are kept in —
[InMemoryBillingSubscriptionCache] unless you put a shared store behind the interface. A [RateLimiter] holds every
call to those limits, reading the [BillingTierCapacity] of each tier and asking a [RequestStrategy] which to send on;
[FreeTierFirstRequestStrategy] spends the free allowance before the subscription.
[OpenDotaClient.statistics] reports the result as [OpenDotaStatistics], a count of what is queued and in flight
alongside the [OpenDotaUsage] each tier has spent of its day and its sliding minute.

# Package io.github.yesitskev.kopendota.model

Response models for the OpenDota API.

Each endpoint has a top-level model named after it — [MatchResponse] for `GET /matches/{match_id}`,
[PlayerWinLossResponse] for `GET /players/{account_id}/wl`, and so on. Models shared by several endpoints are named
`…ObjectResponse`, such as [MatchObjectResponse] and [PlayerObjectResponse].

Nested JSON objects become their own class, named by appending the field path to the owning model:
[MatchResponse.players] holds [MatchResponsePlayers], whose `purchase_log` field holds
[MatchResponsePlayersPurchaseLog]. Names map to JSON keys through `@SerialName`, so a property is camel-cased where the
API uses `snake_case`.
