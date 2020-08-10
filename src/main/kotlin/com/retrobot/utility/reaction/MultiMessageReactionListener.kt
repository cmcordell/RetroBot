package com.retrobot.utility.reaction

import com.retrobot.core.Bot
import com.retrobot.core.Duration
import com.retrobot.core.Emote
import com.retrobot.core.domain.UnicodeEmote
import com.retrobot.core.reactionhandler.ReactionListener
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent

/**
 * A [ReactionListener] that uses Reactions to switch between multiple messages in a single post.
 * This will be used when a [Message] is too large and must be broken down into multiple messages
 */
class MultiMessageReactionListener(
        jdaMessage: Message,
        messages: List<Message>,
        duration: Long = Duration.DAY
) : ReactionListener(duration) {

    init {
        reactions = listOf(
                UnicodeEmote(Emote.Unicode.ARROW_LEFT),
                UnicodeEmote(Emote.Unicode.ARROW_RIGHT)
        )
    }

    private val initialMessage = jdaMessage
    private val messages = messages.toMutableList()
    private var currentMessageIndex = 0


    override suspend fun addReaction(bot: Bot, event: GuildMessageReactionAddEvent) {
        if (event.reaction.reactionEmote.isEmoji) {
            when (event.reaction.reactionEmote.emoji) {
                Emote.Unicode.ARROW_LEFT -> moveToPreviousEmbed(event)
                Emote.Unicode.ARROW_RIGHT -> moveToNextEmbed(event)
                else -> {}
            }
        }
    }

    fun updateMessages(newMessages: List<Message>) {
        if (!isActive()) return
        messages.apply {
            clear()
            addAll(newMessages)
        }.let {
            currentMessageIndex = currentMessageIndex.coerceIn(0 until messages.size)
            initialMessage.textChannel.editMessageById(initialMessage.id, messages[currentMessageIndex]).queue()
        }
    }

    private fun moveToPreviousEmbed(event: GuildMessageReactionAddEvent) {
        if (currentMessageIndex > 0) {
            val messageEmbed = messages[--currentMessageIndex]
            val textChannel = event.jda.getGuildById(event.guild.idLong)?.getGuildChannelById(event.channel.id) as TextChannel?
            textChannel?.editMessageById(event.messageIdLong, messageEmbed)?.queue()
        }
        event.reaction.removeReaction(event.user).queue()
    }

    private fun moveToNextEmbed(event: GuildMessageReactionAddEvent) {
        if (currentMessageIndex < messages.size -1) {
            val messageEmbed = messages[++currentMessageIndex]
            val textChannel = event.jda.getGuildById(event.guild.idLong)?.getGuildChannelById(event.channel.id) as TextChannel?
            textChannel?.editMessageById(event.messageIdLong, messageEmbed)?.queue()
        }
        event.reaction.removeReaction(event.user).queue()
    }

    override suspend fun removeAllReactions(bot: Bot, event: GuildMessageReactionRemoveAllEvent) {
        active = false
    }
}