package io.github.yesitskev.kopendota.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Returned by the `GET /matches/{match_id}` endpoint.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getMatch
 */
@Serializable
data class MatchResponse(
    /** The ID number of the match assigned by Valve. */
    @SerialName("match_id") val matchId: Long? = null,
    /**
     * Bitmask. An integer that represents a binary of which barracks are still standing. 63 would mean all barracks
     * still stand at the end of the game.
     */
    @SerialName("barracks_status_dire") val barracksStatusDire: Long? = null,
    /**
     * Bitmask. An integer that represents a binary of which barracks are still standing. 63 would mean all barracks
     * still stand at the end of the game.
     */
    @SerialName("barracks_status_radiant") val barracksStatusRadiant: Long? = null,
    /** Array containing information on the chat of the game. */
    @SerialName("chat") val chat: List<MatchResponseChat>? = null,
    /** Server cluster the match was played on. */
    @SerialName("cluster") val cluster: Long? = null,
    /** Cosmetic items appearing in the match, keyed by item ID. */
    @SerialName("cosmetics") val cosmetics: Map<String, Long>? = null,
    /** Number of kills the Dire team had when the match ended. */
    @SerialName("dire_score") val direScore: Long? = null,
    /** Per pick draft timings, for Captains Mode matches. */
    @SerialName("draft_timings") val draftTimings: List<MatchResponseDraftTimings>? = null,
    /** Duration of the game in seconds. */
    @SerialName("duration") val duration: Long? = null,
    /** Replay engine version the match was played on. */
    @SerialName("engine") val engine: Long? = null,
    /** Time in seconds at which first blood occurred. */
    @SerialName("first_blood_time") val firstBloodTime: Long? = null,
    /**
     * Integer corresponding to game mode played. List of constants can be found here:
     * https://github.com/odota/dotaconstants/blob/master/json/game_mode.json.
     */
    @SerialName("game_mode") val gameMode: Long? = null,
    /** Number of human players in the game. */
    @SerialName("human_players") val humanPlayers: Long? = null,
    /** ID of the league the match belongs to, or `0` outside league play. */
    @SerialName("leagueid") val leagueid: Long? = null,
    /**
     * Integer corresponding to lobby type of match. List of constants can be found here:
     * https://github.com/odota/dotaconstants/blob/master/json/lobby_type.json.
     */
    @SerialName("lobby_type") val lobbyType: Long? = null,
    /** Sequence number ordering the match within Valve's match history. */
    @SerialName("match_seq_num") val matchSeqNum: Long? = null,
    /** Number of negative votes the replay received in the in-game client. */
    @SerialName("negative_votes") val negativeVotes: Long? = null,
    /** Objective events such as tower, barracks and Roshan kills. */
    @SerialName("objectives") val objectives: List<MatchResponseObjectives>? = null,
    /**
     * Array containing information on the draft. Each item contains a boolean relating to whether the choice is a pick
     * or a ban, the hero ID, the team the picked or banned it, and the order.
     */
    @SerialName("picks_bans") val picksBans: List<MatchResponsePicksBans>? = null,
    /** Number of positive votes the replay received in the in-game client. */
    @SerialName("positive_votes") val positiveVotes: Long? = null,
    /**
     * Array of the Radiant gold advantage at each minute in the game. A negative number means that Radiant is behind,
     * and thus it is their gold disadvantage.
     */
    @SerialName("radiant_gold_adv") val radiantGoldAdv: List<Double>? = null,
    /** Number of kills the Radiant team had when the match ended. */
    @SerialName("radiant_score") val radiantScore: Long? = null,
    /** Boolean indicating whether Radiant won the match. */
    @SerialName("radiant_win") val radiantWin: Boolean? = null,
    /**
     * Array of the Radiant experience advantage at each minute in the game. A negative number means that Radiant is
     * behind, and thus it is their experience disadvantage.
     */
    @SerialName("radiant_xp_adv") val radiantXpAdv: List<Double>? = null,
    /** The Unix timestamp at which the game started. */
    @SerialName("start_time") val startTime: Long? = null,
    /** Team fight summaries extracted from the replay. */
    @SerialName("teamfights") val teamfights: List<MatchResponseTeamfights>? = null,
    /** Bitmask. An integer that represents a binary of which Dire towers are still standing. */
    @SerialName("tower_status_dire") val towerStatusDire: Long? = null,
    /** Bitmask. An integer that represents a binary of which Radiant towers are still standing. */
    @SerialName("tower_status_radiant") val towerStatusRadiant: Long? = null,
    /** Parse version, used internally by OpenDota. */
    @SerialName("version") val version: Long? = null,
    /** Salt required to download the match replay. */
    @SerialName("replay_salt") val replaySalt: Long? = null,
    /** ID of the series the match belongs to. */
    @SerialName("series_id") val seriesId: Long? = null,
    /** Type of the series the match belongs to. */
    @SerialName("series_type") val seriesType: Long? = null,
    /** Radiant team details, for matches played by a registered team. */
    @SerialName("radiant_team") val radiantTeam: MatchResponseTeam? = null,
    /** Dire team details, for matches played by a registered team. */
    @SerialName("dire_team") val direTeam: MatchResponseTeam? = null,
    /** League details, for league matches. */
    @SerialName("league") val league: MatchResponseLeague? = null,
    /** The Radiant's team_id, for matches played by a registered team. */
    @SerialName("radiant_team_id") val radiantTeamId: Long? = null,
    /** The Dire's team_id, for matches played by a registered team. */
    @SerialName("dire_team_id") val direTeamId: Long? = null,
    /** The Radiant's team name, for matches played by a registered team. */
    @SerialName("radiant_name") val radiantName: String? = null,
    /** The Dire's team name, for matches played by a registered team. */
    @SerialName("dire_name") val direName: String? = null,
    /** Account ID of the player who drafted for Radiant, in Captains Mode matches. */
    @SerialName("radiant_captain") val radiantCaptain: Long? = null,
    /** Account ID of the player who drafted for Dire, in Captains Mode matches. */
    @SerialName("dire_captain") val direCaptain: Long? = null,
    /** Steam UGC ID of the Radiant team logo. Unsigned, as the IDs run past [Long.MAX_VALUE]. */
    @SerialName("radiant_logo") val radiantLogo: ULong? = null,
    /** Steam UGC ID of the Dire team logo. Unsigned, as the IDs run past [Long.MAX_VALUE]. */
    @SerialName("dire_logo") val direLogo: ULong? = null,
    /** Whether the whole registered Radiant team played the match, as a binary integer. */
    @SerialName("radiant_team_complete") val radiantTeamComplete: Long? = null,
    /** Whether the whole registered Dire team played the match, as a binary integer. */
    @SerialName("dire_team_complete") val direTeamComplete: Long? = null,
    /** Skill bracket assigned by Valve (Normal, High, Very High). */
    @SerialName("skill") val skill: Long? = null,
    /** Array of information on individual players. */
    @SerialName("players") val players: List<MatchResponsePlayers>? = null,
    /** Patch ID, from dotaconstants. */
    @SerialName("patch") val patch: Long? = null,
    /** Integer corresponding to the region the game was played on. */
    @SerialName("region") val region: Long? = null,
    /** How often each word was said in all chat over the match, keyed by word. */
    @SerialName("all_word_counts") val allWordCounts: Map<String, Long>? = null,
    /** How often each word was said in all chat by the requesting player, keyed by word. */
    @SerialName("my_word_counts") val myWordCounts: Map<String, Long>? = null,
    /** Maximum gold advantage of the player's team if they lost the match. */
    @SerialName("throw") val `throw`: Long? = null,
    /** Maximum gold disadvantage of the player's team if they won the match. */
    @SerialName("comeback") val comeback: Long? = null,
    /** Maximum gold disadvantage of the player's team if they lost the match. */
    @SerialName("loss") val loss: Long? = null,
    /** Maximum gold advantage of the player's team if they won the match. */
    @SerialName("win") val win: Long? = null,
    /** URL of the match replay. */
    @SerialName("replay_url") val replayUrl: String? = null,
    /**
     * Array containing information about pauses during the game. Each item contains the time and duration of the pause.
     */
    @SerialName("pauses") val pauses: List<MatchResponsePauses>? = null,
    /** Length of the pre-game horn phase in seconds, before the match clock starts. */
    @SerialName("pre_game_duration") val preGameDuration: Long? = null,
    /** Bitmask of match flags Valve set on the match. */
    @SerialName("flags") val flags: Long? = null,
    /**
     * Maximum gold advantage of the player's team if they won the match, which the API sends here rather than in
     * [win].
     */
    @SerialName("stomp") val stomp: Long? = null,
    /** Which sources OpenDota holds data for this match from. */
    @SerialName("od_data") val odData: MatchResponseOdData? = null,
    /**
     * Opaque match metadata OpenDota attaches to the match, `null` for every match seen so far.
     *
     * Left as a raw [JsonElement] because the API documents neither the shape nor the keys.
     */
    @SerialName("metadata") val metadata: JsonElement? = null,
)

