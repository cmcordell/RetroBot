package com.retrobot.settings

import com.retrobot.core.Bot
import com.retrobot.core.BotConfig
import com.retrobot.core.Commands
import com.retrobot.core.Commands.Settings.Color.ARG_COLOR_HEX
import com.retrobot.core.Commands.Settings.Color.ARG_COLOR_VALUE
import com.retrobot.core.Commands.Settings.Color.CATEGORY
import com.retrobot.core.Commands.Settings.Color.COMMAND
import com.retrobot.core.Commands.Settings.Color.DESCRIPTION
import com.retrobot.core.Commands.Settings.Color.MESSAGE_RESET_SUCCESS
import com.retrobot.core.Commands.Settings.Color.USAGE
import com.retrobot.core.command.Command
import com.retrobot.core.data.GuildSettingsRepository
import com.retrobot.core.data.exposedrepo.ExposedGuildSettingsRepository
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.util.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.awt.Color
import java.util.regex.Pattern

/**
 * Change RetroBot's highlight color
 */
class ColorCommand : Command() {
    override val label = COMMAND
    override val category = CATEGORY
    override val description = DESCRIPTION
    override val usage = USAGE

    private val REGEX_HEX_COLOR = "^#([A-Fa-f0-9]{6})"
    private val PATTERN_HEX_COLOR = Pattern.compile(REGEX_HEX_COLOR)
    private val COLOR_MAP = caseInsensitiveTreeMapOf(
        "Black" to Color.BLACK,
        "Blue" to Color.BLUE,
        "Bronze" to Color(205, 127, 50),
        "Brown" to Color(165, 42, 42),
        "Crimson" to Color(220, 20, 60),
        "Cyan" to Color.CYAN,
        "DarkGray" to Color.DARK_GRAY,
        "Gold" to Color(255, 215, 0),
        "Gray" to Color.GRAY,
        "Green" to Color.GREEN,
        "LightGray" to Color.LIGHT_GRAY,
        "Chartreuse" to Color(127, 255, 0),
        "Magenta" to Color.MAGENTA,
        "Maroon" to Color(128, 0, 0),
        "Navy" to Color(0, 0, 128),
        "Orange" to Color.ORANGE,
        "Pink" to Color.PINK,
        "Purple" to Color(128, 0, 128),
        "Red" to Color.RED,
        "Silver" to Color(192, 192, 192),
        "Violet" to Color(238, 130, 238),
        "White" to Color.WHITE,
        "Yellow" to Color.YELLOW
    )

    private val guildSettingsRepo: GuildSettingsRepository = ExposedGuildSettingsRepository()


    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        val returnMessage = when {
            args.isEmpty() -> buildMissingArgumentMessage(guildSettings)
            args.equals(Commands.Settings.Prefix.ARG_RESET, true) -> resetColor(event.guild.id)
            isHexString(args) -> handleHexColor(event.guild.id, args)
            else -> handleTextColor(event.guild.id, args)
        }

        event.channel.sendMessage(returnMessage).queue()
    }

    private suspend fun resetColor(guildId: String): Message {
        guildSettingsRepo.updateBotHighlightColor(guildId, BotConfig.COLOR)
        return EmbedBuilder()
                .setColor(BotConfig.COLOR)
                .setTitle(MESSAGE_RESET_SUCCESS)
                .buildMessage()
    }

    private fun buildMissingArgumentMessage(guildSettings: GuildSettings): Message {
        val content = Messages.generateMissingCommandArgumentsMessage(listOf(ARG_COLOR_VALUE, ARG_COLOR_HEX), this, guildSettings)
        return MessageBuilder()
                .setContent(content)
                .build()
    }

    private fun isHexString(value: String) : Boolean {
        return value.trim().startsWith("#")
    }

    private fun isValidHexColor(color: String) : Boolean {
        return PATTERN_HEX_COLOR.matcher(color).matches()
    }

    private suspend fun handleHexColor(guildId: String, color: String) : Message {
        return if (isValidHexColor(color)) {
            val actualColor = Color.decode(color)
            guildSettingsRepo.updateBotHighlightColor(guildId, actualColor)
            EmbedBuilder()
                    .setColor(actualColor)
                    .setTitle("My color has been set to $color.")
                    .buildMessage()
        } else {
            MessageBuilder()
                    .setContent(Messages.generateBasicErrorMessage("The Hex code provided was not a valid color."))
                    .build()
        }
    }

    private suspend fun handleTextColor(guildId: String, color: String) : Message {
        val actualColor = COLOR_MAP[color]
        return if (actualColor != null) {
            guildSettingsRepo.updateBotHighlightColor(guildId, actualColor)
            EmbedBuilder()
                    .setColor(actualColor)
                    .setTitle("My color has been set to $color.")
                    .buildMessage()
        } else {
            val description = "The Color Name provided was not a valid color.\n" +
                    "Please see below for all valid color names:\n" +
                    Markdown.quoteBlock(COLOR_MAP.keys.toDelimitedString("\n"))
            val errorMessage = Messages.generateBasicErrorMessage(description)
            MessageBuilder()
                    .setContent(errorMessage)
                    .build()
        }
    }
}