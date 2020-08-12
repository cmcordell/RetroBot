package com.retrobot.core.command

import com.retrobot.core.Bot
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.util.removePrefixIgnoreCase
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

/**
 * Base class for [Command] sub commands.
 */
abstract class SubCommand {
    abstract val labels: Set<String>
    abstract val description: String
    abstract val usage: String

    suspend fun handle(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) : Boolean {
        for (label in labels) {
            if (args.startsWith(label, true)) {
                val subCommandArgs = args.removePrefixIgnoreCase(label).trim()
                run(bot, event, subCommandArgs, guildSettings)
                return true
            }
        }
        return false
    }

    abstract suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings)
}