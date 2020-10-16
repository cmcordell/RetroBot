package com.retrobot.core.data.exposed

import com.retrobot.core.data.GuildSettingsRepository
import com.retrobot.core.data.cache.Cache
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.util.dbActionQuery
import com.retrobot.core.util.dbQuery
import com.retrobot.core.util.hexString
import com.retrobot.core.util.upsert
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import java.awt.Color
import com.retrobot.core.data.exposed.CoreDatabase.GuildSettings as DbGuildSettings

/**
 * A repository for Discord Guild settings implemented with Kotlin Exposed DSL
 */
class ExposedGuildSettingsRepository(
        coreDatabase: CoreDatabase,
        private val guildSettingsCache: Cache<String, GuildSettings>
) : GuildSettingsRepository {

    private val database = Database.connect(coreDatabase.dataSource)

    override suspend fun put(guildSettings: GuildSettings) {
        guildSettingsCache[guildSettings.id] = guildSettings
        update(guildSettings)
    }

    override suspend fun remove(guildId: String) = dbActionQuery(database) {
        guildSettingsCache.remove(guildId)
        dbActionQuery(database) {
            DbGuildSettings.deleteWhere { DbGuildSettings.id eq guildId }
        }
    }

    override suspend fun get(guildId: String): GuildSettings {
        val cachedGuildSettings = guildSettingsCache[guildId]
        return if (cachedGuildSettings == null) {
            val guildSettings = getOrCreate(guildId)
            guildSettingsCache[guildSettings.id] = guildSettings
            guildSettings
        } else {
            cachedGuildSettings
        }
    }

    override suspend fun updateBotHighlightColor(guildId: String, color: Color) {
        val guildSettings = get(guildId).copy(botHighlightColor = color)
        guildSettingsCache[guildSettings.id] = guildSettings
        update(guildSettings)
    }

    override suspend fun updateBotNickname(guildId: String, nickname: String) {
        val guildSettings = get(guildId).copy(botNickname = nickname)
        guildSettingsCache[guildSettings.id] = guildSettings
        update(guildSettings)
    }

    override suspend fun updateCommandPrefix(guildId: String, prefix: String) {
        val guildSettings = get(guildId).copy(commandPrefix = prefix)
        guildSettingsCache[guildSettings.id] = guildSettings
        update(guildSettings)
    }

    private suspend fun getOrCreate(guildId: String): GuildSettings {
        var guildSettings = dbQuery(database) {
            DbGuildSettings.select { DbGuildSettings.id eq guildId }
                    .map(this::toGuildSettings)
                    .firstOrNull()
        }
        if (guildSettings == null) {
            guildSettings = GuildSettings(guildId)
            update(guildSettings)
        }
        return guildSettings
    }

    private suspend fun update(guildSettings: GuildSettings) = dbActionQuery(database) {
        DbGuildSettings.upsert(DbGuildSettings.columns) { table ->
            table[id] = guildSettings.id
            table[commandPrefix] = guildSettings.commandPrefix
            table[botNickname] = guildSettings.botNickname
            table[botHighlightColor] = guildSettings.botHighlightColor.hexString()
        }
    }

    private fun toGuildSettings(row: ResultRow): GuildSettings = GuildSettings(
            id = row[DbGuildSettings.id],
            commandPrefix = row[DbGuildSettings.commandPrefix],
            botNickname = row[DbGuildSettings.botNickname],
            botHighlightColor = Color.decode(row[DbGuildSettings.botHighlightColor])
    )
}