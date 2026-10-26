# kopendota

[![Licence: GPL-3.0](https://img.shields.io/badge/licence-GPL--3.0-blue.svg)](LICENSE.md)
[![Kotlin](https://img.shields.io/badge/kotlin-2.4.10-7F52FF.svg)](https://kotlinlang.org)

A non-official Kotlin client for the [OpenDota API](https://docs.opendota.com/), which serves Dota 2 match, player,
hero, team and league data — including the detailed statistics OpenDota extracts from match replays.

- Every OpenDota endpoint exposed as a `suspend` function on a single interface
- Typed models for all responses, with field-level documentation
- Rate limiting that spends the free allowance before the subscription, queueing calls rather than dropping them
- Request counts kept behind an interface, so one allowance can be shared across processes
- Built on Retrofit 3, OkHttp 5 and kotlinx.serialization
- API reference generated with Dokka

## Status

Early. Distributed through [GitHub Packages](https://github.com/yesitskev/kopendota/packages) rather than Maven Central.

The service definition ([`OpenDota`](src/main/kotlin/io/github/yesitskev/kopendota/OpenDota.kt)), the response
models and [`OpenDotaClient`](src/main/kotlin/io/github/yesitskev/kopendota/OpenDotaClient.kt), which builds the
service for you, are complete and documented. So is rate limiting: calls are counted, held to the free allowance
before the subscription, and queued rather than dropped — see
[Rate limits, billing and an API key](#rate-limits-billing-and-an-api-key).

Of the `OpenDota*Exception` types only `OpenDotaQueueLimitExceeded` is implemented; the rest are still empty
placeholders, so other failures surface as the exceptions Retrofit and OkHttp raise. See [Usage](#usage) below.

## Requirements

- **JDK 17 or newer.** The build compiles against a Java 17 toolchain, so the jar carries Java 17 bytecode (class
  file version 61) whichever JDK you build it with.
- **Kotlin 2.0 or newer.** The library is built with Kotlin 2.4.10 but compiled at API and language version 2.0, so
  an older compiler can still read it.

## Setup

### From GitHub Packages

GitHub Packages serves no anonymous downloads, so the repository needs credentials even though this one is public:
a [personal access token](https://github.com/settings/tokens) carrying `read:packages`, kept out of the project in
`~/.gradle/gradle.properties`.

```properties
gpr.user=your-github-username
gpr.key=ghp_yourtoken
```

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/yesitskev/kopendota")
        credentials {
            username = providers.gradleProperty("gpr.user").get()
            password = providers.gradleProperty("gpr.key").get()
        }
    }
}

dependencies {
    implementation("io.github.yesitskev:kopendota:1.0.0")
}
```

`mavenCentral()` stays: Retrofit, OkHttp and kotlinx come from there, and only kopendota itself from GitHub.

### Build from source

To work against an unreleased change, build the jar locally:

```bash
git clone https://github.com/yesitskev/kopendota.git
cd kopendota
./gradlew build
# -> build/libs/kopendota-1.0-SNAPSHOT.jar
```

### Use it from a neighbouring project

A composite build avoids copying jars around. In your project's `settings.gradle.kts`:

```kotlin
includeBuild("../kopendota")
```

```kotlin
dependencies {
    implementation("io.github.yesitskev:kopendota")
}
```

### Dependencies you also need

None. Retrofit, kotlinx.serialization and kotlinx.coroutines are `api` dependencies, so they arrive on your compile
classpath with the library — Retrofit because the annotations on `OpenDota` belong to it, kotlinx.serialization
because `OpenDotaClient` takes a `Json` serialiser, and kotlinx.coroutines because every call is a `suspend` function
you will need a scope and a dispatcher to make. OkHttp, the logging interceptor and the Retrofit converter are
`implementation` dependencies: they reach your runtime classpath without appearing in your own API.

Declare one of them yourself only to pin a different version:

```kotlin
dependencies {
    implementation("io.github.yesitskev:kopendota:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
}
```

## Usage

### Creating the service

`OpenDotaClient` owns the Retrofit, OkHttp and JSON wiring, including the base URL, and hands you the service as
`openDota`:

```kotlin
import io.github.yesitskev.kopendota.OpenDotaClient

val client = OpenDotaClient()
val openDota = client.openDota
```

Every argument is optional, so configure only what you need:

```kotlin
import io.github.yesitskev.kopendota.BillingSubscription
import io.github.yesitskev.kopendota.OpenDotaClient
import io.github.yesitskev.kopendota.OpenDotaLogLevel
import io.github.yesitskev.kopendota.OpenDotaLogging
import io.github.yesitskev.kopendota.OpenDotaTimeouts
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

val client =
    OpenDotaClient(
        billingSubscription = BillingSubscription(apiKey = System.getenv("OPEN_DOTA_API_KEY")),
        queueLimit = 500,
        json = Json(from = OpenDotaClient.DEFAULT_JSON) { prettyPrint = true },
        logging = OpenDotaLogging(level = OpenDotaLogLevel.BASIC, logger = { message -> log.debug(message) }),
        timeouts = OpenDotaTimeouts(connect = 5.seconds, read = 60.seconds),
    )
```

| Argument              | Default                          | What it does                                                                                            |
|-----------------------|----------------------------------|---------------------------------------------------------------------------------------------------------|
| `billingSubscription` | Free allowance only              | Allowances every call is counted against, and the key subscription-tier calls carry.                    |
| `queueLimit`          | `0` (unlimited)                  | How many calls may wait for a rate limit at once. Beyond it a call raises `OpenDotaQueueLimitExceeded`. |
| `requestStrategy`     | `FreeTierFirstRequestStrategy`   | Which allowance each call is sent on.                                                                   |
| `json`                | `OpenDotaClient.DEFAULT_JSON`    | Serialiser the responses are deserialised with.                                                         |
| `logging`             | Logs nothing                     | Level and destination of the HTTP log.                                                                  |
| `timeouts`            | 10s connect, 30s read, 10s write | Timeouts for the underlying OkHttp client. `Duration.ZERO` disables one.                                |
| `baseUrl`             | `OpenDotaClient.BASE_URL`        | Base URL paths resolve against. Must end with `/`.                                                      |

A client owns an OkHttp connection pool, the threads that serve it and the scope queued calls wait on, so build one and
share it rather than creating one per call. `close()` releases all three.

The default serialiser ignores unknown keys, and a serialiser of your own should too: OpenDota adds fields over time,
and these models track one snapshot of its schema. `Json(from = OpenDotaClient.DEFAULT_JSON) { … }` keeps that while
changing the rest.

Building the service by hand stays an option — the base URL is the only thing it needs:

```kotlin
import io.github.yesitskev.kopendota.OpenDota
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private val json = Json { ignoreUnknownKeys = true }

val openDota: OpenDota =
    Retrofit.Builder()
        .baseUrl("https://api.opendota.com/api/")
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(OpenDota::class.java)
```

A service built that way is not rate limited: nothing holds a call back, counts it or picks the allowance it goes out
on, and no API key is sent unless you put one on the request yourself. Go through `OpenDotaClient` for any of that.

### Calling endpoints

```kotlin
// A single match, with replay-derived detail when the match has been parsed.
val match = openDota.getMatch(matchId = 7108753629L)
println("Radiant won: ${match.radiantWin}, duration: ${match.duration}s")

match.players.orEmpty().forEach { player ->
    println("${player.heroId}: ${player.kills}/${player.deaths}/${player.assists} @ ${player.goldPerMin} GPM")
}

// A player's profile and recent form.
val player = openDota.getPlayer(accountId = 88367253L)
val recent = openDota.getPlayerRecentMatches(accountId = 88367253L)

// Hero statistics across recent public matches.
val heroStats = openDota.getHeroStats()
```

Every model property is nullable, because OpenDota omits fields it has no data for. Unparsed matches lack most of the
replay-derived fields, so prefer `orEmpty()` and null-safe access over assertions.

### Filtering and pagination

The player endpoints share a large set of optional query parameters. Any left at their `null` default are omitted from
the request:

```kotlin
// Win/loss as Anti-Mage, in the last 30 days, ranked matches only.
val wl =
    openDota.getPlayerWinLoss(
        accountId = 88367253L,
        heroId = 1L,
        date = 30L,
        lobbyType = 7L,
    )
println("${wl.win}W / ${wl.lose}L")

// Page through full match history.
val page = openDota.getPlayerMatches(accountId = 88367253L, limit = 100L, offset = 200L)
```

### Rate limits, billing and an API key

Every call is held to what a `BillingSubscription` allows. Without one, calls go out anonymously on the free
allowance — 3,000 a day and 60 in any sliding minute. Give the subscription an
[API key](https://www.opendota.com/api-keys) and calls spend the free allowance first, then carry the key once the
free limits are reached:

```kotlin
val client = OpenDotaClient(
    billingSubscription = BillingSubscription(apiKey = System.getenv("OPEN_DOTA_API_KEY")),
)
```

| Allowance    | Per day   | Per sliding minute | Sent as              |
|--------------|-----------|--------------------|----------------------|
| Free         | 3,000     | 60                 | No `api_key`         |
| Subscription | Unlimited | 3,000              | `api_key=<your key>` |

Each call is counted against the day and against the second it was made in, which is what the sliding minute is summed
from. Only a response OpenDota charges for moves a count: a 404, 429 or 500 is not billed. A 429 says a limit has been
reached whatever the counts say, so a free-tier call answered with one is sent again on the subscription, and the
allowance that answered it backs off for the `Retry-After` it gave.

A call waits where neither allowance has room. `queueLimit` bounds how many may wait at once, and a call beyond it
raises `OpenDotaQueueLimitExceeded`; a limit of `0` or less queues without bound.

The key is kept out of the HTTP log — the `api_key` parameter is redacted at every log level — and a key that is
blank or `null` leaves the client on the free allowance alone. Note that
[`submitRequest`](src/main/kotlin/io/github/yesitskev/kopendota/OpenDota.kt), which queues a replay for parsing, counts
as ten calls against the rate limit.

#### Sharing one allowance across processes

The counts live in a `BillingSubscriptionCache`, which holds one count per key. The default keeps them in memory, so
each client gets the whole allowance; implement the interface over a shared store to hold one allowance across
several processes:

```kotlin
class RedisBillingSubscriptionCache(private val redis: RedisCommands<String, String>) : BillingSubscriptionCache {

    override suspend fun get(key: String): Long? = redis.get(key)?.toLongOrNull()

    override suspend fun put(key: String, count: Long, ttl: Duration) {
        redis.psetex(key, ttl.inWholeMilliseconds, count.toString())
    }

    // The sliding minute reads sixty keys, so answer it in one round trip.
    override suspend fun getAll(keys: Collection<String>): Map<String, Long?> =
        redis.mget(*keys.toTypedArray()).associate { it.key to it.getValueOrElse(null)?.toLongOrNull() }
}
```

#### Reading what has been spent

`statistics()` reports what the client is doing and what each allowance has spent of its ceiling:

```kotlin
val statistics = client.statistics()

println("${statistics.inFlightRequests} in flight, ${statistics.queuedRequests} waiting")
println("free day: ${statistics.freeTierDay.used}/${statistics.freeTierDay.limit}")
println("free minute: ${statistics.freeTierMinute.remaining} left")
println("subscription minute: ${statistics.subscriptionTierMinute.fractionUsed} of the ceiling")
```

Each usage carries `used`, the `limit` it is measured against, the `remaining` capacity and the `fractionUsed` of the
ceiling. An unlimited period — the subscription's day — reports a `null` limit, and a `null` remaining and fraction
with it, because there is no ceiling to measure against.

### Error handling

`OpenDotaQueueLimitExceeded` is raised where a call cannot be queued because `queueLimit` calls are already waiting
for a rate limit. The remaining `OpenDota*Exception` types are not implemented yet and nothing throws them, so every
other failure surfaces as a Retrofit or OkHttp error — `HttpException` for a non-2xx response, `IOException` for
transport failures:

```kotlin
val match =
    try {
        openDota.getMatch(matchId = 7108753629L)
    } catch (e: HttpException) {
        when (e.code()) {
            404 -> null // Unknown match.
            429 -> throw IllegalStateException("Rate limited", e)
            else -> throw e
        }
    } catch (e: OpenDotaQueueLimitExceeded) {
        throw IllegalStateException("${e.queueDepth} calls already waiting on a rate limit", e)
    }
```

### Resolving IDs

Responses identify heroes, items, abilities, patches, regions, game modes and lobby types by numeric ID. These are
resolved through the [dotaconstants](https://github.com/odota/dotaconstants) repository, which the API mirrors:

```kotlin
val heroes = openDota.getConstants(resource = "heroes")
val items = openDota.getConstants(resource = "items")
```

A few endpoints — `getExplorer`, `getHealth`, `getLive`, `getConstants` and others whose payloads are keyed
dynamically or vary per request — return `JsonElement` rather than a model, so you decode them to whatever shape you
need.

## API documentation

The full API reference is generated from the KDoc with [Dokka](https://kotlinlang.org/docs/dokka-introduction.html):

```bash
make server-docs        # -> docs/index.html
make server-docs-open   # generate, then open in a browser
make server-docs-clean  # remove the generated reference
```

Or directly: `./gradlew dokkaGenerate`.

The reference is written to [`docs/`](docs), so it can be served straight from the repository — GitHub Pages reads
that folder. Everything under it is generated and replaced on each run, so nothing hand-written belongs there: the
module and package overviews that feed the reference live in [`dokka/module.md`](dokka/module.md) instead.

## Disclaimer

**The majority of this client was generated using [Claude Code](https://claude.com/claude-code).** The Retrofit service
definition, the response models and their KDoc were derived mechanically from the OpenDota OpenAPI specification. That
generated code and documentation has not been exhaustively reviewed by hand, nor verified field by field against live
API responses — treat the field-level documentation in particular as a best-effort transcription of the specification,
and expect gaps where the specification itself is thin or out of date.

If there are missing fields, or incorrect fields, please create an issue or submit a pull request. The generated code 
is intended to be a starting point for a client, and may not be complete or fully accurate. The author of this client is not responsible for any inaccuracies or omissions

This project is not affiliated with, endorsed by or supported by OpenDota or its maintainers, or by Valve Corporation.
Dota 2 is a registered trademark of Valve Corporation. The OpenDota API is subject to
[OpenDota's own terms and limits](https://www.opendota.com/api-keys).

## Licence

[GNU General Public License v3.0](LICENSE.md) © Kevin Woodland

## Contributing

Bug reports and pull requests are welcome, and fixing a wrong or missing response field is the most useful thing you
can send. [CONTRIBUTION.md](CONTRIBUTION.md) has the rules — every change ships with tests, a model change is backed
by a live response rather than the specification, and AI assistance is disclosed.
