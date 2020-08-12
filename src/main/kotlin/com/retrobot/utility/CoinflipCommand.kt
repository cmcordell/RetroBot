package com.retrobot.utility

import com.retrobot.core.Bot
import com.retrobot.core.Commands.Utils.Coinflip.CATEGORY
import com.retrobot.core.Commands.Utils.Coinflip.COMMAND
import com.retrobot.core.Commands.Utils.Coinflip.DESCRIPTION
import com.retrobot.core.Commands.Utils.Coinflip.USAGE
import com.retrobot.core.domain.command.Command
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.util.containsInOrder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

/**
 *  Flips a coin.
 *
 *  Example commands:
 *  BASIC:    !coinflip -> returns "Heads!" or "Tails!"
 *  ACTION:   !coinflip go for a jog -> returns "Heads!  Yes, go for a jog." or "Tails!  No, don't go for a jog"
 *  DECISION: !coinflip heads east tails west -> returns "Heads!  east" or "Tails!  west"
 */
class CoinflipCommand : Command() {
    override val label = COMMAND
    override val category = CATEGORY
    override val description = DESCRIPTION
    override val usage = USAGE

    private enum class FlipResult(val value: String) {
        HEADS("Heads"),
        TAILS("Tails")
    }

    private enum class MessageType {
        BASIC, ACTION, DECISION
    }

    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        val returnMessage = when (determineMessageType(args)) {
            MessageType.BASIC -> handleBasicFlip()
            MessageType.ACTION -> handleActionFlip(args)
            MessageType.DECISION -> handleDecisionFlip(args)
        }

        event.channel.sendMessage(returnMessage).queue()
    }

    private fun determineMessageType(messageContent: String) : MessageType {
        return when {
            messageContent.isEmpty() -> MessageType.BASIC
            messageContent.containsInOrder(listOf(FlipResult.HEADS.value, FlipResult.TAILS.value), true) -> MessageType.DECISION
            else -> MessageType.ACTION
        }
    }

    private fun flip() : FlipResult {
        return if ((0..1).random() == 0) {
            FlipResult.HEADS
        } else {
            FlipResult.TAILS
        }
    }

    private fun handleBasicFlip() : String {
        return "${flip().value}!"
    }

    private fun handleActionFlip(message: String) : String {
        val result = flip()
        val affirmative = when (result) {
            FlipResult.HEADS -> "Yes,"
            FlipResult.TAILS -> "No, don't"
        }
        return "${result.value}! $affirmative ${message.trim()}."
    }

    private fun handleDecisionFlip(message: String) : String {
        val result = flip()

        val conditionStart: Int
        val conditionEnd: Int
        when (result) {
            FlipResult.HEADS -> {
                conditionStart = message.indexOf(FlipResult.HEADS.value, 0, true) + FlipResult.HEADS.value.length
                conditionEnd = message.indexOf(FlipResult.TAILS.value, 0, true)
            }
            FlipResult.TAILS -> {
                conditionStart = message.indexOf(FlipResult.TAILS.value, 0, true) + FlipResult.TAILS.value.length
                conditionEnd = message.length
            }
        }

        val condition = message.substring(conditionStart, conditionEnd).trim().capitalize()
        return if (condition.isEmpty()) {
            "${result.value}!"
        } else {
            "${result.value}!  $condition."
        }
    }
}