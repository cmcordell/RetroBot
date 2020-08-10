package com.retrobot.utility.command

import com.retrobot.core.Bot
import com.retrobot.core.BotConfig.PREFIX
import com.retrobot.core.Commands.Utils.Random.CATEGORY
import com.retrobot.core.Commands.Utils.Random.COMMAND
import com.retrobot.core.Commands.Utils.Random.DESCRIPTION
import com.retrobot.core.Commands.Utils.Random.USAGE
import com.retrobot.core.command.Command
import com.retrobot.core.util.Messages.generateIncorrectCommandMessage
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.lang.Integer.parseInt

/**
 *  Picks a random item given the provided items
 *
 *  Example commands:
 *  NUMBER:        !random number 10 or !random number 25-100
 *  LIST:          !random list Red, Blue, Green, Yellow
 *  DAY:           !random day
 *  WEEKDAY:       !random weekday
 *  MONTH:         !random month
 */
class RandomCommand : Command() {
    override val label = "$PREFIX$COMMAND"
    override val category = CATEGORY
    override val description = DESCRIPTION
    override val usage = USAGE

    private enum class MessageType(val value: String) {
        INCORRECT(""),
        NUMBER("Number"),
        LIST("List"),
        DAY("Day"),
        WEEKDAY("Weekday"),
        MONTH("Month")
    }

    enum class DayOfTheWeek(val value: String, val number: Int) {
        SUNDAY("Sunday", 1),
        MONDAY("Monday", 2),
        TUESDAY("Tuesday", 3),
        WEDNESDAY("Wednesday", 4),
        THURSDAY("Thursday", 5),
        FRIDAY("Friday", 6),
        SATURDAY("Saturday", 7),
    }

    enum class Month(val value: String, val number: Int) {
        JANUARY("January", 1),
        FEBRUARY("February", 2),
        MARCH("March", 3),
        APRIL("April", 4),
        MAY("May", 5),
        JUNE("June", 6),
        JULY("July", 7),
        AUGUST("August", 8),
        SEPTEMBER("September", 9),
        OCTOBER("October", 10),
        NOVEMBER("November", 11),
        DECEMBER("December", 12),
    }

    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String) {
        var returnMessage = when (determineMessageType(args)) {
            MessageType.INCORRECT -> null
            MessageType.NUMBER -> handleNumberMessage(args)
            MessageType.LIST -> handleListMessage(args)
            MessageType.DAY -> handleDayMessage()
            MessageType.WEEKDAY -> handleWeekdayMessage()
            MessageType.MONTH -> handleMonthMessage()
        }

        if (returnMessage == null) {
            val guildSettings = bot.guildSettingsRepo.getGuildSettings(event.guild.id)
            returnMessage = generateIncorrectCommandMessage(this, guildSettings)
        }

        event.channel.sendMessage(returnMessage).queue()
    }

    private fun determineMessageType(message: String) : MessageType {
        return when {
            message.isEmpty() -> MessageType.INCORRECT
            message.indexOf(MessageType.NUMBER.value, 0, true) == 0 -> MessageType.NUMBER
            message.indexOf(MessageType.LIST.value, 0, true) == 0 -> MessageType.LIST
            message.indexOf(MessageType.DAY.value, 0, true) == 0 -> MessageType.DAY
            message.indexOf(MessageType.WEEKDAY.value, 0, true) == 0 -> MessageType.WEEKDAY
            message.indexOf(MessageType.MONTH.value, 0, true) == 0 -> MessageType.MONTH
            else -> MessageType.INCORRECT
        }
    }

    private fun handleNumberMessage(message: String) : String? {
        val messageBody = message.trim().substring(MessageType.NUMBER.value.length).trim()
        val numbers = messageBody.split("-", " ", ",").filter { it.isNotEmpty() }

        var returnMessage: String? = null

        try {
            if (numbers.size == 1) {
                val number = parseInt(numbers[0])
                if (number > 0) {
                    returnMessage = (1..number).random().toString()
                }
            } else if (numbers.size == 2) {
                val first = parseInt(numbers[0])
                val second = parseInt(numbers[1])
                returnMessage = if (first <= second) {
                    (first..second).random().toString()
                } else {
                    (second..first).random().toString()
                }
            }
        } catch (e: Exception) {}

        return returnMessage
    }

    private fun handleListMessage(message: String) : String? {
        val messageBody = message.trim()
            .substring(MessageType.LIST.value.length)
            .trim()
        val items = messageBody.split(",")
            .filter { it.isNotEmpty() }

        return if (items.isEmpty()) {
            null
        } else {
            items[0.until(items.size).random()].trim()
        }
    }

    private fun handleDayMessage() : String {
        return DayOfTheWeek.values()[(0..6).random()].value
    }

    private fun handleWeekdayMessage() : String {
        return DayOfTheWeek.values()[(1..5).random()].value
    }

    private fun handleMonthMessage() : String {
        return Month.values()[(0..11).random()].value
    }
}