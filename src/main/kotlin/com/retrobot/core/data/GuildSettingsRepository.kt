package com.retrobot.core.data

import com.retrobot.core.domain.GuildSettings
import java.awt.Color

abstract class GuildSettingsRepository {
    abstract suspend fun putGuildSettings(guildSettings: GuildSettings)
    abstract suspend fun removeGuild(guildId: String)
    suspend fun getGuildSettings(guildId: String) = getOrCreateGuildSettings(guildId)
    protected abstract suspend fun getOrCreateGuildSettings(guildId: String): GuildSettings

    abstract suspend fun updateBotHighlightColor(guildId: String, color: Color)
    abstract suspend fun updateBotNickname(guildId: String, nickname: String)
    abstract suspend fun updateCommandPrefix(guildId: String, prefix: String)
    abstract suspend fun banFrom(guildId: String)
    abstract suspend fun unbanFrom(guildId: String)
}