package com.retrobot.kqb.data.exposedrepo

import com.retrobot.core.util.*
import com.retrobot.kqb.data.TeamRepository
import com.retrobot.kqb.domain.Team
import org.jetbrains.exposed.sql.*

/**
 * KQB Team Repository implement with Kotlin Exposed DSL
 */
class ExposedTeamRepository : TeamRepository {

//    init {
//        GlobalScope.launch(Dispatchers.IO) {
//            Database.connect(DatabaseFactory.h2())
//        }
//    }


    override suspend fun put(team: Team) = dbActionQuery {
        Teams.upsert(Teams.columns) { table ->
            table[name] = team.name
            table[captain] = team.captain
            table[members] = team.members.toDelimitedString(",")
            table[season] = team.season
            table[circuit] = team.circuit
            table[division] = team.division
            table[conference] = team.conference
            table[matchesWon] = team.matchesWon
            table[matchesLost] = team.matchesLost
            table[matchesPlayed] = team.matchesPlayed
            table[setsWon] = team.setsWon
            table[setsLost] = team.setsLost
            table[setsPlayed] = team.setsPlayed
        }
    }

    override suspend fun put(teams: Set<Team>) = dbActionQuery {
        Teams.batchUpsert(teams, Teams.columns) { batch, team ->
            batch[name] = team.name
            batch[captain] = team.captain
            batch[members] = team.members.toDelimitedString(",")
            batch[season] = team.season
            batch[circuit] = team.circuit
            batch[division] = team.division
            batch[conference] = team.conference
            batch[matchesWon] = team.matchesWon
            batch[matchesLost] = team.matchesLost
            batch[matchesPlayed] = team.matchesPlayed
            batch[setsWon] = team.setsWon
            batch[setsLost] = team.setsLost
            batch[setsPlayed] = team.setsPlayed
        }
    }

    override suspend fun getByCircuit(circuit: String, division: String, conference: String) : Set<Team> = dbQuery {
        Teams.select { Teams.circuit.upperCase() like "%${circuit.toUpperCase()}%" }
            .andWhere { Teams.division.upperCase() like "%${division.toUpperCase()}%" }
            .andWhere { Teams.conference.upperCase() like "%${conference.toUpperCase()}%" }
            .map(this::toTeam)
            .toSet()
    }

    override suspend fun getByName(name: String) : Set<Team> = dbQuery {
        Teams.select { Teams.name.upperCase() like "%${name.toUpperCase()}%" }
            .map(this::toTeam)
            .toSet()
    }

    override suspend fun getByMember(name: String) : Set<Team> = dbQuery {
        Teams.select { Teams.members.upperCase() like "%${name.toUpperCase()}%" }
            .map(this::toTeam)
            .toSet()
    }

    override suspend fun getAll() : Set<Team> = dbQuery {
        Teams.selectAll()
            .map(this::toTeam)
            .toSet()
    }

    override suspend fun clear() = dbActionQuery {
        Teams.deleteAll()
    }


    object Teams: Table("teams") {
        val name = varchar("name", 100)
        val captain = varchar("captain", 100)
        val members = text("members")
        val season = varchar("season", 10)
        val circuit = varchar("circuit", 10)
        val division = varchar("division", 10)
        val conference = varchar("conference", 10)
        val matchesWon = integer("matches_won")
        val matchesLost = integer("matches_lost")
        val matchesPlayed = integer("matches_played")
        val setsWon = integer("sets_won")
        val setsLost = integer("sets_lost")
        val setsPlayed = integer("sets_played")
        override val primaryKey = PrimaryKey(season, circuit, division, conference, name)
    }

    private fun toTeam(row: ResultRow): Team = Team(
        name = row[Teams.name],
        captain = row[Teams.captain],
        members = row[Teams.members].split(","),
        season = row[Teams.season],
        circuit = row[Teams.circuit],
        division = row[Teams.division],
        conference = row[Teams.conference],
        matchesWon = row[Teams.matchesWon],
        matchesLost = row[Teams.matchesLost],
        matchesPlayed = row[Teams.matchesPlayed],
        setsWon = row[Teams.setsWon],
        setsLost = row[Teams.setsLost],
        setsPlayed = row[Teams.setsPlayed]
    )
}