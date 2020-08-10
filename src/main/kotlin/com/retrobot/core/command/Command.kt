package com.retrobot.core.command

import com.retrobot.core.Bot
import com.retrobot.core.util.removePrefixIgnoreCase
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

/**
 * Base class for all Discord Commands
 */
abstract class Command {
    abstract val label: String
    abstract val category: CommandCategory
    abstract val description: String
    abstract val usage: String

    open suspend fun handle(bot: Bot, event: GuildMessageReceivedEvent, message: String) : Boolean {
        return if (message.startsWith(label, true)) {
            val args = message.removePrefixIgnoreCase(label).trim()
            run(bot, event, args)
            true
        } else {
            false
        }
    }

    abstract suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String)
}