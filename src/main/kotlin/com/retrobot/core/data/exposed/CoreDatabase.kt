package com.retrobot.core.data.exposed

import org.h2.jdbcx.JdbcDataSource
import org.jetbrains.exposed.sql.Table
import javax.sql.DataSource


class CoreDatabase : Database {
    override val name = "core"
    override val tables = listOf(GuildSettings)
    override val dataSource = buildDataSource() as DataSource
    override val migrationsPath = "db/migrations/$name"


    private fun buildDataSource() = JdbcDataSource().apply {
        setUrl("jdbc:h2:~/$name;mode=MySQL")
        user = "root"
    }


    // ==================== Tables ====================
    object GuildSettings: Table("guild_settings") {
        val id = varchar("id", 50)
        val commandPrefix = text("command_prefix")
        val botNickname = varchar("bot_nickname", 32)
        val botHighlightColor = varchar("bot_highlight_color", 7)
        override val primaryKey = PrimaryKey(id)
    }
}