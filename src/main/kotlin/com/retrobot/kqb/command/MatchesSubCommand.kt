package com.retrobot.kqb.command

import com.retrobot.core.Bot
import com.retrobot.core.Duration
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.domain.command.SubCommand
import com.retrobot.core.domain.reaction.MultiMessageReactionListener
import com.retrobot.core.util.buildMessage
import com.retrobot.core.util.toMessageBuilder
import com.retrobot.kqb.domain.usecase.GetMatchesUseCase
import com.retrobot.kqb.domain.model.Match
import com.retrobot.kqb.service.MatchMessageUpdateService
import com.retrobot.kqb.service.MatchMultiMessageUpdateService
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.koin.core.inject

/**
 * !kqb matches
 */
class MatchesSubCommand : SubCommand() {
    override val labels = setOf("matches", "matchs", "match", "games", "game")
    override val description = "Get KQB match info"
    override val usage = "!kqb matches"

    private val getMatchesUseCase: GetMatchesUseCase by inject()

    // TODO Take args. maybe a date 8/8
    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        val now = System.currentTimeMillis()
        val oneHourBeforeNow = now - Duration.HOUR
        val oneDayFromNow = now + Duration.DAY
        val matches = getMatchesUseCase.getMatches(oneHourBeforeNow, oneDayFromNow)

        when {
            matches.isEmpty() -> sendNoMatchesMessage(event, guildSettings)
            matches.size == 1 -> sendMatchMessage(bot, event, matches[0], guildSettings)
            else -> sendMultiMatchMessage(bot, event, matches, guildSettings)
        }
    }

    private fun sendNoMatchesMessage(event: GuildMessageReceivedEvent, guildSettings: GuildSettings) {
        val embedColor = guildSettings.botHighlightColor
        val message = EmbedBuilder()
                .setColor(embedColor)
                .setTitle("There are no matches scheduled.")
                .buildMessage()
        event.channel.sendMessage(message).queue()
    }

    private suspend fun sendMatchMessage(bot: Bot, event: GuildMessageReceivedEvent, match: Match, guildSettings: GuildSettings) {
        val embedColor = guildSettings.botHighlightColor
        val returnMessage = EmbedBuilder(getMatchesUseCase.mapMatchToMessageEmbed(match))
                .setColor(embedColor)
                .buildMessage()
        event.channel.sendMessage(returnMessage).queue { message ->
            bot.serviceHandler.addService(MatchMessageUpdateService(match, message))
        }
    }

    private suspend fun sendMultiMatchMessage(bot: Bot, event: GuildMessageReceivedEvent, matches: List<Match>, guildSettings: GuildSettings) {
        val embedColor = guildSettings.botHighlightColor
        val returnMessageEmbeds = matches.map { getMatchesUseCase.mapMatchToMessageEmbed(it) }
        val returnMessages = returnMessageEmbeds.mapIndexed { index, embed ->
            EmbedBuilder(embed)
                    .setColor(embedColor)
                    .setTitle(embed.title + "   (${index + 1} of ${returnMessageEmbeds.size})")
                    .toMessageBuilder()
                    .setContent("Here are the matches for the next 24 hours:")
                    .build()
        }
        event.channel.sendMessage(returnMessages[0]).queue { message ->
            val reactionListener =
                MultiMessageReactionListener(
                    message,
                    returnMessages
                )
            bot.reactionHandler.addReactionListener(message.guild.id, message, reactionListener)
            bot.serviceHandler.addService(MatchMultiMessageUpdateService(matches, message, reactionListener))
        }
    }
}