package com.retrobot.polls.reaction

import com.retrobot.core.Bot
import com.retrobot.core.Duration
import com.retrobot.core.Emote.Unicode.NUMBER_EIGHT
import com.retrobot.core.Emote.Unicode.NUMBER_FIVE
import com.retrobot.core.Emote.Unicode.NUMBER_FOUR
import com.retrobot.core.Emote.Unicode.NUMBER_NINE
import com.retrobot.core.Emote.Unicode.NUMBER_ONE
import com.retrobot.core.Emote.Unicode.NUMBER_SEVEN
import com.retrobot.core.Emote.Unicode.NUMBER_SIX
import com.retrobot.core.Emote.Unicode.NUMBER_THREE
import com.retrobot.core.Emote.Unicode.NUMBER_TWO
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.domain.UnicodeEmote
import com.retrobot.core.reactionhandler.ReactionListener
import com.retrobot.core.util.toBuilder
import com.retrobot.polls.domain.Poll
import com.retrobot.polls.domain.PollMessage
import net.dv8tion.jda.api.entities.MessageReaction
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent

/**
 * A [ReactionListener] to enable voting by [MessageReaction] on [Poll]s.
 *
 * @param poll The [Poll] to be voted on.
 * @param duration How long this [ReactionListener] will allow voting in milliseconds.
 */
class PollReactionListener(
    private val poll: Poll,
    duration: Long = Duration.DAY
) : ReactionListener(duration) {
    
    private val POLL_REACTIONS = listOf(
            UnicodeEmote(NUMBER_ONE),
            UnicodeEmote(NUMBER_TWO),
            UnicodeEmote(NUMBER_THREE),
            UnicodeEmote(NUMBER_FOUR),
            UnicodeEmote(NUMBER_FIVE),
            UnicodeEmote(NUMBER_SIX),
            UnicodeEmote(NUMBER_SEVEN),
            UnicodeEmote(NUMBER_EIGHT),
            UnicodeEmote(NUMBER_NINE)
    )
    
    init {
        reactions = POLL_REACTIONS.subList(0, poll.options.size)
    }
    
    
    override suspend fun addReaction(bot: Bot, event: GuildMessageReactionAddEvent, guildSettings: GuildSettings) {
        if (event.reaction.reactionEmote.isEmoji) {
            val optionNumber = when (event.reaction.reactionEmote.emoji) {
                NUMBER_ONE -> 1
                NUMBER_TWO -> 2
                NUMBER_THREE -> 3
                NUMBER_FOUR -> 4
                NUMBER_FIVE -> 5
                NUMBER_SIX -> 6
                NUMBER_SEVEN -> 7
                NUMBER_EIGHT -> 8
                NUMBER_NINE -> 9
                else -> -1
            }

            poll.addVote(optionNumber)
            updatePollMessage(event, poll, guildSettings)
        }
    }
    
    override suspend fun removeReaction(bot: Bot, event: GuildMessageReactionRemoveEvent, guildSettings: GuildSettings) {
        if (event.reaction.reactionEmote.isEmoji) {
            val optionNumber = when (event.reaction.reactionEmote.emoji) {
                NUMBER_ONE -> 1
                NUMBER_TWO -> 2
                NUMBER_THREE -> 3
                NUMBER_FOUR -> 4
                NUMBER_FIVE -> 5
                NUMBER_SIX -> 6
                NUMBER_SEVEN -> 7
                NUMBER_EIGHT -> 8
                NUMBER_NINE -> 9
                else -> -1
            }

            poll.removeVote(optionNumber)
            updatePollMessage(event, poll, guildSettings)
        }
    }

    private suspend fun updatePollMessage(event: GenericGuildMessageReactionEvent, poll: Poll, guildSettings: GuildSettings) {
        val textChannel = event.jda.getGuildById(event.guild.idLong)?.getGuildChannelById(event.channel.id) as TextChannel?
        val newMessage = PollMessage.buildMessageEmbed(poll)
                .toBuilder()
                .setColor(guildSettings.botHighlightColor)
                .build()
        textChannel?.editMessageById(event.messageIdLong, newMessage)?.queue()
    }
}