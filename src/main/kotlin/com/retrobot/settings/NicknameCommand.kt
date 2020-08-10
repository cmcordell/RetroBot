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
import com.retrobot.core.command.Command
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


    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String) {
        when {
            args.isEmpty() -> sendMissingArgumentMessage(bot, event)
            nicknameIsTooLong(args) -> sendNicknameTooLongMessage(event)
            args.equals(ARG_RESET, true) -> resetNickname(bot, event)
            else -> setNickname(bot, event, args)
        }
    }

    private fun nicknameIsTooLong(name: String) : Boolean {
        return name.length > MAX_LENGTH
    }

    private suspend fun setNickname(bot: Bot, event: GuildMessageReceivedEvent, nickname: String) {
        bot.guildSettingsRepo.updateBotNickname(event.guild.id, nickname)
        val guildSettings = bot.guildSettingsRepo.getGuildSettings(event.guild.id)
        event.guild.selfMember.modifyNickname(nickname).queue {
            event.channel.sendMessage(format(MESSAGE_SET_SUCCESS, nickname).formatGuildInfo(guildSettings)).queue()
        }
    }

    private suspend fun resetNickname(bot: Bot, event: GuildMessageReceivedEvent) {
        bot.guildSettingsRepo.updateBotNickname(event.guild.id, BotConfig.NAME)
        event.guild.selfMember.modifyNickname(BotConfig.NAME).queue {
            event.channel.sendMessage(MESSAGE_RESET_SUCCESS).queue()
        }
    }

    private suspend fun sendMissingArgumentMessage(bot: Bot, event: GuildMessageReceivedEvent) {
        val guildSettings = bot.guildSettingsRepo.getGuildSettings(event.guild.id)
        event.channel.sendMessage(Messages.generateMissingCommandArgumentsMessage(listOf(ARG_NAME), this, guildSettings)).queue()
    }

    private fun sendNicknameTooLongMessage(event: GuildMessageReceivedEvent) {
        event.channel.sendMessage(Messages.generateBasicErrorMessage(MESSAGE_ERROR_NAME_TOO_LONG)).queue()
    }
}