/**
 * Array containing information on the chat of the game.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponseChat(
    /** Time in seconds at which the message was said. */
    @SerialName("time") val time: Long? = null,
    /** Name of the player who sent the message. */
    @SerialName("unit") val unit: String? = null,
    /**
     * The message the player sent, or the ID of the chat wheel line they played when [type] is `chatwheel`.
     *
     * Decoded from a JSON string or number alike, since the API sends either.
     */
    @SerialName("key") @Serializable(with = LenientStringSerializer::class) val key: String? = null,
    /** Kind of message, such as `chat` for a typed message or `chatwheel` for a chat wheel line. */
    @SerialName("type") val type: String? = null,
    /** Player slot index of the speaker. */
    @SerialName("slot") val slot: Long? = null,
    /** Which slot the player is in. 0-127 are Radiant, 128-255 are Dire. */
    @SerialName("player_slot") val playerSlot: Long? = null,
)

/**
 * Draft_timings.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponseDraftTimings(
    /** Position of this pick or ban in the draft. */
    @SerialName("order") val order: Long? = null,
    /** Whether this entry is a pick rather than a ban. */
    @SerialName("pick") val pick: Boolean? = null,
    /** Team making the pick or ban. */
    @SerialName("active_team") val activeTeam: Long? = null,
    /** The ID value of the hero played. */
    @SerialName("hero_id") val heroId: Long? = null,
    /** Which slot the player is in. 0-127 are Radiant, 128-255 are Dire. */
    @SerialName("player_slot") val playerSlot: Long? = null,
    /** Reserve time remaining for the team, in seconds. */
    @SerialName("extra_time") val extraTime: Long? = null,
    /** Time taken over this pick or ban, in seconds. */
    @SerialName("total_time_taken") val totalTimeTaken: Long? = null,
)

/**
 * Array containing information on the draft. Each item contains a boolean relating to whether the choice is a pick or a
 * ban, the hero ID, the team the picked or banned it, and the order.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponsePicksBans(
    /** Boolean indicating whether the choice is a pick or a ban. */
    @SerialName("is_pick") val isPick: Boolean? = null,
    /** The ID value of the hero played. */
    @SerialName("hero_id") val heroId: Long? = null,
    /** The team that picked or banned the hero. */
    @SerialName("team") val team: Long? = null,
    /** The order of the pick or ban. */
    @SerialName("order") val order: Long? = null,
)

