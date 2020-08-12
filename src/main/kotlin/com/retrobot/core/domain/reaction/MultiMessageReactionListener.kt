package com.retrobot.core.domain.reaction

import com.retrobot.core.Bot
import com.retrobot.core.Duration
import com.retrobot.core.Emote
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.domain.UnicodeEmote
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageReaction
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent

/**
 * A [ReactionListener] that uses [MessageReaction]s to switch between multiple [Message]s in a single Discord post.
 * This can be used when a [Message] is too large and must be broken down into multiple [Message]s.
 *
 * @param initialMessage This must be the corresponding [Message] returned by JDA.
 * @param messages The [List] of [Message]s that are available to paginate between.
 * @param duration How long this [ReactionListener] will allow pagination on the corresponding [Message] in milliseconds.
 */
class MultiMessageReactionListener(
        private val initialMessage: Message,
        messages: List<Message>,
        duration: Long = Duration.DAY
) : ReactionListener(duration) {

    init {
        reactions = listOf(
                UnicodeEmote(Emote.Unicode.ARROW_LEFT),
                UnicodeEmote(Emote.Unicode.ARROW_RIGHT)
        )
    }

    private val messages = messages.toMutableList()
    private var currentMessageIndex = 0


    override suspend fun addReaction(bot: Bot, event: GuildMessageReactionAddEvent, guildSettings: GuildSettings) {
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
}