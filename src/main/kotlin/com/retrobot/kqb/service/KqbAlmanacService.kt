package com.retrobot.kqb.service

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.retrobot.core.Duration
import com.retrobot.core.domain.service.Service
import com.retrobot.core.util.Logger
import com.retrobot.core.util.launchContinue
import com.retrobot.kqb.data.CasterRepository
import com.retrobot.kqb.data.MatchRepository
import com.retrobot.kqb.data.TeamRepository
import com.retrobot.kqb.domain.model.Caster
import com.retrobot.kqb.domain.model.ColorScheme
import com.retrobot.kqb.domain.model.Match
import com.retrobot.kqb.domain.model.Team
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

    private val GOOGLE_SPREADSHEETS_URL = "https://docs.google.com/spreadsheets/d/%s/export?format=csv&id=%s&gid=%s"
    private val WORKBOOK_ID_KQB_ALMANAC = "11QHK-mGfUhhHa8OsFZwiYn2da6IXUTPYuVKtXC7mMjU"
    private val SHEET_ID_MATCHES = "1122078494"
    private val SHEET_ID_TEAMS = "0"
    private val SHEET_ID_CASTERS = "1096224726"

    private val casterRepo: CasterRepository by inject()
    private val matchRepo: MatchRepository by inject()
    private val teamRepo: TeamRepository by inject()

    private val csvReader: CsvReader by inject()

    private var scope = CoroutineScope(Job() + Dispatchers.Default)


    override fun start() {
        scope.launchContinue {
            while (isActive()) {
                pullMatches()
                pullTeams()
                pullCasters()
                delay(updatePeriod)
            }
        }
    }

    override fun stop() {
        scope.cancel()
    }

    override fun isActive() = scope.isActive

    private suspend fun pullMatches() {
        try {
            val inputStream = getSheetCsvStream(WORKBOOK_ID_KQB_ALMANAC, SHEET_ID_MATCHES)

            val matches = mutableSetOf<Match>()
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
        if (row[0].isEmpty()) return null
        return try {
            Match(
                    season = "Summer",
                    circuit = row[2].take(1),
                    division = row[1],
                    conference = row[2].takeIf { column -> column.length > 1 }?.substring(1) ?: "",
                    week = row[0],
                    awayTeam = row[3],
                    homeTeam = row[5],
                    colorScheme = if (row[6].equals("default", true)) ColorScheme.DEFAULT else ColorScheme.SWAP,
                    date = getDateLong(row[7], row[8]),
                    caster = row[10],
                    coCasters = row[11].split(",").filter(String::isNotEmpty),
                    streamLink = row[12],
                    vodLink = row[13],
                    awaySetsWon = if (row[16].isNotEmpty()) row[16].toInt() else 0,
                    homeSetsWon = if (row[17].isNotEmpty()) row[16].toInt() else 0,
                    winner = row[18]
            )
        } catch (e: Exception) {
            Logger.log(e)
            null
        }
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

            val teams = mutableSetOf<Team>()
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
                    name = row[2],
                    captain = row[11],
                    members = row.subList(12, 19).filter(String::isNotEmpty),
                    season = "Summer",
                    circuit = row[1].take(1),
                    division = row[0],
                    conference = row[1].takeIf { column -> column.length > 1 }?.substring(1) ?: "",
                    matchesWon = row[3].toInt(),
                    matchesLost = row[4].toInt(),
                    matchesPlayed = row[3].toInt() + row[4].toInt(),
                    setsWon = row[8].toInt(),
                    setsLost = row[9].toInt() - row[8].toInt(),
                    setsPlayed = row[9].toInt(),
                    playoffSeed = row[20].toInt()
            )
        } catch (e: Exception) {
            Logger.log(e)
            null
        }
    }

    private suspend fun pullCasters() {
        try {
            val inputStream = getSheetCsvStream(WORKBOOK_ID_KQB_ALMANAC, SHEET_ID_CASTERS)

            val casters = mutableSetOf<Caster>()
            csvReader.open(inputStream) {
                readAllAsSequence().drop(1).forEach { row ->
                    mapRowToCaster(row)?.let { caster ->
                        if (caster.name.isNotBlank()) {
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
                    name = row[0],
                    streamLink = row[1]
            )
        } catch (e: Exception) {
            Logger.log(e)
            null
        }
    }

    private suspend fun getSheetCsvStream(spreadsheetId: String, sheetId: String) = withContext(scope.coroutineContext + Dispatchers.IO) {
        URL(format(GOOGLE_SPREADSHEETS_URL, spreadsheetId, spreadsheetId, sheetId)).openStream()
    }
}