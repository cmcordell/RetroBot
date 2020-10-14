package com.retrobot.core.data

import com.retrobot.core.domain.GuildSettings
import java.awt.Color

interface GuildSettingsRepository {
    suspend fun put(guildSettings: GuildSettings)
    suspend fun remove(guildId: String)
    suspend fun get(guildId: String): GuildSettings

    suspend fun updateBotHighlightColor(guildId: String, color: Color)
    suspend fun updateBotNickname(guildId: String, nickname: String)
    suspend fun updateCommandPrefix(guildId: String, prefix: String)
}