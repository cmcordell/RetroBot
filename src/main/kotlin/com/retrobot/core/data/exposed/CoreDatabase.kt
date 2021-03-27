package com.retrobot.core.data.exposed

import org.h2.jdbcx.JdbcDataSource
import org.jetbrains.exposed.sql.Table
import javax.sql.DataSource


class CoreDatabase : Database {
    override val name = "core"
    override val tables = listOf(GuildSettings)
    override val dataSource = dataSource() as DataSource
    override val migrationsPath = "db/migrations/$name"

    private fun dataSource() = JdbcDataSource().apply {
        setUrl("jdbc:h2:~/$name;mode=MySQL")
        user = "root"
//        setUrl("jdbc:postgresql://ec2-54-172-219-218.compute-1.amazonaws.com:5432/d5pdr833uq9o0u")
//        user = "ezbhujjyvnivdt"
//        password = "b9e45d3e891c99ccb61bc004c556f166e4a102fdb6654ef387f85e384c4edb54"
//        ssl = true
//        sslMode = "require"
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