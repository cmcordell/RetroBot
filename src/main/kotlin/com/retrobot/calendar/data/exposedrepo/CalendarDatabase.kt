package com.retrobot.calendar.data.exposedrepo

import com.retrobot.core.data.exposed.Database
import org.h2.jdbcx.JdbcDataSource
import org.jetbrains.exposed.sql.Table
import javax.sql.DataSource


class CalendarDatabase : Database {

    override val name = "calendar"
    override val dataSource = buildDataSource() as DataSource
    override val tables = listOf(Calendars, Events)
    override val migrationsPath = "db/migrations/$name"


    private fun buildDataSource() = JdbcDataSource().apply {
        setUrl("jdbc:h2:~/$name;mode=MySQL")
        user = "root"
    }


    // ==================== Tables ====================
    object Calendars : Table("calendars") {
        val id = varchar("id", 36)
        val guildId = text("guild_id")
        val name = varchar("name", 32)
        override val primaryKey = PrimaryKey(guildId)
    }

    object Events : Table("events") {
        val id = varchar("id", 36)
        val calendarId = varchar("calendar_id", 36)
        val name = varchar("name", 32)
        val start = long("start_time")
        val end = long("end_time")
        val details = text("details")
        override val primaryKey = PrimaryKey(id)
    }
}