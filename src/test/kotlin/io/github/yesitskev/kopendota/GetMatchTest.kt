package io.github.yesitskev.kopendota

import io.github.yesitskev.kopendota.model.MatchResponse
import io.github.yesitskev.kopendota.model.MatchResponsePlayers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class GetMatchTest {

    private lateinit var server: MockWebServer

    @BeforeEach
    fun startServer() {
        server = MockWebServer()
        server.start()
    }

    @AfterEach
    fun stopServer() {
        server.close()
    }

    private fun fixture(matchId: Long): String =
        checkNotNull(javaClass.getResourceAsStream("/matches/match-$matchId.json")) {
            "No recorded response for match $matchId"
        }
            .use { it.readBytes().decodeToString() }

    private suspend fun getMatch(matchId: Long, json: Json = OpenDotaClient.DEFAULT_JSON): MatchResponse {
        server.enqueue(MockResponse.Builder().code(200).body(fixture(matchId)).build())

        return OpenDotaClient(json = json, baseUrl = server.url("/").toString()).openDota.getMatch(matchId)
    }

    @Nested
    inner class GetMatchFieldCoverage {

        @ParameterizedTest
        @ValueSource(longs = [9028263007L, 9060239199L])
        fun `maps every field of a recorded match response`(matchId: Long) = runTest {
            val strictJson = Json { ignoreUnknownKeys = false }

            val match = getMatch(matchId = matchId, json = strictJson)

            assertEquals(matchId, match.matchId)
        }
    }

    @Nested
    inner class GetMatchParsedMatch {

        private suspend fun match(): MatchResponse = getMatch(matchId = 9028263007L)

        private suspend fun firstPlayer(): MatchResponsePlayers = checkNotNull(match().players).first()

        @Test
        fun `maps the match summary`() = runTest {
            val match = match()

            assertEquals(9028263007L, match.matchId)
            assertEquals(7583554260L, match.matchSeqNum)
            assertEquals(true, match.radiantWin)
            assertEquals(1541L, match.duration)
            assertEquals(1791065438L, match.startTime)
            assertEquals(60L, match.preGameDuration)
            assertEquals(50L, match.radiantScore)
            assertEquals(21L, match.direScore)
            assertEquals(0L, match.firstBloodTime)
            assertEquals(23L, match.gameMode)
            assertEquals(0L, match.lobbyType)
            assertEquals(10L, match.humanPlayers)
            assertEquals(213L, match.cluster)
            assertEquals(11L, match.region)
            assertEquals(60L, match.patch)
            assertEquals(1L, match.engine)
            assertEquals(22L, match.version)
            assertEquals(3L, match.flags)
            assertEquals(2046L, match.towerStatusRadiant)
            assertEquals(0L, match.towerStatusDire)
            assertEquals(63L, match.barracksStatusRadiant)
            assertEquals(0L, match.barracksStatusDire)
            assertEquals(510100518L, match.replaySalt)
            assertEquals("http://replay213.valve.net/570/9028263007_510100518.dem.bz2", match.replayUrl)
            assertEquals(786L, match.`throw`)
            assertEquals(39984L, match.loss)
            assertNull(match.metadata)
        }

        @Test
        fun `maps the sources opendota holds the match from`() = runTest {
            val odData = checkNotNull(match().odData)

            assertEquals(true, odData.hasApi)
            assertEquals(true, odData.hasGcdata)
            assertEquals(true, odData.hasParsed)
            assertEquals(false, odData.hasArchive)
        }

        @Test
        fun `maps the per minute advantage series`() = runTest {
            val match = match()

            assertEquals(26, match.radiantGoldAdv?.size)
            assertEquals(26, match.radiantXpAdv?.size)
            assertEquals(-786.0, match.radiantGoldAdv?.first())
        }

        @Test
        fun `maps the draft`() = runTest {
            val match = match()

            assertEquals(8, match.picksBans?.size)
            assertEquals(emptyList<Nothing>(), match.draftTimings)
            val firstPick = checkNotNull(match.picksBans).first()
            assertEquals(true, firstPick.isPick)
            assertEquals(14L, firstPick.heroId)
            assertEquals(0L, firstPick.team)
            assertEquals(0L, firstPick.order)
        }

        @Test
        fun `maps the chat log and its word counts`() = runTest {
            val match = match()

            assertEquals(104, match.chat?.size)
            val chatWheel = checkNotNull(match.chat).first()
            assertEquals(-51L, chatWheel.time)
            assertEquals("chatwheel", chatWheel.type)
            assertEquals("142948", chatWheel.key)
            assertEquals(0L, chatWheel.slot)
            assertEquals(0L, chatWheel.playerSlot)
            assertEquals(80, match.allWordCounts?.size)
            assertEquals(2L, match.allWordCounts?.get("why"))
            assertEquals(emptyMap<String, Long>(), match.myWordCounts)
        }

        @Test
        fun `maps the objectives taken`() = runTest {
            val objectives = checkNotNull(match().objectives)

            assertEquals(24, objectives.size)
            val firstBlood = objectives.first { it.type == "CHAT_MESSAGE_FIRSTBLOOD" }
            assertEquals(-9L, firstBlood.time)
            assertEquals("2", firstBlood.key)
            assertEquals(9L, firstBlood.slot)
            assertEquals(132L, firstBlood.playerSlot)
            assertEquals(2L, firstBlood.victimPlayerSlot)
            val buildingKill = objectives.first { it.type == "building_kill" }
            assertEquals(482L, buildingKill.time)
            assertEquals("npc_dota_hero_meepo", buildingKill.unit)
            assertEquals("npc_dota_badguys_tower1_bot", buildingKill.key)
            val roshanKill = objectives.first { it.type == "CHAT_MESSAGE_ROSHAN_KILL" }
            assertEquals(2L, roshanKill.team)
        }

        @Test
        fun `maps the team fights`() = runTest {
            val teamfights = checkNotNull(match().teamfights)

            assertEquals(11, teamfights.size)
            val first = teamfights.first()
            assertEquals(287L, first.start)
            assertEquals(322L, first.end)
            assertEquals(307L, first.lastDeath)
            assertEquals(3L, first.deaths)
            assertEquals(10, first.players?.size)
            val player = checkNotNull(first.players).first()
            assertEquals(0L, player.deaths)
            assertEquals(0L, player.buybacks)
            assertEquals(383L, player.damage)
            assertEquals(563L, player.healing)
            assertEquals(236L, player.goldDelta)
            assertEquals(1149L, player.xpDelta)
            assertEquals(909L, player.xpStart)
            assertEquals(2058L, player.xpEnd)
            assertEquals(1L, player.abilityUses?.get("tusk_snowball"))
            assertEquals(1L, player.itemUses?.get("enchanted_mango"))
            assertEquals(emptyMap<String, Long>(), player.killed)
            assertEquals(emptyMap<String, Map<String, Long>>(), player.deathsPos)
            assertEquals(mapOf("160" to 1L), checkNotNull(first.players)[4].deathsPos?.get("83"))
        }

        @Test
        fun `maps the summary of a player`() = runTest {
            val player = firstPlayer()

            assertNull(player.matchId)
            assertEquals(1221405523L, player.accountId)
            assertEquals("something2say", player.personaname)
            assertNull(player.name)
            assertNull(player.lastLogin)
            assertEquals(100L, player.heroId)
            assertEquals(0L, player.heroVariant)
            assertEquals(0L, player.playerSlot)
            assertEquals(0L, player.teamNumber)
            assertEquals(0L, player.teamSlot)
            assertEquals(true, player.isRadiant)
            assertEquals(6L, player.kills)
            assertEquals(6L, player.deaths)
            assertEquals(17L, player.assists)
            assertEquals(26L, player.level)
            assertEquals(15333L, player.netWorth)
            assertEquals(745L, player.goldPerMin)
            assertEquals(1619L, player.xpPerMin)
            assertEquals(43L, player.rankTier)
            assertEquals(5L, player.partySize)
            assertEquals(4L, player.positionEst)
            assertEquals(false, player.randomed)
            assertEquals(false, player.predVict)
            assertEquals(false, player.isSubscriber)
            assertEquals(false, player.isContributor)
            assertEquals(4045.67, player.computedMmr)
            assertEquals(3.29, player.kda)
            assertEquals(24.600079, player.stuns)
            assertEquals(231L, player.actionsPerMin)
            assertEquals(251L, player.lifeStateDead)
        }

        @Test
        fun `maps the objective and ward tallies of a player`() = runTest {
            val player = firstPlayer()

            assertEquals(0L, player.firstbloodClaimed)
            assertEquals(0L, player.towersKilled)
            assertEquals(0L, player.roshansKilled)
            assertEquals(3L, player.observersPlaced)
            assertEquals(0.46, player.teamfightParticipation)
            assertEquals(2L, player.purchaseWardObserver)
            assertEquals(4L, player.purchaseWardSentry)
            assertEquals(1L, player.purchaseTpscroll)
        }

        @Test
        fun `maps the end of game inventory of a player`() = runTest {
            val player = firstPlayer()

            assertEquals(675L, player.itemNeutral)
            assertEquals(1577L, player.itemNeutral2)
            assertEquals(0L, player.aghanimsScepter)
            assertEquals(1L, player.aghanimsShard)
            assertEquals(0L, player.moonshard)
        }

        @Test
        fun `maps the per minute series of a player`() = runTest {
            val player = firstPlayer()

            assertEquals(26, player.goldT?.size)
            assertEquals(26, player.xpT?.size)
            assertEquals(26, player.networthT?.size)
            assertEquals(26, player.campsStackedT?.size)
            assertEquals(26, player.heroDamageT?.size)
            assertEquals(26, player.heroHealingT?.size)
            assertEquals(13259L, player.networthT?.last())
            assertNull(player.towerDamageT)
        }

        @Test
        fun `maps the keyed tallies of a player`() = runTest {
            val player = firstPlayer()

            assertEquals(4426L, player.actions?.get("1"))
            assertEquals(mapOf("0" to 1291L, "1" to 12L, "2" to 239L), player.lifeState)
            assertEquals(mapOf("npc_dota_hero_tusk" to 9026L), player.healing)
            assertEquals(mapOf("5" to 1L), player.runes)
            assertEquals(mapOf("151" to 1L), player.obs?.get("113"))
            assertEquals(4L, player.lanePos?.get("70")?.get("161"))
            assertEquals(2L, player.abilityTargets?.get("seasonal_dark_carnival_pie")?.get("npc_dota_hero_tusk"))
        }

        @Test
        fun `maps the benchmarks of a player`() = runTest {
            val goldPerMin = checkNotNull(firstPlayer().benchmarks?.get("gold_per_min"))

            assertEquals(745.0, goldPerMin.raw)
            assertEquals(0.3486238532110092, goldPerMin.pct)
        }

        @Test
        fun `maps the highest damage instance of a player`() = runTest {
            val maxHeroHit = checkNotNull(firstPlayer().maxHeroHit)

            assertEquals(1221L, maxHeroHit.time)
            assertEquals("max_hero_hit", maxHeroHit.type)
            assertEquals("npc_dota_hero_tusk", maxHeroHit.unit)
            assertEquals("npc_dota_hero_kez", maxHeroHit.key)
            assertEquals(432L, maxHeroHit.value)
            assertEquals(0L, maxHeroHit.slot)
            assertEquals(0L, maxHeroHit.playerSlot)
            assertEquals(true, maxHeroHit.max)
            assertNull(maxHeroHit.inflictor)
        }

        @Test
        fun `maps the wards a player placed`() = runTest {
            val player = firstPlayer()

            val placed = checkNotNull(player.obsLog).first()
            assertEquals(-14L, placed.time)
            assertEquals("obs_log", placed.type)
            assertEquals("[113,151]", placed.key)
            assertEquals(112.9, placed.x)
            assertEquals(151.2, placed.y)
            assertEquals(130.0, placed.z)
            assertEquals(false, placed.entityleft)
            assertEquals(3720601L, placed.ehandle)
            assertNull(placed.attackername)

            val destroyed = checkNotNull(player.obsLeftLog).first()
            assertEquals(346L, destroyed.time)
            assertEquals("obs_left_log", destroyed.type)
            assertEquals("npc_dota_hero_tusk", destroyed.attackername)
            assertEquals(true, destroyed.entityleft)
            assertEquals(3720601L, destroyed.ehandle)

            assertTrue(player.senLog?.isNotEmpty() == true, "Expected the sentry log to be mapped")
            assertTrue(player.senLeftLog?.isNotEmpty() == true, "Expected the sentry departure log to be mapped")
        }

        @Test
        fun `maps the deaths of a player`() = runTest {
            val death = checkNotNull(firstPlayer().deathsLog).first()

            assertEquals(454L, death.time)
            assertEquals("npc_dota_hero_spirit_breaker", death.key)
            assertEquals(2288L, death.goldFed)
            assertEquals(true, death.goldFedShared)
            assertEquals(0L, death.goldLost)
            assertEquals(24L, death.timeDead)
        }

        @Test
        fun `maps the kills of a player`() = runTest {
            val players = checkNotNull(match().players)

            val kill = checkNotNull(players.first().killsLog).first()
            assertEquals(342L, kill.time)
            assertEquals("npc_dota_hero_sniper", kill.key)
            assertNull(kill.smoke)

            val smokeKill = players.flatMap { it.killsLog.orEmpty() }.first { it.smoke == true }
            assertEquals("npc_dota_hero_tusk", smokeKill.key)
        }

        @Test
        fun `maps the rune a player picked up as a string key`() = runTest {
            val rune = checkNotNull(firstPlayer().runesLog).single()

            assertEquals(0L, rune.time)
            assertEquals("5", rune.key)
        }

        @Test
        fun `maps the permanent buffs of a player`() = runTest {
            val buff = checkNotNull(firstPlayer().permanentBuffs).single()

            assertEquals(12L, buff.permanentBuff)
            assertEquals(0L, buff.stackCount)
            assertEquals(905L, buff.grantTime)
        }

        @Test
        fun `maps the purchases and neutral items of a player`() = runTest {
            val player = firstPlayer()

            val purchase = checkNotNull(player.purchaseLog).first()
            assertEquals(-59L, purchase.time)
            assertEquals("magic_wand", purchase.key)
            assertNull(purchase.charges)
            assertEquals(-59L, player.purchaseTime?.get("magic_wand"))
            assertEquals(-59L, player.firstPurchaseTime?.get("magic_wand"))
            assertEquals(1L, player.itemWin?.get("magic_wand"))
            assertEquals(1L, player.itemUsage?.get("magic_wand"))

            val neutral = checkNotNull(player.neutralItemHistory).first()
            assertEquals(427L, neutral.time)
            assertEquals("dormant_curio", neutral.itemNeutral)
            assertEquals("enhancement_quickened", neutral.itemNeutralEnhancement)
            assertEquals(emptyList<Nothing>(), player.neutralTokensLog)
        }

        @Test
        fun `maps the additional units a player controlled`() = runTest {
            val players = checkNotNull(match().players)

            val unit = checkNotNull(players.first { it.heroId == 82L }.additionalUnits).single()
            assertEquals("", unit.unitname)
            assertEquals(96L, unit.item0)
            assertEquals(1804L, unit.backpack1)
            assertEquals(0L, unit.itemNeutral)
            assertEquals(0L, unit.itemNeutral2)
            assertNull(players.first().additionalUnits)
        }
    }

    @Nested
    inner class GetMatchProMatch {

        private suspend fun match(): MatchResponse = getMatch(matchId = 9060239199L)

        @Test
        fun `maps the registered teams of a pro match`() = runTest {
            val match = match()

            assertEquals(10292107L, match.radiantTeamId)
            assertEquals(10291652L, match.direTeamId)
            assertEquals("Astra Invicta", match.radiantName)
            assertEquals("LEGION", match.direName)
            assertEquals(152168157L, match.radiantCaptain)
            assertEquals(262476000L, match.direCaptain)
            assertEquals(0L, match.radiantTeamComplete)
            assertEquals(0L, match.direTeamComplete)
            assertEquals(4096UL, match.radiantLogo)
            assertEquals(13029747532764537000UL, match.direLogo)

            val radiant = checkNotNull(match.radiantTeam)
            assertEquals(10292107L, radiant.teamId)
            assertEquals("Astra Invicta", radiant.name)
            assertEquals("AI", radiant.tag)
            assertNull(radiant.logoUrl)

            val dire = checkNotNull(match.direTeam)
            assertEquals("LEGION", dire.tag)
            assertTrue(dire.logoUrl?.startsWith("https://") == true, "Expected a logo url, got ${dire.logoUrl}")
        }

        @Test
        fun `maps the league of a pro match`() = runTest {
            val league = checkNotNull(match().league)

            assertEquals(20395L, league.leagueid)
            assertEquals("PARI UNIVERSE by FISSURE", league.name)
            assertEquals("professional", league.tier)
            assertNull(league.ticket)
            assertNull(league.banner)
        }

        @Test
        fun `maps the series and the margin of a pro match`() = runTest {
            val match = match()

            assertEquals(1159090L, match.seriesId)
            assertEquals(1L, match.seriesType)
            assertEquals(18931L, match.stomp)
            assertEquals(7522L, match.comeback)
            assertEquals(90L, match.preGameDuration)
            assertNull(match.`throw`)
            assertNull(match.loss)
        }

        @Test
        fun `maps an objective that carries a value and a killer`() = runTest {
            val courierLost = checkNotNull(match().objectives).first { it.type == "CHAT_MESSAGE_COURIER_LOST" }

            assertEquals(85L, courierLost.time)
            assertEquals(3L, courierLost.team)
            assertEquals(25L, courierLost.value)
            assertEquals(1L, courierLost.killer)
        }

        @Test
        fun `maps the pro names of the players`() = runTest {
            val player = checkNotNull(match().players).first()

            assertEquals("Nightfall", player.name)
            assertEquals("2017-08-02T13:01:28.316Z", player.lastLogin)
        }

        @Test
        fun `maps the full captains mode draft`() = runTest {
            val picksBans = checkNotNull(match().picksBans)

            assertEquals(24, picksBans.size)
            assertEquals(false, picksBans.first().isPick)
            assertEquals(91L, picksBans.first().heroId)
        }
    }

    @Nested
    inner class GetMatchLenientKeys {

        @ParameterizedTest
        @ValueSource(strings = ["\"5\"", "5"])
        fun `maps a rune key the api sends as either a string or a number`(key: String) = runTest {
            server.enqueue(
                MockResponse.Builder()
                    .code(200)
                    .body("""{"match_id": 1, "players": [{"runes_log": [{"time": 0, "key": $key}]}]}""")
                    .build(),
            )
            val client = OpenDotaClient(baseUrl = server.url("/").toString())

            val match = client.openDota.getMatch(matchId = 1L)

            assertEquals("5", match.players?.single()?.runesLog?.single()?.key)
        }

        @ParameterizedTest
        @ValueSource(strings = ["\"142948\"", "142948"])
        fun `maps a chat key the api sends as either a string or a number`(key: String) = runTest {
            server.enqueue(
                MockResponse.Builder()
                    .code(200)
                    .body("""{"match_id": 1, "chat": [{"time": 0, "type": "chatwheel", "key": $key}]}""")
                    .build(),
            )
            val client = OpenDotaClient(baseUrl = server.url("/").toString())

            val match = client.openDota.getMatch(matchId = 1L)

            assertEquals("142948", match.chat?.single()?.key)
        }

        @ParameterizedTest
        @ValueSource(strings = ["\"2\"", "2"])
        fun `maps an objective key the api sends as either a string or a number`(key: String) = runTest {
            server.enqueue(
                MockResponse.Builder()
                    .code(200)
                    .body(
                        """
                        {"match_id": 1, "objectives": [{"time": -9, "type": "CHAT_MESSAGE_FIRSTBLOOD", "key": $key}]}
                        """
                            .trimIndent(),
                    )
                    .build(),
            )
            val client = OpenDotaClient(baseUrl = server.url("/").toString())

            val match = client.openDota.getMatch(matchId = 1L)

            assertEquals("2", match.objectives?.single()?.key)
        }

        @Test
        fun `leaves a null key null`() = runTest {
            server.enqueue(
                MockResponse.Builder()
                    .code(200)
                    .body("""{"match_id": 1, "players": [{"runes_log": [{"time": 0, "key": null}]}]}""")
                    .build(),
            )
            val client = OpenDotaClient(baseUrl = server.url("/").toString())

            val match = client.openDota.getMatch(matchId = 1L)

            assertNull(match.players?.single()?.runesLog?.single()?.key)
        }

        @Test
        fun `leaves an absent key null`() = runTest {
            server.enqueue(
                MockResponse.Builder()
                    .code(200)
                    .body("""{"match_id": 1, "players": [{"runes_log": [{"time": 0}]}]}""")
                    .build(),
            )
            val client = OpenDotaClient(baseUrl = server.url("/").toString())

            val match = client.openDota.getMatch(matchId = 1L)

            assertFalse(match.players.isNullOrEmpty(), "Expected the player to be mapped")
            assertNull(match.players?.single()?.runesLog?.single()?.key)
        }
    }
}
