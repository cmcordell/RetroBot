package com.retrobot.core.command

import com.retrobot.core.Bot
import com.retrobot.core.util.removePrefixIgnoreCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent


abstract class SubCommand {
    abstract val labels: Set<String>
    abstract val description: String
    abstract val usage: String

    open suspend fun handle(bot: Bot, event: GuildMessageReceivedEvent, args: String) : Boolean {
        for (label in labels) {
            if (args.startsWith(label, true)) {
                val subCommandArgs = args.removePrefixIgnoreCase(label).trim()
                GlobalScope.launch(Dispatchers.Default) { run(bot, event, subCommandArgs) }
                return true
            }
        }
        return false
    }

    abstract suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String)
}