/**
 * Array of information on individual players.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponsePlayers(
    /** The ID number of the match assigned by Valve. */
    @SerialName("match_id") val matchId: Long? = null,
    /** Which slot the player is in. 0-127 are Radiant, 128-255 are Dire. */
    @SerialName("player_slot") val playerSlot: Long? = null,
    /** An array describing how abilities were upgraded. */
    @SerialName("ability_upgrades_arr") val abilityUpgradesArr: List<Long>? = null,
    /** Object containing information on how many times the played used their abilities. */
    @SerialName("ability_uses") val abilityUses: Map<String, Long>? = null,
    /** Object containing information on who the player used their abilities on. */
    @SerialName("ability_targets") val abilityTargets: Map<String, Map<String, Long>>? = null,
    /** Object containing information on how and how much damage the player dealt to other heroes. */
    @SerialName("damage_targets") val damageTargets: Map<String, Map<String, Long>>? = null,
    /** The player account ID. */
    @SerialName("account_id") val accountId: Long? = null,
    /** Object containing information on how many and what type of actions the player issued to their hero. */
    @SerialName("actions") val actions: Map<String, Long>? = null,
    /** Object containing information on additional units the player had under their control. */
    @SerialName("additional_units") val additionalUnits: List<MatchResponsePlayersAdditionalUnits>? = null,
    /** Number of assists the player had. */
    @SerialName("assists") val assists: Long? = null,
    /** Item in backpack slot 0. */
    @SerialName("backpack_0") val backpack0: Long? = null,
    /** Item in backpack slot 1. */
    @SerialName("backpack_1") val backpack1: Long? = null,
    /** Item in backpack slot 2. */
    @SerialName("backpack_2") val backpack2: Long? = null,
    /** Array containing information about buybacks. */
    @SerialName("buyback_log") val buybackLog: List<MatchResponsePlayersBuybackLog>? = null,
    /** Number of camps stacked. */
    @SerialName("camps_stacked") val campsStacked: Long? = null,
    /** Array containing information about the player's disconnections and reconnections. */
    @SerialName("connection_log") val connectionLog: List<MatchResponsePlayersConnectionLog>? = null,
    /** Number of creeps stacked. */
    @SerialName("creeps_stacked") val creepsStacked: Long? = null,
    /** Object containing information about damage dealt by the player to different units. */
    @SerialName("damage") val damage: Map<String, Long>? = null,
    /** Object containing information about about the sources of this player's damage to heroes. */
    @SerialName("damage_inflictor") val damageInflictor: Map<String, Long>? = null,
    /** Object containing information about the sources of damage received by this player from heroes. */
    @SerialName("damage_inflictor_received") val damageInflictorReceived: Map<String, Long>? = null,
    /** Object containing information about from whom the player took damage. */
    @SerialName("damage_taken") val damageTaken: Map<String, Long>? = null,
    /** Number of deaths. */
    @SerialName("deaths") val deaths: Long? = null,
    /** Array containing information on every death the player suffered. */
    @SerialName("deaths_log") val deathsLog: List<MatchResponsePlayersDeathsLog>? = null,
    /** Number of denies. */
    @SerialName("denies") val denies: Long? = null,
    /** Array containing number of denies at different times of the match. */
    @SerialName("dn_t") val dnT: List<Long>? = null,
    /** Gold at the end of the game. */
    @SerialName("gold") val gold: Long? = null,
    /** Gold Per Minute obtained by this player. */
    @SerialName("gold_per_min") val goldPerMin: Long? = null,
    /** Object containing information on how the player gainined gold over the course of the match. */
    @SerialName("gold_reasons") val goldReasons: Map<String, Long>? = null,
    /** How much gold the player spent. */
    @SerialName("gold_spent") val goldSpent: Long? = null,
    /** Array containing total gold at different times of the match. */
    @SerialName("gold_t") val goldT: List<Long>? = null,
    /** Hero Damage Dealt. */
    @SerialName("hero_damage") val heroDamage: Long? = null,
    /** Hero Healing Done. */
    @SerialName("hero_healing") val heroHealing: Long? = null,
    /** How much the player healed each unit, keyed by unit name. */
    @SerialName("healing") val healing: Map<String, Long>? = null,
    /**
     * Object containing information on how many ticks of damages the hero inflicted with different spells and damage
     * inflictors.
     */
    @SerialName("hero_hits") val heroHits: Map<String, Long>? = null,
    /** The ID value of the hero played. */
    @SerialName("hero_id") val heroId: Long? = null,
    /** Item in the player's first slot. */
    @SerialName("item_0") val item0: Long? = null,
    /** Item in the player's second slot. */
    @SerialName("item_1") val item1: Long? = null,
    /** Item in the player's third slot. */
    @SerialName("item_2") val item2: Long? = null,
    /** Item in the player's fourth slot. */
    @SerialName("item_3") val item3: Long? = null,
    /** Item in the player's fifth slot. */
    @SerialName("item_4") val item4: Long? = null,
    /** Item in the player's sixth slot. */
    @SerialName("item_5") val item5: Long? = null,
    /** Item in the player's neutral slot. */
    @SerialName("item_neutral") val itemNeutral: Long? = null,
    /** Enhancement in the player's neutral slot. */
    @SerialName("item_neutral2") val itemNeutral2: Long? = null,
    /** Whether the player held an Aghanim's Scepter buff at the end of the game, as a binary integer. */
    @SerialName("aghanims_scepter") val aghanimsScepter: Long? = null,
    /** Whether the player held an Aghanim's Shard at the end of the game, as a binary integer. */
    @SerialName("aghanims_shard") val aghanimsShard: Long? = null,
    /** Whether the player consumed a Moon Shard, as a binary integer. */
    @SerialName("moonshard") val moonshard: Long? = null,
    /** Object containing information about how many times a player used items. */
    @SerialName("item_uses") val itemUses: Map<String, Long>? = null,
    /** Object containing information about the player's killstreaks. */
    @SerialName("kill_streaks") val killStreaks: Map<String, Long>? = null,
    /** Object containing information about what units the player killed. */
    @SerialName("killed") val killed: Map<String, Long>? = null,
    /** Object containing information about who killed the player. */
    @SerialName("killed_by") val killedBy: Map<String, Long>? = null,
    /** Number of kills. */
    @SerialName("kills") val kills: Long? = null,
    /** Array containing information on which hero the player killed at what time. */
    @SerialName("kills_log") val killsLog: List<MatchResponsePlayersKillsLog>? = null,
    /** Object containing information on lane position. */
    @SerialName("lane_pos") val lanePos: Map<String, Map<String, Long>>? = null,
    /** Number of last hits. */
    @SerialName("last_hits") val lastHits: Long? = null,
    /** Integer describing whether or not the player left the game. 0: didn't leave. 1: left safely. 2+: Abandoned. */
    @SerialName("leaver_status") val leaverStatus: Long? = null,
    /** Level at the end of the game. */
    @SerialName("level") val level: Long? = null,
    /** Array describing last hits at each minute in the game. */
    @SerialName("lh_t") val lhT: List<Long>? = null,
    /** Time spent in each life state, keyed by life state. */
    @SerialName("life_state") val lifeState: Map<String, Long>? = null,
    /** Object with information on the highest damage instance the player inflicted. */
    @SerialName("max_hero_hit") val maxHeroHit: MatchResponsePlayersMaxHeroHit? = null,
    /** Object with information on the number of the number of multikills the player had. */
    @SerialName("multi_kills") val multiKills: Map<String, Long>? = null,
    /**
     * Object with information on where the player placed observer wards. The location takes the form (outer number,
     * inner number) and are from ~64-192.
     */
    @SerialName("obs") val obs: Map<String, Map<String, Long>>? = null,
    /** Log of observer wards that expired or were destroyed. */
    @SerialName("obs_left_log") val obsLeftLog: List<MatchResponsePlayersWardLog>? = null,
    /** Object containing information on when and where the player placed observer wards. */
    @SerialName("obs_log") val obsLog: List<MatchResponsePlayersWardLog>? = null,
    /** Total number of observer wards placed. */
    @SerialName("obs_placed") val obsPlaced: Long? = null,
    /** Identifier grouping players who queued together. */
    @SerialName("party_id") val partyId: Long? = null,
    /** How many players queued together in this player's party. */
    @SerialName("party_size") val partySize: Long? = null,
    /** Which team the player was on. 0 is Radiant and 1 is Dire. */
    @SerialName("team_number") val teamNumber: Long? = null,
    /** The player's slot within their own team, from 0 to 4. */
    @SerialName("team_slot") val teamSlot: Long? = null,
    /** Whether the player randomed their hero. */
    @SerialName("randomed") val randomed: Boolean? = null,
    /** Whether the player predicted victory before the match began. */
    @SerialName("pred_vict") val predVict: Boolean? = null,
    /** MMR OpenDota estimated for the player from the match, where it could estimate one. */
    @SerialName("computed_mmr") val computedMmr: Double? = null,
    /** Whether the player subscribes to OpenDota Plus. */
    @SerialName("is_subscriber") val isSubscriber: Boolean? = null,
    /** Whether the player contributes to OpenDota. */
    @SerialName("is_contributor") val isContributor: Boolean? = null,
    /**
     * Array describing permanent buffs the player had at the end of the game. List of constants can be found here:
     * https://github.com/odota/dotaconstants/blob/master/json/permanent_buffs.json.
     */
    @SerialName("permanent_buffs") val permanentBuffs: List<MatchResponsePlayersPermanentBuffs>? = null,
    /** 1-indexed facet, see https://github.com/odota/dotaconstants/blob/master/build/hero_abilities.json. */
    @SerialName("hero_variant") val heroVariant: Long? = null,
    /** Total number of pings. */
    @SerialName("pings") val pings: Long? = null,
    /** Object containing information on the items the player purchased. */
    @SerialName("purchase") val purchase: Map<String, Long>? = null,
    /** Object containing information on when items were purchased. */
    @SerialName("purchase_log") val purchaseLog: List<MatchResponsePlayersPurchaseLog>? = null,
    /** Number of runes picked up. */
    @SerialName("rune_pickups") val runePickups: Long? = null,
    /** Object with information about which runes the player picked up. */
    @SerialName("runes") val runes: Map<String, Long>? = null,
    /** Array with information on when runes were picked up. */
    @SerialName("runes_log") val runesLog: List<MatchResponsePlayersRunesLog>? = null,
    /**
     * Object with information on where sentries were placed. The location takes the form (outer number, inner number)
     * and are from ~64-192.
     */
    @SerialName("sen") val sen: Map<String, Map<String, Long>>? = null,
    /** Array containing information on when and where the player placed sentries. */
    @SerialName("sen_left_log") val senLeftLog: List<MatchResponsePlayersWardLog>? = null,
    /** Array with information on when and where sentries were placed by the player. */
    @SerialName("sen_log") val senLog: List<MatchResponsePlayersWardLog>? = null,
    /** How many sentries were placed by the player. */
    @SerialName("sen_placed") val senPlaced: Long? = null,
    /** Total stun duration of all stuns by the player. */
    @SerialName("stuns") val stuns: Double? = null,
    /** Time in seconds corresponding to the time of entries of other arrays in the match. */
    @SerialName("times") val times: List<Long>? = null,
    /** Total tower damage done by the player. */
    @SerialName("tower_damage") val towerDamage: Long? = null,
    /** Experience Per Minute obtained by the player. */
    @SerialName("xp_per_min") val xpPerMin: Long? = null,
    /** Object containing information on the sources of this player's experience. */
    @SerialName("xp_reasons") val xpReasons: Map<String, Long>? = null,
    /** Experience at each minute of the game. */
    @SerialName("xp_t") val xpT: List<Long>? = null,
    /** Net worth at each minute of the game. */
    @SerialName("networth_t") val networthT: List<Long>? = null,
    /** Running total of camps stacked at each minute of the game. */
    @SerialName("camps_stacked_t") val campsStackedT: List<Long>? = null,
    /** Running total of hero damage dealt at each minute of the game. */
    @SerialName("hero_damage_t") val heroDamageT: List<Long>? = null,
    /** Running total of hero healing done at each minute of the game. */
    @SerialName("hero_healing_t") val heroHealingT: List<Long>? = null,
    /** Running total of tower damage dealt at each minute of the game. Absent from older parsed matches. */
    @SerialName("tower_damage_t") val towerDamageT: List<Long>? = null,
    /** Player's Steam name. */
    @SerialName("personaname") val personaname: String? = null,
    /** The player's professional name, when they are a pro player. */
    @SerialName("name") val name: String? = null,
    /** Time of player's last login. */
    @SerialName("last_login") val lastLogin: String? = null,
    /** Boolean indicating whether Radiant won the match. */
    @SerialName("radiant_win") val radiantWin: Boolean? = null,
    /** The Unix timestamp at which the game started. */
    @SerialName("start_time") val startTime: Long? = null,
    /** Duration of the game in seconds. */
    @SerialName("duration") val duration: Long? = null,
    /** Server cluster the match was played on. */
    @SerialName("cluster") val cluster: Long? = null,
    /**
     * Integer corresponding to lobby type of match. List of constants can be found here:
     * https://github.com/odota/dotaconstants/blob/master/json/lobby_type.json.
     */
    @SerialName("lobby_type") val lobbyType: Long? = null,
    /**
     * Integer corresponding to game mode played. List of constants can be found here:
     * https://github.com/odota/dotaconstants/blob/master/json/game_mode.json.
     */
    @SerialName("game_mode") val gameMode: Long? = null,
    /** Patch ID, from dotaconstants. */
    @SerialName("patch") val patch: Long? = null,
    /** Integer corresponding to the region the game was played on. */
    @SerialName("region") val region: Long? = null,
    /** Boolean for whether or not the player is on Radiant. */
    @SerialName("isRadiant") val isRadiant: Boolean? = null,
    /** Binary integer representing whether or not the player won. */
    @SerialName("win") val win: Long? = null,
    /** Binary integer representing whether or not the player lost. */
    @SerialName("lose") val lose: Long? = null,
    /** Total gold at the end of the game. */
    @SerialName("total_gold") val totalGold: Long? = null,
    /** Net worth at the end of the game, the gold held plus the value of everything carried. */
    @SerialName("net_worth") val netWorth: Long? = null,
    /** Total experience at the end of the game. */
    @SerialName("total_xp") val totalXp: Long? = null,
    /** Number of kills per minute. */
    @SerialName("kills_per_min") val killsPerMin: Double? = null,
    /** Ratio of kills and assists to deaths. */
    @SerialName("kda") val kda: Double? = null,
    /** Number of abandons recorded for the player in the match. */
    @SerialName("abandons") val abandons: Long? = null,
    /** Total number of neutral creeps killed. */
    @SerialName("neutral_kills") val neutralKills: Long? = null,
    /** Total number of tower kills the player had. */
    @SerialName("tower_kills") val towerKills: Long? = null,
    /** Number of towers the player landed the killing blow on, counted from the replay. */
    @SerialName("towers_killed") val towersKilled: Long? = null,
    /** Number of Roshan kills the player landed the killing blow on, counted from the replay. */
    @SerialName("roshans_killed") val roshansKilled: Long? = null,
    /** Number of observer wards the player placed, counted from the replay. */
    @SerialName("observers_placed") val observersPlaced: Long? = null,
    /** Whether the player claimed first blood, as a binary integer. */
    @SerialName("firstblood_claimed") val firstbloodClaimed: Long? = null,
    /** Share of the team's teamfights the player took part in, from 0.0 to 1.0. */
    @SerialName("teamfight_participation") val teamfightParticipation: Double? = null,
    /** Total number of courier kills the player had. */
    @SerialName("courier_kills") val courierKills: Long? = null,
    /** Total number of lane creeps killed by the player. */
    @SerialName("lane_kills") val laneKills: Long? = null,
    /** Total number of heroes killed by the player. */
    @SerialName("hero_kills") val heroKills: Long? = null,
    /** Total number of observer wards killed by the player. */
    @SerialName("observer_kills") val observerKills: Long? = null,
    /** Total number of sentry wards killed by the player. */
    @SerialName("sentry_kills") val sentryKills: Long? = null,
    /** Total number of roshan kills (last hit on roshan) the player had. */
    @SerialName("roshan_kills") val roshanKills: Long? = null,
    /** Total number of Necronomicon creeps killed by the player. */
    @SerialName("necronomicon_kills") val necronomiconKills: Long? = null,
    /** Total number of Ancient creeps killed by the player. */
    @SerialName("ancient_kills") val ancientKills: Long? = null,
    /** Total number of buyback the player used. */
    @SerialName("buyback_count") val buybackCount: Long? = null,
    /** Number of observer wards used. */
    @SerialName("observer_uses") val observerUses: Long? = null,
    /** Number of sentry wards used. */
    @SerialName("sentry_uses") val sentryUses: Long? = null,
    /** Share of the lane's available gold and experience collected, from 0.0 to 1.0. */
    @SerialName("lane_efficiency") val laneEfficiency: Double? = null,
    /** Lane efficiency as a percentage. */
    @SerialName("lane_efficiency_pct") val laneEfficiencyPct: Double? = null,
    /** Integer referring to which lane the hero laned in. */
    @SerialName("lane") val lane: Long? = null,
    /** Lane role ID. */
    @SerialName("lane_role") val laneRole: Long? = null,
    /** Boolean referring to whether or not the player roamed. */
    @SerialName("is_roaming") val isRoaming: Boolean? = null,
    /**
     * Estimated position (1-5) of the player within their team, from early farm priority and lane data. Only present on
     * parsed matches.
     */
    @SerialName("position_est") val positionEst: Long? = null,
    /** Object with information on when the player last purchased an item. */
    @SerialName("purchase_time") val purchaseTime: Map<String, Long>? = null,
    /** Object with information on when the player first puchased an item. */
    @SerialName("first_purchase_time") val firstPurchaseTime: Map<String, Long>? = null,
    /** Object with information on whether or not the item won. */
    @SerialName("item_win") val itemWin: Map<String, Long>? = null,
    /**
     * Object containing binary integers the tell whether the item was purchased by the player (note: this is always 1).
     */
    @SerialName("item_usage") val itemUsage: Map<String, Long>? = null,
    /** Total number of TP scrolls purchased by the player. */
    @SerialName("purchase_tpscroll") val purchaseTpscroll: Long? = null,
    /** Total number of observer wards purchased by the player. */
    @SerialName("purchase_ward_observer") val purchaseWardObserver: Long? = null,
    /** Total number of sentry wards purchased by the player. */
    @SerialName("purchase_ward_sentry") val purchaseWardSentry: Long? = null,
    /** Total number of gems purchased by the player. */
    @SerialName("purchase_gem") val purchaseGem: Long? = null,
    /** Total number of Divine Rapiers purchased by the player. */
    @SerialName("purchase_rapier") val purchaseRapier: Long? = null,
    /** Actions per minute. */
    @SerialName("actions_per_min") val actionsPerMin: Long? = null,
    /** Total time spent dead, in seconds. */
    @SerialName("life_state_dead") val lifeStateDead: Long? = null,
    /** The rank tier of the player. Tens place indicates rank, ones place indicates stars. */
    @SerialName("rank_tier") val rankTier: Long? = null,
    /** Cosmetic items equipped by the player. */
    @SerialName("cosmetics") val cosmetics: List<MatchResponsePlayersCosmetics>? = null,
    /** Object containing information on certain benchmarks like GPM, XPM, KDA, tower damage, etc. */
    @SerialName("benchmarks") val benchmarks: Map<String, MatchResponsePlayersBenchmark>? = null,
    /** Object containing information on neutral tokens drops. */
    @SerialName("neutral_tokens_log") val neutralTokensLog: List<MatchResponsePlayersNeutralTokensLog>? = null,
    /** Object containing information on neutral item history. */
    @SerialName("neutral_item_history") val neutralItemHistory: List<MatchResponsePlayersNeutralItemHistory>? = null,
)

