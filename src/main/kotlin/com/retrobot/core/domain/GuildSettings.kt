package com.retrobot.core.domain

import com.retrobot.core.BotConfig
import java.awt.Color

data class GuildSettings(
        val id: String,
        val commandPrefix: String = "!",
        val botNickname: String = BotConfig.NAME,
        val botHighlightColor: Color = BotConfig.COLOR
)