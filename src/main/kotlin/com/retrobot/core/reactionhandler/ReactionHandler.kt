package com.retrobot.core.reactionhandler

import com.retrobot.core.Bot
import com.retrobot.core.domain.CustomEmote
import com.retrobot.core.domain.UnicodeEmote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent
import java.util.concurrent.ConcurrentHashMap

/**
 * Handler for all [ReactionListener]s.  Accepts one [ReactionListener] per [Message].
 */
class ReactionHandler {
    private val guildListenerMap = ConcurrentHashMap<String, ConcurrentHashMap<String, ReactionListener>>()

    /**
     * Add a [ReactionListener] for the given [Message] and automatically add valid response reactions.
     */
    fun addReactionListener(guildId: String, message: Message, reactionListener: ReactionListener) {
        val messageListenerMap = guildListenerMap.getOrPut(guildId, { ConcurrentHashMap() })
        if (!messageListenerMap.contains(message.idLong)) {
            for (reaction in reactionListener.reactions) {
                when (reaction) {
                    is CustomEmote -> message.addReaction(reaction.emote).queue()
                    is UnicodeEmote -> message.addReaction(reaction.unicode).queue()
                }
            }
            messageListenerMap[message.id] = reactionListener
        }
    }

    fun onGuildMessageReactionAdd(bot: Bot, event: GuildMessageReactionAddEvent) {
        if (event.user.isBot) return

        doOnActiveListener(event) { reactionListener ->
            GlobalScope.launch(Dispatchers.Default) { reactionListener.onReactionAdd(bot, event) }
        }
    }

    fun onGuildMessageReactionRemove(bot: Bot, event: GuildMessageReactionRemoveEvent) {
        if (event.user != null && event.user!!.isBot) return

        doOnActiveListener(event) { reactionListener ->
            GlobalScope.launch(Dispatchers.Default) { reactionListener.onReactionRemove(bot, event) }
        }
    }

    fun onGuildMessageReactionRemoveAll(bot: Bot, event: GuildMessageReactionRemoveAllEvent) {
        doOnActiveListener(event) { reactionListener ->
            GlobalScope.launch(Dispatchers.Default) { reactionListener.onReactionRemoveAll(bot, event) }
        }
    }

    /**
     * Perform [doOnActive] if a [ReactionListener] exists in [guildListenerMap] and is active for the given [event].
     * If a listener exists and is not active, it will be removed from [guildListenerMap]
     */
    private fun doOnActiveListener(event: GenericGuildMessageEvent, doOnActive: (ReactionListener) -> Unit) {
        val listener = guildListenerMap[event.guild.id]?.get(event.messageId)
        if (listener != null) {
            if (listener.isActive()) {
                doOnActive(listener)
            } else {
                guildListenerMap[event.guild.id]?.remove(event.messageId)
            }
        }
    }

    @Synchronized
    fun cleanCache() {
        for (messageListenerMapEntry in guildListenerMap.entries) {
            messageListenerMapEntry.value.values.removeIf { !it.isActive() }
            if (messageListenerMapEntry.value.values.isEmpty()) {
                guildListenerMap.remove(messageListenerMapEntry.key)
            }
        }
    }
}