/**
 * Array containing information about buybacks.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponsePlayersBuybackLog(
    /** Time in seconds the buyback occurred. */
    @SerialName("time") val time: Long? = null,
    /** Kind of entry, always `buyback_log`. */
    @SerialName("type") val type: String? = null,
    /** Player slot index of the buyback. */
    @SerialName("slot") val slot: Long? = null,
    /** Which slot the player is in. 0-127 are Radiant, 128-255 are Dire. */
    @SerialName("player_slot") val playerSlot: Long? = null,
)

/**
 * Array containing information about the player's disconnections and reconnections.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponsePlayersConnectionLog(
    /** Game time in seconds the event ocurred. */
    @SerialName("time") val time: Long? = null,
    /** Event that occurred. */
    @SerialName("event") val event: String? = null,
    /** Kind of entry, always `connection_log`. */
    @SerialName("type") val type: String? = null,
    /** Player slot index of the event. */
    @SerialName("slot") val slot: Long? = null,
    /** Which slot the player is in. 0-127 are Radiant, 128-255 are Dire. */
    @SerialName("player_slot") val playerSlot: Long? = null,
)

/**
 * Array containing information on which hero the player killed at what time.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponsePlayersKillsLog(
    /** Time in seconds the player killed the hero. */
    @SerialName("time") val time: Long? = null,
    /** Hero killed. */
    @SerialName("key") val key: String? = null,
    /** Whether the kill was made from smoke. */
    @SerialName("smoke") val smoke: Boolean? = null,
)

