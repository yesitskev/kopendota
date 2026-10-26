# Contributing to kopendota

Thanks for looking. These rules follow directly from the [Disclaimer](README.md#disclaimer): most of this client was
generated from the OpenDota OpenAPI specification by [Claude Code](https://claude.com/claude-code), has not been
reviewed by hand field by field, and has not been verified against live API responses. That shapes what is useful to
contribute and what a contribution has to prove.

## What the project needs most

The generated code is a starting point, not a finished transcription. In order of value:

1. **Wrong or missing response fields.** The models track one snapshot of a specification that is thin in places and
   out of date in others. Fields the API really sends are missing from the models; fields the specification documents
   have the wrong type. Both are silent — see [Why a missing field is invisible](#why-a-missing-field-is-invisible).
2. **Field documentation.** The KDoc on the models is a best-effort transcription of the specification. Where the
   specification says nothing, the KDoc says nothing useful.
3. **Endpoints returning `JsonElement`.** A few payloads are keyed dynamically or vary per request and were left
   untyped. Some of them can be modelled properly.
4. **The unimplemented failure types.** `OpenDotaNotFoundException`, `OpenDotaRateLimitException` and
   `OpenDotaServerException` are empty placeholders that nothing throws.

Bug reports are welcome on their own. You do not have to fix a field to report it.

## Before you start

```bash
./gradlew build          # compile and test
make server-test         # all tests
make server-docs         # regenerate the API reference into docs/
```

You need **JDK 25** and the Gradle wrapper in the repository. Kotlin is 2.4.10.

## Rules

### 1. Always add tests

Every change ships with tests. No exceptions for "obvious" changes — a wrong field type looks obvious and still needs
a test, because the test is what stops it coming back.

- A changed or added **response field** needs a recorded fixture and an assertion on the mapped value.
- A changed **behaviour** needs a test that fails before your change and passes after. Say so in the pull request.
- A fixed **bug** needs a test that reproduces it.

Follow what is already there:

| Need | Use | Example |
| --- | --- | --- |
| Group tests by behaviour | `@Nested` inner classes, names in backticks | `RateLimiterTest` |
| The same assertion over several inputs | `@ParameterizedTest` with `@ValueSource` / `@CsvSource` | `BillingSubscriptionTest` |
| Suspend code | `runTest` from `kotlinx-coroutines-test` | everywhere |
| An HTTP response | `mockwebserver3.MockWebServer` | `OpenDotaClientBillingTest` |
| Anything time-dependent | the `MutableClock` test helper, never a real sleep | `RateLimiterTest` |
| A real API payload | a fixture under `src/test/resources/` | `GetMatchTest` |

Arrange, act and assert are separated by blank lines, not by comments. The test name in backticks is the
documentation.

Run just your test class while you work:

```bash
make server-test-class TEST_CLASS=io.github.yesitskev.kopendota.GetMatchTest
```

### 2. Prove a model change against a live response, not against the specification

The specification is the thing that was already wrong. A model change is reviewed against what the API actually sends,
so show that:

```bash
curl -s "https://api.opendota.com/api/matches/9028263007" | jq '.players[0].runes_log'
# [{"time": 0, "key": "5"}]   <- a string, though the published schema calls it an integer
```

In your pull request, include the endpoint and the ID you checked, and paste the fragment of JSON that shows the
field. If the field only appears on certain responses — a parsed match, a professional match, a player with a public
profile — say which, because that determines whether the existing fixtures cover it.

Where a field is not already covered, add a fixture:

1. Save the whole response under `src/test/resources/`, named for what it is.
2. Assert the mapped values you care about.
3. Add it to the strict-decoding test, which decodes each fixture with `Json { ignoreUnknownKeys = false }`.

### Why a missing field is invisible

`OpenDotaClient.DEFAULT_JSON` sets `ignoreUnknownKeys = true`, because OpenDota adds fields over time and a client
that threw on a new one would break in production. The cost is that a field missing from a model is dropped in
silence — no error, just `null` where data should be.

The strict-decoding test is the guard. It decodes every recorded fixture with `ignoreUnknownKeys = false`, so any key
in a fixture that no model maps fails the build. That is why a model change needs a fixture rather than only an
assertion: without one, the next missing field goes unnoticed again.

### 3. Disclose AI assistance

Much of this repository was machine-generated and the Disclaimer says so. Contributions follow the same standard: if
you used an AI tool for any part of a change, say so.

Either cite the model and the prompt in the pull request description:

> Assisted by Claude Opus 5 (`claude-opus-5`) via Claude Code.
>
> Prompt: "Add the `tower_damage_t` field to `MatchResponsePlayers` and cover it with a fixture test."

Or let your agent set its own attribution, so the trailer lands on the commit:

```
Assisted-by: Claude Opus 5 (claude-opus-5) via Claude Code
```

Either is fine; both is better. Name the model you actually used, with its version, not just the vendor.

This is a disclosure rule, not a restriction — AI-assisted contributions are welcome. It exists because a reviewer
treats generated field documentation differently from documentation someone read off a live response, and because the
provenance of this codebase is already public. Do not present generated output as hand-verified. If you did not check
a field against a real response, say that too.

### 4. Leave `docs/` alone

Everything under [`docs/`](docs) is Dokka output and the whole folder is replaced on each run, so an edit there is
lost on the next build. Change the KDoc in the source instead, or, for the module and package overviews,
[`dokka/module.md`](dokka/module.md). Regenerate with `make server-docs` and commit the result if your change affects
the reference.

### 5. Respect OpenDota's limits while you work

The API is [subject to OpenDota's own terms and limits](https://www.opendota.com/api-keys). When you gather evidence
for a change, request what you need and no more. Do not script a sweep of thousands of matches to find a field;
a handful of responses makes the point. `submitRequest`, which queues a replay for parsing, counts as ten calls.

The tests must never call the live API. Record a fixture and serve it from `MockWebServer`.

### 6. Match the surrounding code

There is no formatter wired up, so this is on you:

- 4-space indent, 120-column lines.
- KDoc on every public declaration: purpose, `@param`, `@return`. For a `val` constructor parameter use `@property`,
  not `@param` — Dokka renders property descriptions only from `@property`, and `@param` leaves the generated
  reference blank.
- Name a JSON key with `@SerialName`; the Kotlin property is camel-cased.
- Every response property is nullable with a `null` default, because the API omits what it has no data for.

### 7. No claim of affiliation

This project is not affiliated with, endorsed by or supported by OpenDota or its maintainers, or by Valve
Corporation. Dota 2 is a registered trademark of Valve Corporation. Do not add branding, logos or wording that
implies otherwise.

## Reporting a missing or wrong field

Open an issue with:

- The endpoint and the ID you called.
- The field name as the API spells it.
- What the API sent, and what the model did with it.
- Whether it appears on every response or only some.

## Pull request checklist

- [ ] Tests added, and `./gradlew build` passes.
- [ ] A model change is backed by a real response, quoted in the description.
- [ ] A new field is covered by a fixture and the strict-decoding test.
- [ ] KDoc added for anything public, using `@property` for `val` constructor parameters.
- [ ] AI assistance disclosed — model, version and prompt, or an agent attribution trailer.
- [ ] No hand edits under `docs/`.
- [ ] `make server-docs` re-run if the reference changes.

## Licence of contributions

This project is licensed under the [GNU General Public License v3.0 (GPL-3.0)](LICENSE.md). By contributing you agree
your contribution is licensed under the same terms.

Contributions are reviewed, but as the Disclaimer says, the code is offered as a starting point and the author is not
responsible for inaccuracies or omissions. Accepting a change is not a warranty that it is correct.
