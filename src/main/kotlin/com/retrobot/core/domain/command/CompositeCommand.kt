package com.retrobot.core.domain.command

import com.retrobot.core.Bot
import com.retrobot.core.Discord.Markdown.OP_BOLD
import com.retrobot.core.Discord.Markdown.OP_QUOTE_BLOCK
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.util.formatGuildInfo
import com.retrobot.core.util.toDelimitedString
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

/**
 * A [Command] that contains [SubCommand]s.
 *
 * e.g. !kqb teams
 * The CompositeCommand "!kqb" has SubCommand "teams"
 */
abstract class CompositeCommand: Command() {
    abstract val subCommands: List<SubCommand>

    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        for (subCommand in subCommands) {
            if (subCommand.handle(bot, event, args, guildSettings)) return
        }

        val returnMessage = EmbedBuilder()
                .setColor(guildSettings.botHighlightColor)
                .setDescription(getCommandHelpInfo().formatGuildInfo(guildSettings))
                .build()
        event.channel.sendMessage(returnMessage).queue()
    }

    override fun getCommandHelpInfo(): String {
        return "$OP_BOLD$label$OP_BOLD\n" +
                "$description\n" +
                "$OP_QUOTE_BLOCK${getSubCommandHelpInfo()}"
    }

    private fun getSubCommandHelpInfo(): String {
        return subCommands.filter { it.usage.isNotEmpty() }.toDelimitedString("\n") { subCommand ->
            "$OP_BOLD${subCommand.labels.toDelimitedString()}$OP_BOLD\n" +
                    "${subCommand.description}\n" +
                    "${subCommand.usage}\n"
        }
    }
}