/**
 * Object containing information on when items were purchased.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponsePlayersPurchaseLog(
    /** Time in seconds the item was bought. */
    @SerialName("time") val time: Long? = null,
    /** String item ID. */
    @SerialName("key") val key: String? = null,
    /** Integer amount of charges. */
    @SerialName("charges") val charges: Long? = null,
)

/**
 * Array with information on when runes were picked up.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponsePlayersRunesLog(
    /** Time in seconds rune picked up. */
    @SerialName("time") val time: Long? = null,
    /**
     * Type of rune picked up. List of constants can be found here:
     * https://github.com/odota/dotaconstants/blob/master/json/runes.json.
     *
     * Decoded from a JSON string or number alike, since the API sends the rune type as a string where the published
     * schema calls it a number.
     */
    @SerialName("key") @Serializable(with = LenientStringSerializer::class) val key: String? = null,
)

/**
 * Cosmetics.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponsePlayersCosmetics(
    /** ID of the cosmetic item. */
    @SerialName("item_id") val itemId: Long? = null,
    /** Internal name of the cosmetic item. */
    @SerialName("name") val name: String? = null,
    /** Prefab type of the cosmetic item. */
    @SerialName("prefab") val prefab: String? = null,
    /** Date the cosmetic item was created. */
    @SerialName("creation_date") val creationDate: String? = null,
    /** Path to the cosmetic item's inventory image. */
    @SerialName("image_inventory") val imageInventory: String? = null,
    /** Path to the cosmetic item's image. */
    @SerialName("image_path") val imagePath: String? = null,
    /** Localisation token for the cosmetic item's description. */
    @SerialName("item_description") val itemDescription: String? = null,
    /** Localisation token for the cosmetic item's name. */
    @SerialName("item_name") val itemName: String? = null,
    /** Rarity of the cosmetic item. */
    @SerialName("item_rarity") val itemRarity: String? = null,
    /** Localisation token for the cosmetic item's type. */
    @SerialName("item_type_name") val itemTypeName: String? = null,
    /** Internal name of the hero the cosmetic item is worn by. */
    @SerialName("used_by_heroes") val usedByHeroes: String? = null,
)

