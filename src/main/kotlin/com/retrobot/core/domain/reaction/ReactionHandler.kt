package com.retrobot.core.domain.reaction

import com.retrobot.core.Bot
import com.retrobot.core.domain.CustomEmote
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.domain.UnicodeEmote
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
    // TODO Persist ReactionListeners so we can restart them after process death
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

    fun onGuildMessageReactionAdd(bot: Bot, event: GuildMessageReactionAddEvent, guildSettings: GuildSettings) {
        if (event.user.isBot) return

        doOnActiveListener(event) { reactionListener ->
            reactionListener.onReactionAdd(bot, event, guildSettings)
        }
    }

    fun onGuildMessageReactionRemove(bot: Bot, event: GuildMessageReactionRemoveEvent, guildSettings: GuildSettings) {
        if (event.user != null && event.user!!.isBot) return

        doOnActiveListener(event) { reactionListener ->
            reactionListener.onReactionRemove(bot, event, guildSettings)
        }
    }

    fun onGuildMessageReactionRemoveAll(bot: Bot, event: GuildMessageReactionRemoveAllEvent, guildSettings: GuildSettings) {
        doOnActiveListener(event) { reactionListener ->
            reactionListener.onReactionRemoveAll(bot, event, guildSettings)
        }
    }

    /**
     * Remove all inactive [ReactionListener]s and guilds with 0 [ReactionListener]s
     */
    @Synchronized
    fun cleanCache() {
        guildListenerMap.entries.forEach { entry ->
            entry.value.values.removeIf { !it.isActive() }
            if (entry.value.values.isEmpty()) {
                guildListenerMap.remove(entry.key)
            }
        }
    }

    /**
     * Perform [doOnActive] if a [ReactionListener] exists in [guildListenerMap] and is active for the given [event].
     * If a listener exists and is not active, it will be removed from [guildListenerMap]
     */
    private fun doOnActiveListener(event: GenericGuildMessageEvent, doOnActive: (ReactionListener) -> Unit) {
        guildListenerMap[event.guild.id]?.get(event.messageId)?.let { listener ->
            when {
                listener.isActive() -> doOnActive(listener)
                else -> guildListenerMap[event.guild.id]?.remove(event.messageId)
            }
        }
    }
}