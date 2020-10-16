package com.retrobot.core

import com.retrobot.core.Discord.Markdown.OP_BOLD
import com.retrobot.core.Discord.Markdown.OP_CODE_LINE
import com.retrobot.core.Discord.Markdown.OP_ITALIC
import com.retrobot.core.Discord.Markdown.OP_QUOTE_LINE
import com.retrobot.core.Emote.QUESTION_MARK
import com.retrobot.core.Emote.X
import com.retrobot.core.domain.command.CommandCategory
import java.awt.Color


/*
 * TODO Features
 *  - Add cooldown options for specific commands
 *  - KQB Add starting soon notifications for matches
 *  - Twitch API - add starting soon notifications for specific streamers
 *  - Twitch API - Map out watching stream with chat in discord
 *  - Message Update Services: On message delete, remove service
 *  - More MessageUpdateServices, Standings, Casters
 *  - Long term subscribe for MessageUpdateServices
 *  - Notifications subscriptions (PM or a specific TextChannel) for upcoming matches by Caster, Team, Player, or all
 *  - Direct Message Commands
 *  - GuildSettings: Set main timezone, other timezones
 *  - Add Bot Sharding
 */

/*
 * TODO Code Improvements
 *  - Remove all try/catch blocks according to https://medium.com/@elizarov/kotlin-and-exceptions-8062f589d07
 *  - Add middle interactor/use case layer
 *  - Find a better way to send Message without a JDA Event
 *  - Refactor for Dependency Injection (Too many objects/Singletons, too many hardcoded dependencies)
 *  - Write test coverage
 *  - Revisit Twemoji for JumboCommand (Would allow us to decrease .jar size by ~10MB
 *  - Find more ways to decrease .jar size
 *  - Add analytics
 */

const val SPECIFIER_BOT_NAME = "&BOT_NAME"
const val SPECIFIER_COMMAND_PREFIX = "&COMMAND_PREFIX"
        
enum class BuildType {
    DEBUG, RELEASE
}

object BotConfig {
    const val NAME = "RetroBot"
    const val PREFIX = "!" // Default Prefix for commands
    val COLOR: Color = Colors.RETROBOT_PRIMARY // Default Bot highlight color for commands
}

object Colors {
    val RETROBOT_PRIMARY = Color(43, 198, 250)
    val TWITCH_PRIMARY = Color(100, 65, 164)
}

object Commands {
    object KQB {
        const val TITLE = "Killer Queen Black"
        const val DESCRIPTION = "Commands related the game Killer Queen Black."
        val ALIASES = listOf("Killer Queen Black", "Killer Queen", "KillerQueen", "KillerQueenBlack", "KQB", "KQ")

        object Competition {
            const val COMMAND = "kqb"
            val CATEGORY = CommandCategory.KILLER_QUEEN
            const val DESCRIPTION = "Get competition info for KQB."
            const val USAGE = "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND casters\n" +
                    "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND caster <caster name>\n" +
                    "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND matches\n" +
                    "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND player <player name (full)>\n" +
                    "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND teams\n" +
                    "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND team <team name>\n" +
                    "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND standings\n" +
                    "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND standings <circuit>, <division>, <conference>"
            const val MESSAGE_INFO = "$OP_BOLD$SPECIFIER_COMMAND_PREFIX$COMMAND$OP_BOLD\n" +
                    "$DESCRIPTION\n" +
                    USAGE
        }
    }

    object PollsAndEvents {
        const val TITLE = "Polls and Events"
        const val DESCRIPTION = "Commands related to managing polls and events."
        val ALIASES = listOf("Poll", "Polls", "Event", "Events", "Polls and Events")

        object Poll {
            const val COMMAND = "poll"
            val CATEGORY = CommandCategory.POLL_OR_EVENT
            const val DESCRIPTION = "Create a poll."
            const val USAGE = "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND [Favorite Color] Red, Blue, Green"
        }
    }

    object Settings {
        const val TITLE = "Settings"
        const val DESCRIPTION = "Commands related to changing $SPECIFIER_BOT_NAME's settings."
        val ALIASES = listOf("Setting", "Settings", "Option", "Options")

        object Color {
            const val COMMAND = "color"
            val CATEGORY = CommandCategory.SETTING
            const val DESCRIPTION = "Change $SPECIFIER_BOT_NAME's highlight color."
            const val USAGE = "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND <color name>, i.e. Blue, Red, Green\n" +
                    "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND <color hex>, i.e #954E4C\n" +
                    "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND reset"
            const val ARG_COLOR_VALUE = "color value"
            const val ARG_COLOR_HEX = "color hex"
            const val MESSAGE_RESET_SUCCESS = "My color has been reset to its default."
        }