/**
 * Object containing information on neutral tokens drops.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponsePlayersNeutralTokensLog(
    /** Time in seconds at which the token was dropped. */
    @SerialName("time") val time: Long? = null,
    /** Type of token dropped. */
    @SerialName("key") val key: String? = null,
)

/**
 * Object containing information on neutral item history.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponsePlayersNeutralItemHistory(
    /** Time in seconds at which the item was crafted. */
    @SerialName("time") val time: Long? = null,
    /** Neutral item name. */
    @SerialName("item_neutral") val itemNeutral: String? = null,
    /** Neutral enhancement name. */
    @SerialName("item_neutral_enhancement") val itemNeutralEnhancement: String? = null,
)

/**
 * Array containing information about pauses during the game. Each item contains the time and duration of the pause.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponsePauses(
    /** Time in seconds at which pause started. */
    @SerialName("time") val time: Long? = null,
    /** The duration of the pause. */
    @SerialName("duration") val duration: Long? = null,
)

/**
 * Objective events such as tower, barracks and Roshan kills.
 *
 * Which members carry a value depends on [type]: a `building_kill` names the building in [key] and its killer in
 * [unit], while a `CHAT_MESSAGE_ROSHAN_KILL` carries only [team].
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponseObjectives(
    /** Time in seconds at which the objective was taken. */
    @SerialName("time") val time: Long? = null,
    /**
     * Kind of objective, either a parsed event such as `building_kill` or a Dota chat message such as
     * `CHAT_MESSAGE_FIRSTBLOOD`, `CHAT_MESSAGE_AEGIS` or `CHAT_MESSAGE_ROSHAN_KILL`.
     */
    @SerialName("type") val type: String? = null,
    /** Name of the unit that took the objective. */
    @SerialName("unit") val unit: String? = null,
    /**
     * What the objective was taken on, such as the name of the building killed.
     *
     * Decoded from a JSON string or number alike, since the API sends either depending on [type].
     */
    @SerialName("key") @Serializable(with = LenientStringSerializer::class) val key: String? = null,
    /** Player slot index of the player credited with the objective. */
    @SerialName("slot") val slot: Long? = null,
    /** Which slot the player is in. 0-127 are Radiant, 128-255 are Dire. */
    @SerialName("player_slot") val playerSlot: Long? = null,
    /** Which team took the objective. 2 is Radiant and 3 is Dire. */
    @SerialName("team") val team: Long? = null,
    /** Value carried by the event, such as the gold bounty of a kill. */
    @SerialName("value") val value: Long? = null,
    /** Player slot index of the killer. */
    @SerialName("killer") val killer: Long? = null,
    /** Which slot the victim of the event is in. 0-127 are Radiant, 128-255 are Dire. */
    @SerialName("victim_player_slot") val victimPlayerSlot: Long? = null,
)

