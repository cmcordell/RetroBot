package com.retrobot.kqb.data.exposedrepo

import com.retrobot.core.util.*
import com.retrobot.kqb.data.MatchRepository
import com.retrobot.kqb.data.exposedrepo.KqbDatabase.Matches
import com.retrobot.kqb.domain.model.Match
import org.jetbrains.exposed.sql.*

/**
 * KQB Match Repository implement with Kotlin Exposed DSL
 */
class ExposedMatchRepository(kqbDatabase: KqbDatabase) : MatchRepository {

    private val database = Database.connect(kqbDatabase.dataSource)
    
    override suspend fun put(match: Match) = dbActionQuery(database) {
        Matches.upsert(Matches.columns) { table ->
            table[season] = match.season
            table[circuit] = match.circuit
            table[division] = match.division
            table[conference] = match.conference
            table[week] = match.week
            table[awayTeam] = match.awayTeam
            table[homeTeam] = match.homeTeam
            table[color] = match.colorScheme
            table[date] = match.date
            table[caster] = match.caster
            table[coCasters] = match.coCasters.toDelimitedString(",")
            table[streamLink] = match.streamLink
            table[vodLink] = match.vodLink
            table[awaySetsWon] = match.awaySetsWon
            table[homeSetsWon] = match.homeSetsWon
        }
    }

    override suspend fun put(matches: List<Match>) = dbActionQuery(database) {
        Matches.batchUpsert(matches, Matches.columns, true) { batch, match ->
            batch[season] = match.season
            batch[circuit] = match.circuit
            batch[division] = match.division
            batch[conference] = match.conference
            batch[week] = match.week
            batch[awayTeam] = match.awayTeam
            batch[homeTeam] = match.homeTeam
            batch[color] = match.colorScheme
            batch[date] = match.date
            batch[caster] = match.caster
            batch[coCasters] = match.coCasters.toDelimitedString(",")
            batch[streamLink] = match.streamLink
            batch[vodLink] = match.vodLink
            batch[awaySetsWon] = match.awaySetsWon
            batch[homeSetsWon] = match.homeSetsWon
        }
    }

    override suspend fun getByWeekAndTeams(week: String, awayTeam: String, homeTeam: String): Match? = dbQuery(database) {
        Matches.select { Matches.week.upperCase() like "%${week.toUpperCase()}%" }
                .andWhere { Matches.awayTeam.upperCase() like "%${awayTeam.toUpperCase()}%" }
                .andWhere { Matches.homeTeam.upperCase() like "%${homeTeam.toUpperCase()}%" }
                .map(this::toMatch)
                .firstOrNull()
    }

    override suspend fun getByCircuit(circuit: String, division: String, conference: String) : List<Match> = dbQuery(database) {
        Matches.select { Matches.circuit.upperCase() like "%${circuit.toUpperCase()}%" }
                .andWhere { Matches.division.upperCase() like "%${division.toUpperCase()}%" }
                .andWhere { Matches.conference.upperCase() like "%${conference.toUpperCase()}%" }
                .map(this::toMatch)
                .toList()
    }

    override suspend fun getByDate(from: Long, to: Long) : List<Match> = dbQuery(database) {
        Matches.select { Matches.date greaterEq from }
                .andWhere { Matches.date lessEq to }
                .map(this::toMatch)
                .toList()
    }

    override suspend fun getByWeek(week: String) : List<Match> = dbQuery(database) {
        Matches.select { Matches.week.upperCase() like "%${week.toUpperCase()}%" }
            .map(this::toMatch)
            .toList()
    }

    override suspend fun getByTeam(team: String) : List<Match> = dbQuery(database) {
        Matches.select { Matches.awayTeam.upperCase() like "%${team.toUpperCase()}%" or (Matches.homeTeam.upperCase() like "%${team.toUpperCase()}%") }
            .map(this::toMatch)
            .toList()
    }

    override suspend fun getByCaster(caster: String) : List<Match> = dbQuery(database) {
        Matches.select { Matches.caster.upperCase() like "%${caster.toUpperCase()}%" }
            .map(this::toMatch)
            .toList()
    }
    
    override suspend fun getAll() : List<Match> = dbQuery(database) {
        Matches.selectAll()
            .map(this::toMatch)
            .toList()
    }

    override suspend fun clear() = dbActionQuery(database) {
        Matches.deleteAll()
    }

    private fun toMatch(row: ResultRow): Match = Match(
            season = row[Matches.season],
            circuit = row[Matches.circuit],
            division = row[Matches.division],
            conference = row[Matches.conference],
            week = row[Matches.week],
            awayTeam = row[Matches.awayTeam],
            homeTeam = row[Matches.homeTeam],
            colorScheme = row[Matches.color],
            date = row[Matches.date],
            caster = row[Matches.caster],
            coCasters = row[Matches.coCasters].split(","),
            streamLink = row[Matches.streamLink],
            vodLink = row[Matches.vodLink],
            awaySetsWon = row[Matches.awaySetsWon],
            homeSetsWon = row[Matches.homeSetsWon],
    )
}