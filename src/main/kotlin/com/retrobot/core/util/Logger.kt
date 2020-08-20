package com.retrobot.core.util

import com.retrobot.core.BuildType
import net.dv8tion.jda.api.events.guild.GuildBanEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent
import java.lang.String.format


object Logger {
    private const val LOG_GUILD_JOIN_EVENT = "Joined guild %s"
    private const val LOG_GUILD_LEAVE_EVENT = "Left guild %s"
    private const val LOG_GUILD_BAN_EVENT = "Banned from guild %s by user %s"
    private const val LOG_GUILD_UNBAN_EVENT = "Unbanned from guild %s by user %s"
    private const val LOG_GUILD_MESSAGE_RECEIVED_EVENT = "%s : [%s] %s: %s"
    private const val LOG_GUILD_REACTION_ADD_EVENT = "%s : [%s] %s added reaction %s to message with id %s"
    private const val LOG_GUILD_REACTION_REMOVE_EVENT = "%s : [%s] %s removed reaction %s to message with id %s"
    private const val LOG_GUILD_REACTION_REMOVE_ALL_EVENT = "%s : [%s] All reactions removed from message with id %s"
    private val BUILD_TYPE = Properties.config()[Properties.bot.buildType]


    fun log(message: String) {
        when (BUILD_TYPE) {
            BuildType.DEBUG -> println(message)
            BuildType.RELEASE -> {}
        }
    }

    fun log(e: Exception) {
        when (BUILD_TYPE) {
            BuildType.DEBUG -> println(e.localizedMessage)
            BuildType.RELEASE -> {}
        }
    }

    fun log(t: Throwable) {
        when (BUILD_TYPE) {
            BuildType.DEBUG -> println(t.localizedMessage)
            BuildType.RELEASE -> {}
        }
    }

    fun log(event: GuildJoinEvent) {
        log(format(LOG_GUILD_JOIN_EVENT, event.guild.name))
    }

    fun log(event: GuildLeaveEvent) {
        log(format(LOG_GUILD_LEAVE_EVENT, event.guild.name))
    }

    fun log(event: GuildBanEvent) {
        log(format(LOG_GUILD_BAN_EVENT, event.guild.name, event.user))
    }

    fun log(event: GuildUnbanEvent) {
        log(format(LOG_GUILD_UNBAN_EVENT, event.guild.name, event.user))
    }

    fun log(event: GuildMessageReceivedEvent) {
        log(
            format(
                LOG_GUILD_MESSAGE_RECEIVED_EVENT,
                event.message.guild.name, event.message.channel.name, event.author.name, event.message.contentRaw
            )
        )
    }

    fun log(event: GuildMessageReactionAddEvent) {
        log(
            format(
                LOG_GUILD_REACTION_ADD_EVENT,
                event.guild.name,
                event.channel.name,
                event.user.name,
                event.reactionEmote.asReactionCode,
                event.messageId
            )
        )
    }

    fun log(event: GuildMessageReactionRemoveEvent) {
        log(
            format(
                LOG_GUILD_REACTION_REMOVE_EVENT,
                event.guild.name,
                event.channel.name,
                event.user?.name,
                event.reactionEmote.asReactionCode,
                event.messageId
            )
        )
    }

    fun log(event: GuildMessageReactionRemoveAllEvent) {
        log(
            format(
                LOG_GUILD_REACTION_REMOVE_ALL_EVENT,
                event.guild.name,
                event.channel.name,
                event.messageId
            )
        )
    }
}