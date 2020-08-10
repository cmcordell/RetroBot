package com.retrobot.settings

import com.retrobot.core.Bot
import com.retrobot.core.BotConfig
import com.retrobot.core.Commands.Settings.Prefix.ARG_PREFIX
import com.retrobot.core.Commands.Settings.Prefix.ARG_RESET
import com.retrobot.core.Commands.Settings.Prefix.CATEGORY
import com.retrobot.core.Commands.Settings.Prefix.COMMAND
import com.retrobot.core.Commands.Settings.Prefix.DESCRIPTION
import com.retrobot.core.Commands.Settings.Prefix.MESSAGE_RESET_SUCCESS
import com.retrobot.core.Commands.Settings.Prefix.MESSAGE_SET_SUCCESS
import com.retrobot.core.Commands.Settings.Prefix.USAGE
import com.retrobot.core.command.Command
import com.retrobot.core.util.Messages
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.lang.String.format

/**
 * Change RetroBot's command prefix
 */
class PrefixCommand : Command() {
    override val label = COMMAND
    override val category = CATEGORY
    override val description = DESCRIPTION
    override val usage = USAGE


    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String) {
        when {
            args.isEmpty() -> sendMissingArgumentMessage(bot, event)
            args.contains(" ") -> sendPrefixCannotContainSpacesMessage(bot, event)
            args.equals(ARG_RESET, true) -> resetPrefix(bot, event)
            else -> setPrefix(bot, event, args)
        }
    }

    private suspend fun setPrefix(bot: Bot, event: GuildMessageReceivedEvent, prefix: String) {
        bot.guildSettingsRepo.updateCommandPrefix(event.guild.id, prefix)
        event.channel.sendMessage(format(MESSAGE_SET_SUCCESS, prefix, prefix)).queue()
    }

    private suspend fun resetPrefix(bot: Bot, event: GuildMessageReceivedEvent) {
        bot.guildSettingsRepo.updateCommandPrefix(event.guild.id, BotConfig.PREFIX)
        event.channel.sendMessage(format(MESSAGE_RESET_SUCCESS, BotConfig.PREFIX)).queue()
    }

    private suspend fun sendMissingArgumentMessage(bot: Bot, event: GuildMessageReceivedEvent) {
        val guildSettings = bot.guildSettingsRepo.getGuildSettings(event.guild.id)
        event.channel.sendMessage(Messages.generateMissingCommandArgumentsMessage(listOf(ARG_PREFIX), this, guildSettings)).queue()
    }

    private suspend fun sendPrefixCannotContainSpacesMessage(bot: Bot, event: GuildMessageReceivedEvent) {
        val guildSettings = bot.guildSettingsRepo.getGuildSettings(event.guild.id)
        event.channel.sendMessage(Messages.generateUsageErrorMessage("Prefix cannot contain spaces.", this, guildSettings)).queue()
    }
}