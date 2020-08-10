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
import com.retrobot.core.domain.UnicodeEmote
import com.retrobot.core.reactionhandler.ReactionListener
import com.retrobot.core.util.toBuilder
import com.retrobot.polls.domain.Poll
import com.retrobot.polls.domain.PollMessage
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent


class PollReactionListener(
    private val poll: Poll,
    duration: Long = Duration.DAY
) : ReactionListener(duration) {
    
    private val pollReactions = listOf(
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
        reactions = pollReactions.subList(0, poll.options.size)
    }
    
    
    override suspend fun addReaction(bot: Bot, event: GuildMessageReactionAddEvent) {
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
            updatePollMessage(bot, event, poll)
//            val textChannel = event.jda.getGuildById(event.guild.idLong)?.getGuildChannelById(event.channel.id) as TextChannel?
//            textChannel?.editMessageById(event.messageIdLong, PollMessage.buildMessageEmbed(poll))?.queue()
        }
    }
    
    override suspend fun removeReaction(bot: Bot, event: GuildMessageReactionRemoveEvent) {
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
            updatePollMessage(bot, event, poll)
//            val textChannel = event.jda.getGuildById(event.guild.idLong)?.getGuildChannelById(event.channel.id) as TextChannel?
//            textChannel?.editMessageById(event.messageIdLong, PollMessage.buildMessageEmbed(poll))?.queue()
        }
    }
    
    override suspend fun removeAllReactions(bot: Bot, event: GuildMessageReactionRemoveAllEvent) {
        active = false
    }

    private suspend fun updatePollMessage(bot: Bot, event: GenericGuildMessageReactionEvent, poll: Poll) {
        val textChannel = event.jda.getGuildById(event.guild.idLong)?.getGuildChannelById(event.channel.id) as TextChannel?
        val botHighlightColor = bot.guildSettingsRepo.getGuildSettings(event.guild.id).botHighlightColor
        val newMessage = PollMessage.buildMessageEmbed(poll)
                .toBuilder()
                .setColor(botHighlightColor)
                .build()
        textChannel?.editMessageById(event.messageIdLong, newMessage)?.queue()
    }
}