        object Nickname {
            const val COMMAND = "nickname"
            val CATEGORY = CommandCategory.SETTING
            const val DESCRIPTION = "Change $SPECIFIER_BOT_NAME's nickname."
            const val ARG_NAME = "name"
            const val ARG_RESET = "reset"
            const val USAGE = "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND <$ARG_NAME>\n" +
                    "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND reset"
            const val MAX_LENGTH = 32
            const val MESSAGE_SET_SUCCESS = "My nickname has been changed to %s.\n" +
                    "Use the command $OP_CODE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND $ARG_RESET$OP_CODE_LINE to reset my original nickname."
            const val MESSAGE_RESET_SUCCESS = "My nickname has been reset to ${BotConfig.NAME}."
            const val MESSAGE_ERROR_NAME_TOO_LONG = "Nickname must be $MAX_LENGTH characters or less and not contain any Custom Emotes."
        }

        object Prefix {
            const val COMMAND = "prefix"
            val CATEGORY = CommandCategory.SETTING
            const val DESCRIPTION = "Change $SPECIFIER_BOT_NAME's command prefix."
            const val USAGE = "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND <Prefix>\n" +
                    "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND reset"
            const val ARG_PREFIX = "prefix"
            const val ARG_RESET = "reset"
            const val MESSAGE_SET_SUCCESS = "My prefix has been changed to \"%s\".\n" +
                    "Use the command $OP_CODE_LINE%s$COMMAND $ARG_RESET$OP_CODE_LINE to reset my original prefix."
            const val MESSAGE_RESET_SUCCESS = "My prefix has been reset to ${BotConfig.PREFIX}."
        }
    }

    object Utils {
        const val TITLE = "Utilities"
        const val DESCRIPTION = "Utility commands like !coinflip and !jumbo."
        val ALIASES = listOf("Utilities", "Util", "Utils", "Utility", "Tool", "Tools")

        object Coinflip {
            const val COMMAND = "coinflip"
            val CATEGORY = CommandCategory.UTILITY
            const val DESCRIPTION = "Flips a coin."
            const val USAGE = "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND\n" +
                    "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND go for a run\n" +
                    "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND heads east tails west"
        }

        object Help {
            const val COMMAND = "help"
            val CATEGORY = CommandCategory.HIDDEN
            const val DESCRIPTION = "Shows a list of commands."
            const val USAGE = "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND\n" +
                    "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND <command category>"
            const val TITLE_CATEGORIES = "$QUESTION_MARK $SPECIFIER_BOT_NAME Help"
            const val DESCRIPTION_CATEGORIES = "$SPECIFIER_BOT_NAME's commands are separated by category.\n" +
                    "Type $OP_CODE_LINE!help <category name>$OP_CODE_LINE for more information."
            const val TITLE_COMMANDS = "$QUESTION_MARK $SPECIFIER_BOT_NAME Help - %s"
            const val DESCRIPTION_COMMANDS = "Viewing $OP_ITALIC$SPECIFIER_BOT_NAME$OP_ITALIC commands.\n" +
                    "Type $OP_CODE_LINE!help$OP_CODE_LINE for a list of command categories."
        }

        object Jumbo {
            const val ARG_EMOJI = "emojis"
            const val COMMAND = "jumbo"
            val CATEGORY = CommandCategory.UTILITY
            const val DESCRIPTION = "Makes emoji larger."
            const val USAGE_ARGS = "$SPECIFIER_COMMAND_PREFIX$COMMAND <$ARG_EMOJI>"
            const val USAGE = "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND <emojis>\n" +
                    "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND <emojis> 200px\n" +
                    "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND <emojis> 20%"
            const val MAX_SIZE = 800
            const val MIN_SIZE = 136
        }

        object Ping {
            const val COMMAND = "ping"
            val CATEGORY = CommandCategory.UTILITY
            const val DESCRIPTION = "Make sure $SPECIFIER_BOT_NAME isn't sleeping on the job."
            const val USAGE = "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND"
        }

        object Random {
            const val COMMAND = "random"
            val CATEGORY = CommandCategory.UTILITY
            const val DESCRIPTION = "Picks a random thing."
            const val USAGE = "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND number 10\n" +
                    "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND number 15 - 40\n" +
                    "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND list Red, Blue, Green\n" +
                    "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND day\n" +
                    "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND weekday\n" +
                    "$OP_QUOTE_LINE$SPECIFIER_COMMAND_PREFIX$COMMAND month"
        }
    }
}