/**
 * Team fight summaries extracted from the replay.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponseTeamfights(
    /** Time in seconds at which the fight started. */
    @SerialName("start") val start: Long? = null,
    /** Time in seconds at which the fight ended. */
    @SerialName("end") val end: Long? = null,
    /** Time in seconds of the last death in the fight. */
    @SerialName("last_death") val lastDeath: Long? = null,
    /** Number of deaths in the fight. */
    @SerialName("deaths") val deaths: Long? = null,
    /** What each of the ten players did in the fight, in player slot order. */
    @SerialName("players") val players: List<MatchResponseTeamfightsPlayers>? = null,
)

/**
 * What one player did over a single team fight.
 *
 * Nested payload of [MatchResponseTeamfights].
 */
@Serializable
data class MatchResponseTeamfightsPlayers(
    /**
     * Where the player died, keyed by x and then by y position. The location takes the form (outer number, inner
     * number) and are from ~64-192.
     */
    @SerialName("deaths_pos") val deathsPos: Map<String, Map<String, Long>>? = null,
    /** How many times the player used each of their abilities, keyed by ability name. */
    @SerialName("ability_uses") val abilityUses: Map<String, Long>? = null,
    /** Who the player used each ability on, keyed by ability name and then by target unit name. */
    @SerialName("ability_targets") val abilityTargets: Map<String, Map<String, Long>>? = null,
    /** How many times the player used each item, keyed by item name. */
    @SerialName("item_uses") val itemUses: Map<String, Long>? = null,
    /** What the player killed, keyed by unit name. */
    @SerialName("killed") val killed: Map<String, Long>? = null,
    /** Number of times the player died in the fight. */
    @SerialName("deaths") val deaths: Long? = null,
    /** Number of times the player bought back in the fight. */
    @SerialName("buybacks") val buybacks: Long? = null,
    /** Damage the player dealt over the fight. */
    @SerialName("damage") val damage: Long? = null,
    /** Healing the player did over the fight. */
    @SerialName("healing") val healing: Long? = null,
    /** Gold the player gained or lost over the fight. */
    @SerialName("gold_delta") val goldDelta: Long? = null,
    /** Experience the player gained over the fight. */
    @SerialName("xp_delta") val xpDelta: Long? = null,
    /** Experience the player held when the fight started. */
    @SerialName("xp_start") val xpStart: Long? = null,
    /** Experience the player held when the fight ended. */
    @SerialName("xp_end") val xpEnd: Long? = null,
)

/**
 * Registered team details, for matches played by a registered team.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponseTeam(
    /** The team's team_id. */
    @SerialName("team_id") val teamId: Long? = null,
    /** The team's name. */
    @SerialName("name") val name: String? = null,
    /** The team's tag. */
    @SerialName("tag") val tag: String? = null,
    /** URL of the team's logo, where the team has uploaded one. */
    @SerialName("logo_url") val logoUrl: String? = null,
)

/**
 * League details, for league matches.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponseLeague(
    /** ID of the league. */
    @SerialName("leagueid") val leagueid: Long? = null,
    /** Ticket the league was sold under. */
    @SerialName("ticket") val ticket: String? = null,
    /** Path to the league's banner image. */
    @SerialName("banner") val banner: String? = null,
    /** Tier of the league, such as `professional` or `premium`. */
    @SerialName("tier") val tier: String? = null,
    /** Name of the league. */
    @SerialName("name") val name: String? = null,
)

/**
 * Which sources OpenDota holds data for a match from.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponseOdData(
    /** Whether OpenDota holds the match summary Valve's Web API serves. */
    @SerialName("has_api") val hasApi: Boolean? = null,
    /** Whether OpenDota holds the match data Valve's game coordinator serves. */
    @SerialName("has_gcdata") val hasGcdata: Boolean? = null,
    /** Whether the replay has been parsed, which the per-minute and log members depend on. */
    @SerialName("has_parsed") val hasParsed: Boolean? = null,
    /** Whether the parsed match has been moved to OpenDota's archive. */
    @SerialName("has_archive") val hasArchive: Boolean? = null,
)

/**
 * Array containing information on every death the player suffered.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponsePlayersDeathsLog(
    /** Time in seconds at which the player died. */
    @SerialName("time") val time: Long? = null,
    /** Name of the unit that killed the player. */
    @SerialName("key") val key: String? = null,
    /** Gold the death handed the enemy team. */
    @SerialName("gold_fed") val goldFed: Long? = null,
    /** Whether the gold the death handed over was shared across the enemy team. */
    @SerialName("gold_fed_shared") val goldFedShared: Boolean? = null,
    /** Gold the player lost to the death. */
    @SerialName("gold_lost") val goldLost: Long? = null,
    /** How long the player stayed dead, in seconds. */
    @SerialName("time_dead") val timeDead: Long? = null,
)

/**
 * A ward placed by the player, or one of theirs that left the map.
 *
 * Carries the entries of `obs_log` and `sen_log`, where a ward was placed, and of `obs_left_log` and `sen_left_log`,
 * where one expired or was destroyed. [attackername] is set only on the latter two, and only for a ward that was
 * destroyed.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponsePlayersWardLog(
    /** Time in seconds at which the ward was placed, or at which it left the map. */
    @SerialName("time") val time: Long? = null,
    /** Which log the entry belongs to, one of `obs_log`, `sen_log`, `obs_left_log` and `sen_left_log`. */
    @SerialName("type") val type: String? = null,
    /** Position of the ward, as a `[x,y]` pair of the rounded coordinates. */
    @SerialName("key") val key: String? = null,
    /** Player slot index of the player who placed the ward. */
    @SerialName("slot") val slot: Long? = null,
    /** Which slot the player is in. 0-127 are Radiant, 128-255 are Dire. */
    @SerialName("player_slot") val playerSlot: Long? = null,
    /** Name of the unit that destroyed the ward, for a ward that did not expire on its own. */
    @SerialName("attackername") val attackername: String? = null,
    /** The ward's x position, from ~64-192. */
    @SerialName("x") val x: Double? = null,
    /** The ward's y position, from ~64-192. */
    @SerialName("y") val y: Double? = null,
    /** The ward's z position. */
    @SerialName("z") val z: Double? = null,
    /** Whether the ward has left the map, which is `true` throughout the two `_left_log` members. */
    @SerialName("entityleft") val entityleft: Boolean? = null,
    /** Handle the replay identifies the ward entity by, which pairs a placement up with its departure. */
    @SerialName("ehandle") val ehandle: Long? = null,
)

