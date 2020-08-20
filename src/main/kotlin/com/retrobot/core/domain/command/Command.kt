package com.retrobot.core.domain.command

import com.retrobot.core.Bot
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.util.removePrefixIgnoreCase
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.koin.core.KoinComponent

/**
 * Base class for all Discord Commands
 */
abstract class Command : KoinComponent {
    abstract val label: String
    abstract val category: CommandCategory
    abstract val description: String
    abstract val usage: String

    abstract suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings)
}