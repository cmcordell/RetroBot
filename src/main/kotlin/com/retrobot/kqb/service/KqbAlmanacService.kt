package com.retrobot.kqb.service

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.retrobot.core.Duration
import com.retrobot.core.domain.service.Service
import com.retrobot.core.util.Logger
import com.retrobot.core.util.launchContinue
import com.retrobot.core.util.toDoubleOrDefault
import com.retrobot.core.util.toIntOrDefault
import com.retrobot.kqb.data.AwardRepository
import com.retrobot.kqb.data.CasterRepository
import com.retrobot.kqb.data.MatchRepository
import com.retrobot.kqb.data.TeamRepository
import com.retrobot.kqb.domain.model.*
import kotlinx.coroutines.*
import org.koin.core.inject
import java.lang.String.format
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

/**
 * [Service] to periodically pull KQB Competition information from the KQB IGL Almanac.
 *
 * @param updatePeriod How often to pull data from the Almanac in milliseconds
 */
class KqbAlmanacService(
        private val updatePeriod: Long = 15 * Duration.MINUTE
) : Service {
    override val key = "KqbAlmanacService"

    // TODO Un-hardcode this.  Might have to ask BeesKnees to add a field to the almanac
    private val SEASON = "Fall"

    private val GOOGLE_SPREADSHEETS_URL = "https://docs.google.com/spreadsheets/d/%s/export?format=csv&gid=%s"
    private val WORKBOOK_ID_KQB_ALMANAC = "11QHK-mGfUhhHa8OsFZwiYn2da6IXUTPYuVKtXC7mMjU"
    private val SHEET_ID_AWARDS = "1077693514"
    private val SHEET_ID_CASTERS = "1096224726"
    private val SHEET_ID_MATCHES = "1122078494"
    private val SHEET_ID_TEAMS = "0"

    private val awardRepo: AwardRepository by inject()
    private val casterRepo: CasterRepository by inject()
    private val matchRepo: MatchRepository by inject()
    private val teamRepo: TeamRepository by inject()

    private val csvReader: CsvReader by inject()

    private var scope = CoroutineScope(Job() + Dispatchers.Default)


    override fun start() {
        scope.launchContinue {
            while (isActive()) {
                pullAwards()
                pullCasters()
                pullMatches()
                pullTeams()
                delay(updatePeriod)
            }
        }
    }

    override fun stop() {
        scope.cancel()
    }

    override fun isActive() = scope.isActive

    private suspend fun pullAwards() {
        try {
            val inputStream = getSheetCsvStream(WORKBOOK_ID_KQB_ALMANAC, SHEET_ID_AWARDS)

            val awards = mutableListOf<Award>()
            csvReader.open(inputStream) {
                readAllAsSequence().drop(2).forEach { row ->
                    awards.addAll(mapRowToAwards(row))
                }
            }
            awardRepo.clear()
            awardRepo.put(awards)
        } catch (e: Exception) {
            Logger.log(e)
        }
    }

    private fun mapRowToAwards(row: List<String>) : List<Award> {
        return try {
            val awards = mutableListOf<Award>()

            val circuit = row[1].substring(1, 2)
            val division = row[1].take(1)
            val conference = row[1].takeIf { column -> column.length > 2 }?.substring(2) ?: ""
            val week = row[0].take(20)

            awards.add(Award(
                    awardType = AwardType.QUEEN_OF_THE_HIVE,
                    season = SEASON,
                    circuit = circuit,
                    division = division,
                    conference = conference,
                    week = week,
                    player = row[3].take(100),
                    stats = listOf(Statistic("KDR", row[2].toDoubleOrDefault()))
            ))

            awards.add(Award(
                    awardType = AwardType.ETERNAL_WARRIOR,
                    season = SEASON,
                    circuit = circuit,
                    division = division,
                    conference = conference,
                    week = week,
                    player = row[5].take(100),
                    stats = listOf(Statistic("Kills/Set", row[4].toDoubleOrDefault()))
            ))

            awards.add(Award(
                    awardType = AwardType.PURPLE_HEART,
                    season = SEASON,
                    circuit = circuit,
                    division = division,
                    conference = conference,
                    week = week,
                    player = row[7].take(100),
                    stats = listOf(Statistic("Deaths/Set & Win", row[6].toDoubleOrDefault()))
            ))

            awards.add(Award(
                    awardType = AwardType.BERRY_BONANZA,
                    season = SEASON,
                    circuit = circuit,
                    division = division,
                    conference = conference,
                    week = week,
                    player = row[9].take(100),
                    stats = listOf(Statistic("Berries/Set", row[8].toDoubleOrDefault()))
            ))

            awards.add(Award(
                    awardType = AwardType.SNAIL_WHISPERER,
                    season = SEASON,
                    circuit = circuit,
                    division = division,
                    conference = conference,
                    week = week,
                    player = row[11].take(100),
                    stats = listOf(Statistic("Snail/Set", row[10].toDoubleOrDefault()))
            ))

            awards.add(Award(
                    awardType = AwardType.TRIPLE_THREAT,
                    season = SEASON,
                    circuit = circuit,
                    division = division,
                    conference = conference,
                    week = week,
                    player = row[13].take(100),
                    stats = listOf(
                            Statistic("Score", row[12].toDoubleOrDefault()),
                            Statistic("Kills/Set", row[14].toDoubleOrDefault()),
                            Statistic("Berries/Set", row[15].toDoubleOrDefault()),
                            Statistic("Snail/Set", row[16].toDoubleOrDefault())
                    )
            ))

            awards
        } catch (e: Exception) {
            Logger.log(e)
            emptyList()
        }
    }

    private suspend fun pullCasters() {
        try {
            val inputStream = getSheetCsvStream(WORKBOOK_ID_KQB_ALMANAC, SHEET_ID_CASTERS)

            val casters = mutableListOf<Caster>()
            csvReader.open(inputStream) {
                readAllAsSequence().drop(2).forEach { row ->
                    mapRowToCaster(row)?.let { caster ->
                        if (caster.name.isNotBlank() && caster.gamesCasted >= 0) {
                            casters.add(caster)
                        }
                    }
                }
            }
            casterRepo.clear()
            casterRepo.put(casters)
        } catch (e: Exception) {
            Logger.log(e)
        }
    }

    private fun mapRowToCaster(row: List<String>) : Caster? {
        return try {
            Caster(
                    name = row[1].take(50),
                    streamLink = row[2],
                    bio = row[3],
                    gamesCasted = row[4].toIntOrDefault(-1)
            )
        } catch (e: Exception) {
            Logger.log(e)
            null
        }
    }

    private suspend fun pullMatches() {
        try {
            val inputStream = getSheetCsvStream(WORKBOOK_ID_KQB_ALMANAC, SHEET_ID_MATCHES)

            val matches = mutableListOf<Match>()
            csvReader.open(inputStream) {
                readAllAsSequence().drop(1).forEach { row ->
                    mapRowToMatch(row)?.let(matches::add)
                }
            }
            matchRepo.clear()
            matchRepo.put(matches)
        } catch (e: Exception) {
            Logger.log(e)
        }
    }

    private fun mapRowToMatch(row: List<String>) : Match? {
        var match: Match? = null
        if (row[0].isNotBlank()) {
            try {
                match = Match(
                        season = SEASON,
                        circuit = row[2].take(1),
                        division = row[1].take(10),
                        conference = row[2].takeIf { column -> column.length > 1 }?.substring(1) ?: "",
                        week = row[0].take(20),
                        awayTeam = row[3].take(100),
                        homeTeam = row[5].take(100),
                        colorScheme = if (row[6].equals("default", true)) ColorScheme.DEFAULT else ColorScheme.SWAP,
                        date = getDateLong(row[7], row[8]),
                        caster = row[10].take(50),
                        coCasters = row[11].split(",").filter(String::isNotEmpty),
                        streamLink = row[12],
                        vodLink = row[13],
                        awaySetsWon = row[16].toIntOrDefault(),
                        homeSetsWon = row[17].toIntOrDefault(),
                )
            } catch (e: Exception) {
                Logger.log(e)
            }
        }

        return match
    }

    private fun getDateLong(time: String, date: String) : Long {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a")
            dateFormat.timeZone = TimeZone.getTimeZone("US/Eastern")
            dateFormat.parse("$date $time").time
        } catch (e: Exception) {
            Logger.log(e)
            0
        }
    }

    private suspend fun pullTeams() {
        try {
            val inputStream = getSheetCsvStream(WORKBOOK_ID_KQB_ALMANAC, SHEET_ID_TEAMS)

            val teams = mutableListOf<Team>()
            csvReader.open(inputStream) {
                readAllAsSequence().drop(1).forEach { row ->
                    mapRowToTeam(row)?.let(teams::add)
                }
            }
            teamRepo.clear()
            teamRepo.put(teams)
        } catch (e: Exception) {
            Logger.log(e)
        }
    }

    private fun mapRowToTeam(row: List<String>) : Team? {
        return try {
            Team(
                    name = row[2].take(100),
                    captain = row[11].take(100),
                    members = row.subList(12, 19).filter(String::isNotEmpty),
                    season = SEASON,
                    circuit = row[1].take(1),
                    division = row[0].take(10),
                    conference = row[1].takeIf { column -> column.length > 1 }?.substring(1) ?: "",
                    matchesWon = row[3].toIntOrDefault(),
                    matchesLost = row[4].toIntOrDefault(),
                    matchesPlayed = row[3].toIntOrDefault() + row[4].toIntOrDefault(),
                    setsWon = row[8].toIntOrDefault(),
                    setsLost = row[9].toIntOrDefault() - row[8].toIntOrDefault(),
                    setsPlayed = row[9].toIntOrDefault(),
                    playoffSeed = row[20].toIntOrDefault(),
                    infoLink = row[24]
            )
        } catch (e: Exception) {
            Logger.log(e)
            null
        }
    }

    private suspend fun getSheetCsvStream(spreadsheetId: String, sheetId: String) = withContext(scope.coroutineContext + Dispatchers.IO) {
        URL(format(GOOGLE_SPREADSHEETS_URL, spreadsheetId, sheetId)).openStream()
    }
}