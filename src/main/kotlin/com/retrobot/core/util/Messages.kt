package com.retrobot.core.util

import com.retrobot.core.Emote.X
import com.retrobot.core.Messages.INCORRECT_COMMAND
import com.retrobot.core.Messages.MISSING_REQUIRED_COMMAND_ARGUMENT
import com.retrobot.core.Messages.MISSING_REQUIRED_COMMAND_ARGUMENTS
import com.retrobot.core.command.Command
import com.retrobot.core.domain.GuildSettings

/**
 * Utility functions to generate commonly used messages.
 */
object Messages {

    fun generateIncorrectCommandMessage(command: Command, guildSettings: GuildSettings) : String {
        return generateUsageErrorMessage(INCORRECT_COMMAND, command, guildSettings)
    }

    fun generateMissingCommandArgumentsMessage(arguments: List<String>, command: Command, guildSettings: GuildSettings) : String {
        val sb = StringBuilder()
        if (arguments.size == 1) {
            sb.append(MISSING_REQUIRED_COMMAND_ARGUMENT)
        } else {
            sb.append(MISSING_REQUIRED_COMMAND_ARGUMENTS)
        }
        sb.append(" ${Markdown.codeLine(generateDelimitedString(arguments))}")
        return generateUsageErrorMessage(sb.toString(), command, guildSettings)
    }

    fun generateUsageErrorMessage(errorMessage: String, command: Command, guildSettings: GuildSettings) : String {
        return "$errorMessage\n${command.usage}".formatGuildInfo(guildSettings)
    }

    fun generateBasicErrorMessage(errorMessage: String) : String {
        return "$X $errorMessage"
    }

    fun generateDelimitedString(items: List<String>, delimiter: String = ", ") : String {
        val sb = StringBuilder("")
        for (i in items.indices) {
            if (i > 0) {
                sb.append(delimiter)
            }
            sb.append(items[i])
        }
        return sb.toString()
    }
}