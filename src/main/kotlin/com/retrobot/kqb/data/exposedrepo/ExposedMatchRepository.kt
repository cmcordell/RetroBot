package com.retrobot.kqb.data.exposedrepo

import com.retrobot.core.util.*
import com.retrobot.kqb.data.MatchRepository
import com.retrobot.kqb.domain.model.ColorScheme
import com.retrobot.kqb.domain.model.Match
import org.jetbrains.exposed.sql.*

/**
 * KQB Match Repository implement with Kotlin Exposed DSL
 */
class ExposedMatchRepository : MatchRepository {

    override suspend fun put(match: Match) = dbActionQuery {
        Matches.upsert(Matches.columns) { table ->
            table[id] = match.id
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
            table[winner] = match.winner
        }
    }

    override suspend fun put(matches: Set<Match>) = dbActionQuery {
        Matches.batchUpsert(matches, Matches.columns, true) { batch, match ->
            batch[id] = match.id
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
            batch[winner] = match.winner
        }
    }

    override suspend fun getByWeekAndTeams(week: String, awayTeam: String, homeTeam: String): Match? = dbQuery {
        Matches.select { Matches.week.upperCase() like "%${week.toUpperCase()}%" }
                .andWhere { Matches.awayTeam.upperCase() like "%${awayTeam.toUpperCase()}%" }
                .andWhere { Matches.homeTeam.upperCase() like "%${homeTeam.toUpperCase()}%" }
                .map(this::toMatch)
                .firstOrNull()
    }

    override suspend fun getByCircuit(circuit: String, division: String, conference: String) : Set<Match> = dbQuery {
        Matches.select { Matches.circuit.upperCase() like "%${circuit.toUpperCase()}%" }
                .andWhere { Matches.division.upperCase() like "%${division.toUpperCase()}%" }
                .andWhere { Matches.conference.upperCase() like "%${conference.toUpperCase()}%" }
                .map(this::toMatch)
                .toSet()
    }

    override suspend fun getByDate(from: Long, to: Long) : Set<Match> = dbQuery {
        Matches.select { Matches.date greaterEq from }
                .andWhere { Matches.date lessEq to }
                .map(this::toMatch)
                .toSet()
    }

    override suspend fun getByWeek(week: String) : Set<Match> = dbQuery {
        Matches.select { Matches.week.upperCase() like "%${week.toUpperCase()}%" }
            .map(this::toMatch)
            .toSet()
    }

    override suspend fun getByTeam(team: String) : Set<Match> = dbQuery {
        Matches.select { Matches.awayTeam.upperCase() like "%${team.toUpperCase()}%" or (Matches.homeTeam.upperCase() like "%${team.toUpperCase()}%") }
            .map(this::toMatch)
            .toSet()
    }

    override suspend fun getByCaster(caster: String) : Set<Match> = dbQuery {
        Matches.select { Matches.caster.upperCase() like "%${caster.toUpperCase()}%" }
            .map(this::toMatch)
            .toSet()
    }
    
    override suspend fun getAll() : Set<Match> = dbQuery {
        Matches.selectAll()
            .map(this::toMatch)
            .toSet()
    }

    override suspend fun clear() = dbActionQuery {
        Matches.deleteAll()
    }


    object Matches: Table("matches") {
        val id = varchar("id", 50)
        val season = varchar("season", 10)
        val circuit = varchar("circuit", 10)
        val division = varchar("division", 10)
        val conference = varchar("conference", 10)
        val week = varchar("week", 20)
        val awayTeam = varchar("away_team", 100)
        val homeTeam = varchar("home_team", 100)
        val color = enumeration("color", ColorScheme::class)
        val date = long("date")
        val caster = varchar("caster", 50)
        val coCasters = text("co_casters")
        val streamLink = text("stream_link")
        val vodLink = text("vod_link")
        val awaySetsWon = integer("away_sets_won")
        val homeSetsWon = integer("home_sets_won")
        val winner = varchar("winner", 100)
        override val primaryKey = PrimaryKey(id)
    }

    private fun toMatch(row: ResultRow): Match = Match(
            id = row[Matches.id],
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
            winner = row[Matches.winner]
    )
}