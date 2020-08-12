package com.retrobot.core.reactionhandler

import com.retrobot.core.Bot
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.domain.WrappedEmote
import kotlinx.coroutines.*
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageReaction
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent

/**
 * Base class for listening to [MessageReaction]s on a specific Discord [Message]
 *
 * @param duration How long this [ReactionListener] will listen for [MessageReaction]s in milliseconds.
 */
abstract class ReactionListener(
    duration: Long
) {
    var reactions = listOf<WrappedEmote>()
    private val expiresAt: Long = System.currentTimeMillis() + duration
    private val scope = CoroutineScope(Job() + Dispatchers.Default)


    open suspend fun addReaction(bot: Bot, event: GuildMessageReactionAddEvent, guildSettings: GuildSettings) {}
    open suspend fun removeReaction(bot: Bot, event: GuildMessageReactionRemoveEvent, guildSettings: GuildSettings) {}
    open suspend fun removeAllReactions(bot: Bot, event: GuildMessageReactionRemoveAllEvent, guildSettings: GuildSettings) {}

    fun onReactionAdd(bot: Bot, event: GuildMessageReactionAddEvent, guildSettings: GuildSettings) {
        scope.launch { addReaction(bot, event, guildSettings) }
    }

    fun onReactionRemove(bot: Bot, event: GuildMessageReactionRemoveEvent, guildSettings: GuildSettings) {
        scope.launch { removeReaction(bot, event, guildSettings) }
    }

    fun onReactionRemoveAll(bot: Bot, event: GuildMessageReactionRemoveAllEvent, guildSettings: GuildSettings) {
        scope.launch {
            removeAllReactions(bot, event, guildSettings)
            stop()
        }
    }

    fun stop() {
        scope.cancel()
    }

    fun hasExpired(): Boolean {
        return System.currentTimeMillis() > expiresAt
    }

    fun isActive(): Boolean {
        if (hasExpired()) stop()
        return scope.isActive
    }
}