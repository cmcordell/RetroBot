package com.retrobot.core.reactionhandler

import com.retrobot.core.Bot
import com.retrobot.core.domain.WrappedEmote
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent

/**
 * Base class for listening to reactions on a specific Discord Message
 */
abstract class ReactionListener(
    duration: Long
) {
    var reactions = listOf<WrappedEmote>()
    private val expiresAt: Long = System.currentTimeMillis() + duration
    protected var active: Boolean = true

    open suspend fun addReaction(bot: Bot, event: GuildMessageReactionAddEvent) {}
    open suspend fun removeReaction(bot: Bot, event: GuildMessageReactionRemoveEvent) {}
    open suspend fun removeAllReactions(bot: Bot, event: GuildMessageReactionRemoveAllEvent) {}

    suspend fun onReactionAdd(bot: Bot, event: GuildMessageReactionAddEvent) {
        if (isActive()) addReaction(bot, event)
    }

    suspend fun onReactionRemove(bot: Bot, event: GuildMessageReactionRemoveEvent) {
        if (isActive()) removeReaction(bot, event)
    }

    suspend fun onReactionRemoveAll(bot: Bot, event: GuildMessageReactionRemoveAllEvent) {
        if (isActive()) removeAllReactions(bot, event)
    }

    fun isActive(): Boolean {
        if (System.currentTimeMillis() > expiresAt) {
            active = false
        }
        return active
    }
}