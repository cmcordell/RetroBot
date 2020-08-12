package com.retrobot.core.data.exposedrepo

import com.retrobot.core.data.GuildSettingsRepository
import com.retrobot.core.util.dbActionQuery
import com.retrobot.core.util.dbQuery
import com.retrobot.core.util.hexString
import com.retrobot.core.util.upsert
import org.jetbrains.exposed.sql.*
import java.awt.Color
import com.retrobot.core.domain.GuildSettings as DomainGuildSettings

/**
 * A repository for Discord Guild settings implemented with Kotlin Exposed DSL
 */
class ExposedGuildSettingsRepository : GuildSettingsRepository() {

    override suspend fun putGuildSettings(guildSettings: DomainGuildSettings) = dbActionQuery {
        GuildSettings.upsert(GuildSettings.columns) { table ->
            table[id] = guildSettings.id
            table[commandPrefix] = guildSettings.commandPrefix
            table[botNickname] = guildSettings.botNickname
            table[botHighlightColor] = guildSettings.botHighlightColor.hexString()
            table[isBanned] = guildSettings.isBanned
        }
    }

    override suspend fun removeGuild(guildId: String) = dbActionQuery {
        GuildSettings.deleteWhere { GuildSettings.id eq guildId }
    }

    override suspend fun getOrCreateGuildSettings(guildId: String): DomainGuildSettings = dbQuery {
        var guildSettings = GuildSettings.select { GuildSettings.id eq guildId }
                .map(this::toGuildSettings)
                .firstOrNull()
        if (guildSettings == null) {
            guildSettings = DomainGuildSettings(guildId)
            putGuildSettings(guildSettings)
            guildSettings
        } else {
            guildSettings
        }
    }

    override suspend fun updateBotHighlightColor(guildId: String, color: Color) = dbActionQuery {
        GuildSettings.update({ GuildSettings.id eq guildId }) { table ->
            table[botHighlightColor] = color.hexString()
        }
    }

    override suspend fun updateBotNickname(guildId: String, nickname: String) = dbActionQuery {
        GuildSettings.update({ GuildSettings.id eq guildId }) { table ->
            table[botNickname] = nickname
        }
    }

    override suspend fun updateCommandPrefix(guildId: String, prefix: String) = dbActionQuery {
        GuildSettings.update({ GuildSettings.id eq guildId }) { table ->
            table[commandPrefix] = prefix
        }
    }

    override suspend fun banFrom(guildId: String) = dbActionQuery {
        GuildSettings.update({ GuildSettings.id eq guildId }) { table ->
            table[isBanned] = true
        }
    }

    override suspend fun unbanFrom(guildId: String) = dbActionQuery {
        GuildSettings.update({ GuildSettings.id eq guildId }) { table ->
            table[isBanned] = false
        }
    }


    object GuildSettings: Table("guild_settings") {
        val id = varchar("id", 50)
        val commandPrefix = text("command_prefix")
        val botNickname = varchar("bot_nickname", 32)
        val botHighlightColor = varchar("bot_highlight_color", 7)
        val isBanned = bool("is_banned")
        override val primaryKey = PrimaryKey(id)
    }

    private fun toGuildSettings(row: ResultRow): DomainGuildSettings = DomainGuildSettings(
            id = row[GuildSettings.id],
            commandPrefix = row[GuildSettings.commandPrefix],
            botNickname = row[GuildSettings.botNickname],
            botHighlightColor = Color.decode(row[GuildSettings.botHighlightColor]),
            isBanned = row[GuildSettings.isBanned]
    )
}