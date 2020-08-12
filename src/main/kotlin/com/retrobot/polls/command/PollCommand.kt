package com.retrobot.polls.command

import com.retrobot.core.Bot
import com.retrobot.core.Commands.PollsAndEvents.Poll.CATEGORY
import com.retrobot.core.Commands.PollsAndEvents.Poll.COMMAND
import com.retrobot.core.Commands.PollsAndEvents.Poll.DESCRIPTION
import com.retrobot.core.Commands.PollsAndEvents.Poll.USAGE
import com.retrobot.core.Emote.TOOLS
import com.retrobot.core.Emote.X
import com.retrobot.core.command.Command
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.util.containsInOrder
import com.retrobot.core.util.formatGuildInfo
import com.retrobot.core.util.toBuilder
import com.retrobot.polls.domain.Poll
import com.retrobot.polls.domain.PollMessage
import com.retrobot.polls.reaction.PollReactionListener
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

/**
 * Creates a poll
 *
 * !poll [Favorite Color] Red, Blue, Green
 * !poll
 */
class PollCommand : Command() {
    override val label = COMMAND
    override val category = CATEGORY
    override val description = DESCRIPTION
    override val usage = USAGE

    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        try {
            val poll = parseMessageForPoll(args)
            val botHighlightColor = guildSettings.botHighlightColor
            val returnMessage = PollMessage.buildMessageEmbed(poll)
                    .toBuilder()
                    .setColor(botHighlightColor)
                    .build()

            event.channel.sendMessage(returnMessage).queue { message ->
                bot.reactionHandler.addReactionListener(message.guild.id, message,
                    PollReactionListener(poll)
                )
            }
        } catch (e: Exception) {
            event.channel.sendMessage("$X Error: ${e.localizedMessage}\n$TOOLS Command Usage\n${USAGE.formatGuildInfo(guildSettings)}").queue()
        }
    }

    @Throws(Exception::class)
    private fun parseMessageForPoll(message: String) : Poll {
        var errorMessage = "Poll title was incorrect"
        if (message.containsInOrder(listOf("[", "]"))) {
            val pollTitle = message.substring(message.indexOf("[") + 1, message.indexOf("]"))
            if (pollTitle.isNotEmpty()) {
                val optionArgs = message.substring(message.indexOf("]") + 1)
                        .split(",")
                        .map(String::trim)
                        .filter(String::isNotEmpty)
                if (optionArgs.size >= 2) {
                    return buildInitialPoll(pollTitle, optionArgs.take(9))
                } else {
                    errorMessage = "Poll needs 2 or more options"
                }
            }
        }
        throw Exception(errorMessage)
    }

    private fun buildInitialPoll(title: String, options: List<String>) : Poll {
        val pollOptions = mutableMapOf<Int, Poll.Option>()
        for (index in options.indices) {
            val optionNumber = index + 1
            pollOptions[optionNumber] = Poll.Option(optionNumber, options[index])
        }
        return Poll(title, pollOptions)
    }
}