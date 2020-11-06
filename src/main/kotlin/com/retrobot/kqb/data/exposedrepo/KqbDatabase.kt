package com.retrobot.kqb.data.exposedrepo

import com.retrobot.core.data.exposed.Database
import com.retrobot.core.domain.command.Command
import com.retrobot.kqb.domain.model.AwardType
import com.retrobot.kqb.domain.model.ColorScheme
import org.h2.jdbcx.JdbcDataSource
import org.jetbrains.exposed.sql.Table
import javax.sql.DataSource

/**
 * [Database] for KQB related [Command]s
 */
class KqbDatabase : Database {

    override val name = "kqb"
    override val dataSource = buildDataSource() as DataSource
    override val tables = listOf(Awards, Casters, Matches, Teams)
    override val migrationsPath = "db/migrations/$name"


    private fun buildDataSource() = JdbcDataSource().apply {
        setUrl("jdbc:h2:~/$name;mode=MySQL")
        user = "root"
    }


// ==================== Tables ====================
    object Awards: Table("awards") {
        val awardType = enumeration("award_type", AwardType::class)
        val season = varchar("season", 10)
        val circuit = varchar("circuit", 10)
        val division = varchar("division", 10)
        val conference = varchar("conference", 10)
        val week = varchar("week", 20)
        val player = varchar("player", 100)
        val stats = text("stats")
        override val primaryKey = PrimaryKey(season, circuit, division, conference, week, awardType, player)
    }

    object Casters: Table("casters") {
        val name = varchar("name", 50)
        val streamLink = text("stream_link")
        val bio = text("bio")
        val gamesCasted = integer("games_casted")
        override val primaryKey = PrimaryKey(name)
    }

    object Matches: Table("matches") {
        val id = integer("id").autoIncrement()
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
        override val primaryKey = PrimaryKey(id)
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
        val playoffSeed = integer("playoff_seed")
        val infoLink = text("info_link")
        override val primaryKey = PrimaryKey(season, circuit, division, conference, name)
    }
}