object Discord {
    object Markdown {
        const val OP_BOLD = "**"
        const val OP_CODE_LINE = "`"
        const val OP_CODE_BLOCK = "```"
        const val OP_ITALIC = "*"
        const val OP_ITALIC_ALT = "_"
        const val OP_SPOILER = "||"
        const val OP_STRIKE = "~~"
        const val OP_QUOTE_LINE = "> "
        const val OP_QUOTE_BLOCK = ">>> "
        const val OP_UNDERLINE = "__"
    }
}

object Duration {
    const val WEEK = 604800000L
    const val DAY = 86400000L
    const val HOUR = 3600000L
    const val MINUTE = 60000L
    const val SECOND = 1000L
}

object Emote {
    const val BALLOT_BOX = ":ballot_box:"
    const val BLUE_SQUARE = ":blue_square:"
    const val CALENDAR = ":calendar_spiral:"
    const val CHECKMARK = ":white_check_mark:"
    const val INBOX_TRAY = ":inbox_tray:"
    const val ORANGE_SQUARE = ":orange_square:"
    const val OUTBOX_TRAY = ":outbox_tray:"
    const val QUESTION_MARK = ":grey_question:"
    const val TOOLS = ":tools:"
    const val X = ":x:"

    object Character {
        const val ZERO = ":zero:"
        const val ONE = ":one:"
        const val TWO = ":two:"
        const val THREE = ":three:"
        const val FOUR = ":four:"
        const val FIVE = ":five:"
        const val SIX = ":six:"
        const val SEVEN = ":seven:"
        const val EIGHT = ":eight:"
        const val NINE = ":nine:"
        const val A = ":regional_indicator_a:"
        const val B = ":regional_indicator_b:"
        const val C = ":regional_indicator_c:"
        const val D = ":regional_indicator_d:"
        const val E = ":regional_indicator_e:"
        const val F = ":regional_indicator_f:"
        const val G = ":regional_indicator_g:"
        const val H = ":regional_indicator_h:"
        const val I = ":regional_indicator_i:"
        const val J = ":regional_indicator_j:"
        const val K = ":regional_indicator_k:"
        const val L = ":regional_indicator_l:"
        const val M = ":regional_indicator_m:"
        const val N = ":regional_indicator_n:"
        const val O = ":regional_indicator_o:"
        const val P = ":regional_indicator_p:"
        const val Q = ":regional_indicator_q:"
        const val R = ":regional_indicator_r:"
        const val S = ":regional_indicator_s:"
        const val T = ":regional_indicator_t:"
        const val U = ":regional_indicator_u:"
        const val V = ":regional_indicator_v:"
        const val W = ":regional_indicator_w:"
        const val X = ":regional_indicator_x:"
        const val Y = ":regional_indicator_y:"
        const val Z = ":regional_indicator_z:"
        const val ASTERISK = ":asterisk:"
        const val HASH = ":hash:"
        const val EXCLAMATION_MARK = ":exclamation:"
        const val QUESTION_MARK = ":question:"
    }
    
    object Unicode {
        const val ARROW_LEFT = "⬅️"
        const val ARROW_LEFT_DOUBLE = "⏮️"
        const val ARROW_RIGHT = "➡️"
        const val ARROW_RIGHT_DOUBLE = "⏭️"
        const val BLUEBERRIES = "\uD83E\uDED0"
        const val CROSSED_SWORDS = "⚔️"
        const val HEART_PURPLE = "\uD83D\uDC9C"
        const val NUMBER_ONE = "1️⃣"
        const val NUMBER_TWO = "2️⃣"
        const val NUMBER_THREE = "3️⃣"
        const val NUMBER_FOUR = "4️⃣"
        const val NUMBER_FIVE = "5️⃣"
        const val NUMBER_SIX = "6️⃣"
        const val NUMBER_SEVEN = "7️⃣"
        const val NUMBER_EIGHT = "8️⃣"
        const val NUMBER_NINE = "9️⃣"
        const val PRINCESS = "\uD83D\uDC78"
        const val RECYCLE_SYMBOL = "♻️"
        const val SNAIL = "\uD83D\uDC0C"
        const val SQUARE_BLUE = "\uD83D\uDFE6"
        const val SQUARE_ORANGE = "\uD83D\uDFE7"
    }
}

object File {
    object Ext {
        const val GIF = "gif"
        const val PNG = "png"
    }
}

object Messages {
    const val INCORRECT_COMMAND = "$X Command was incorrect.  Available commands:"
    const val MISSING_REQUIRED_COMMAND_ARGUMENT = "$X You are missing a required command argument:"
    const val MISSING_REQUIRED_COMMAND_ARGUMENTS = "$X You are missing required command arguments:"
}