package com.retrobot.utility

import com.retrobot.core.Bot
import com.retrobot.core.Commands.Utils.Ping.CATEGORY
import com.retrobot.core.Commands.Utils.Ping.COMMAND
import com.retrobot.core.Commands.Utils.Ping.DESCRIPTION
import com.retrobot.core.Commands.Utils.Ping.USAGE
import com.retrobot.core.Emote.INBOX_TRAY
import com.retrobot.core.Emote.OUTBOX_TRAY
import com.retrobot.core.domain.command.Command
import com.retrobot.core.domain.GuildSettings
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

/**
 * Ping
 */
class PingCommand : Command() {
    override val label = COMMAND
    override val category = CATEGORY
    override val description = DESCRIPTION
    override val usage = USAGE

    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        val start = System.currentTimeMillis()

        event.channel.sendMessage("$OUTBOX_TRAY Checking ping...").queue { message ->
            val pingTime = System.currentTimeMillis() - start
            message.editMessage("$INBOX_TRAY Ping is ${pingTime}ms").queue()
        }
    }
}