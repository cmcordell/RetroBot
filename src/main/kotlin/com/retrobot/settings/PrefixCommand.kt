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
import com.retrobot.core.data.GuildSettingsRepository
import com.retrobot.core.data.exposedrepo.ExposedGuildSettingsRepository
import com.retrobot.core.domain.GuildSettings
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

    private val guildSettingsRepo: GuildSettingsRepository = ExposedGuildSettingsRepository()


    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        when {
            args.isEmpty() -> sendMissingArgumentMessage(event, guildSettings)
            args.contains(" ") -> sendPrefixCannotContainSpacesMessage(event, guildSettings)
            args.equals(ARG_RESET, true) -> resetPrefix(event)
            else -> setPrefix(event, args)
        }
    }

    private suspend fun setPrefix(event: GuildMessageReceivedEvent, prefix: String) {
        guildSettingsRepo.updateCommandPrefix(event.guild.id, prefix)
        event.channel.sendMessage(format(MESSAGE_SET_SUCCESS, prefix, prefix)).queue()
    }

    private suspend fun resetPrefix(event: GuildMessageReceivedEvent) {
        guildSettingsRepo.updateCommandPrefix(event.guild.id, BotConfig.PREFIX)
        event.channel.sendMessage(format(MESSAGE_RESET_SUCCESS, BotConfig.PREFIX)).queue()
    }

    private suspend fun sendMissingArgumentMessage(event: GuildMessageReceivedEvent, guildSettings: GuildSettings) {
        event.channel.sendMessage(Messages.generateMissingCommandArgumentsMessage(listOf(ARG_PREFIX), this, guildSettings)).queue()
    }

    private suspend fun sendPrefixCannotContainSpacesMessage(event: GuildMessageReceivedEvent, guildSettings: GuildSettings) {
        event.channel.sendMessage(Messages.generateUsageErrorMessage("Prefix cannot contain spaces.", this, guildSettings)).queue()
    }
}