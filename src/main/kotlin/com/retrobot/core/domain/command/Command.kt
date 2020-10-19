package com.retrobot.core.domain.command

import com.retrobot.core.Bot
import com.retrobot.core.Discord.Markdown.OP_BOLD
import com.retrobot.core.domain.GuildSettings
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

    open fun getCommandHelpInfo(): String {
        return "$OP_BOLD$label$OP_BOLD\n" +
                "$description\n" +
                "$usage\n"
    }
}