/**
 * Array describing permanent buffs the player had at the end of the game. List of constants can be found here:
 * https://github.com/odota/dotaconstants/blob/master/json/permanent_buffs.json.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponsePlayersPermanentBuffs(
    /** ID of the permanent buff. */
    @SerialName("permanent_buff") val permanentBuff: Long? = null,
    /** How many stacks of the buff the player held. */
    @SerialName("stack_count") val stackCount: Long? = null,
    /** Time in seconds at which the buff was granted. */
    @SerialName("grant_time") val grantTime: Long? = null,
)

/**
 * Object containing information on additional units the player had under their control.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponsePlayersAdditionalUnits(
    /** Name of the unit, which is empty for a unit the replay does not name. */
    @SerialName("unitname") val unitname: String? = null,
    /** Item in the unit's first slot. */
    @SerialName("item_0") val item0: Long? = null,
    /** Item in the unit's second slot. */
    @SerialName("item_1") val item1: Long? = null,
    /** Item in the unit's third slot. */
    @SerialName("item_2") val item2: Long? = null,
    /** Item in the unit's fourth slot. */
    @SerialName("item_3") val item3: Long? = null,
    /** Item in the unit's fifth slot. */
    @SerialName("item_4") val item4: Long? = null,
    /** Item in the unit's sixth slot. */
    @SerialName("item_5") val item5: Long? = null,
    /** Item in the unit's first backpack slot. */
    @SerialName("backpack_0") val backpack0: Long? = null,
    /** Item in the unit's second backpack slot. */
    @SerialName("backpack_1") val backpack1: Long? = null,
    /** Item in the unit's third backpack slot. */
    @SerialName("backpack_2") val backpack2: Long? = null,
    /** Item in the unit's neutral slot. */
    @SerialName("item_neutral") val itemNeutral: Long? = null,
    /** Enhancement in the unit's neutral slot. */
    @SerialName("item_neutral2") val itemNeutral2: Long? = null,
)

/**
 * Object with information on the highest damage instance the player inflicted.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponsePlayersMaxHeroHit(
    /** Time in seconds at which the damage was dealt. */
    @SerialName("time") val time: Long? = null,
    /** Kind of entry, always `max_hero_hit`. */
    @SerialName("type") val type: String? = null,
    /** Name of the unit that dealt the damage. */
    @SerialName("unit") val unit: String? = null,
    /** Name of the hero the damage was dealt to. */
    @SerialName("key") val key: String? = null,
    /** How much damage was dealt. */
    @SerialName("value") val value: Long? = null,
    /** Player slot index of the player who dealt the damage. */
    @SerialName("slot") val slot: Long? = null,
    /** Which slot the player is in. 0-127 are Radiant, 128-255 are Dire. */
    @SerialName("player_slot") val playerSlot: Long? = null,
    /** Whether the entry is the player's maximum, which it is throughout this member. */
    @SerialName("max") val max: Boolean? = null,
    /** Name of the ability or item that dealt the damage. */
    @SerialName("inflictor") val inflictor: String? = null,
)

/**
 * How one of a player's benchmarks compares to the same benchmark across other matches on the hero.
 *
 * Nested payload of [MatchResponse].
 */
@Serializable
data class MatchResponsePlayersBenchmark(
    /** The player's own value for the benchmark. */
    @SerialName("raw") val raw: Double? = null,
    /** Share of players on the hero the player beat on the benchmark, from 0.0 to 1.0. */
    @SerialName("pct") val pct: Double? = null,
)

/**
 * Returned by the `GET /proMatches`, `GET /heroes/{hero_id}/matches`, `GET /leagues/{league_id}/matches` endpoints.
 *
 * @see io.github.yesitskev.kopendota.OpenDota.getProMatches
 * @see io.github.yesitskev.kopendota.OpenDota.getHeroMatches
 * @see io.github.yesitskev.kopendota.OpenDota.getLeagueMatches
 */
@Serializable
data class MatchObjectResponse(
    /** The ID number of the match assigned by Valve. */
    @SerialName("match_id") val matchId: Long? = null,
    /** Duration of the game in seconds. */
    @SerialName("duration") val duration: Long? = null,
    /** The Unix timestamp at which the game started. */
    @SerialName("start_time") val startTime: Long? = null,
    /** The Radiant's team_id. */
    @SerialName("radiant_team_id") val radiantTeamId: Long? = null,
    /** The Radiant's team name. */
    @SerialName("radiant_name") val radiantName: String? = null,
    /** The Dire's team_id. */
    @SerialName("dire_team_id") val direTeamId: Long? = null,
    /** The Dire's team name. */
    @SerialName("dire_name") val direName: String? = null,
    /** Identifier for the league the match took place in. */
    @SerialName("leagueid") val leagueid: Long? = null,
    /** Name of league the match took place in. */
    @SerialName("league_name") val leagueName: String? = null,
    /** Identifier for the series of the match. */
    @SerialName("series_id") val seriesId: Long? = null,
    /** Type of series the match was. */
    @SerialName("series_type") val seriesType: Long? = null,
    /** Number of kills the Radiant team had when the match ended. */
    @SerialName("radiant_score") val radiantScore: Long? = null,
    /** Number of kills the Dire team had when the match ended. */
    @SerialName("dire_score") val direScore: Long? = null,
    /** Boolean indicating whether Radiant won the match. */
    @SerialName("radiant_win") val radiantWin: Boolean? = null,
    /** Whether the team/player/hero was on Radiant. */
    @SerialName("radiant") val radiant: Boolean? = null,
)
