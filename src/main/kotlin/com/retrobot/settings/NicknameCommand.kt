package com.retrobot.settings

import com.retrobot.core.Bot
import com.retrobot.core.BotConfig
import com.retrobot.core.Commands.Settings.Nickname.ARG_NAME
import com.retrobot.core.Commands.Settings.Nickname.ARG_RESET
import com.retrobot.core.Commands.Settings.Nickname.CATEGORY
import com.retrobot.core.Commands.Settings.Nickname.COMMAND
import com.retrobot.core.Commands.Settings.Nickname.DESCRIPTION
import com.retrobot.core.Commands.Settings.Nickname.MAX_LENGTH
import com.retrobot.core.Commands.Settings.Nickname.MESSAGE_ERROR_NAME_TOO_LONG
import com.retrobot.core.Commands.Settings.Nickname.MESSAGE_RESET_SUCCESS
import com.retrobot.core.Commands.Settings.Nickname.MESSAGE_SET_SUCCESS
import com.retrobot.core.Commands.Settings.Nickname.USAGE
import com.retrobot.core.domain.command.Command
import com.retrobot.core.data.GuildSettingsRepository
import com.retrobot.core.data.exposedrepo.ExposedGuildSettingsRepository
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.util.Messages
import com.retrobot.core.util.formatGuildInfo
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.lang.String.format

/**
 * Nickname RetroBot
 */
class NicknameCommand : Command() {
    override val label = COMMAND
    override val category = CATEGORY
    override val description = DESCRIPTION
    override val usage = USAGE

    private val guildSettingsRepo: GuildSettingsRepository = ExposedGuildSettingsRepository()


    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        when {
            args.isEmpty() -> sendMissingArgumentMessage(event, guildSettings)
            nicknameIsTooLong(args) -> sendNicknameTooLongMessage(event)
            args.equals(ARG_RESET, true) -> resetNickname(event)
            else -> setNickname(event, args)
        }
    }

    private fun nicknameIsTooLong(name: String) : Boolean {
        return name.length > MAX_LENGTH
    }

    private suspend fun setNickname(event: GuildMessageReceivedEvent, nickname: String) {
        guildSettingsRepo.updateBotNickname(event.guild.id, nickname)
        val guildSettings = guildSettingsRepo.getGuildSettings(event.guild.id)
        event.guild.selfMember.modifyNickname(nickname).queue {
            event.channel.sendMessage(format(MESSAGE_SET_SUCCESS, nickname).formatGuildInfo(guildSettings)).queue()
        }
    }

    private suspend fun resetNickname(event: GuildMessageReceivedEvent) {
        guildSettingsRepo.updateBotNickname(event.guild.id, BotConfig.NAME)
        event.guild.selfMember.modifyNickname(BotConfig.NAME).queue {
            event.channel.sendMessage(MESSAGE_RESET_SUCCESS).queue()
        }
    }

    private suspend fun sendMissingArgumentMessage(event: GuildMessageReceivedEvent, guildSettings: GuildSettings) {
        event.channel.sendMessage(Messages.generateMissingCommandArgumentsMessage(listOf(ARG_NAME), this, guildSettings)).queue()
    }

    private fun sendNicknameTooLongMessage(event: GuildMessageReceivedEvent) {
        event.channel.sendMessage(Messages.generateBasicErrorMessage(MESSAGE_ERROR_NAME_TOO_LONG)).queue()
    }
}