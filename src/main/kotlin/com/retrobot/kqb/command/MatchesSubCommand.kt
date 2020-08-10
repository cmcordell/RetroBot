package com.retrobot.kqb.command

import com.retrobot.core.Bot
import com.retrobot.core.Duration
import com.retrobot.core.command.SubCommand
import com.retrobot.core.util.buildMessage
import com.retrobot.core.util.toMessageBuilder
import com.retrobot.kqb.GetMatchesUseCase
import com.retrobot.kqb.domain.Match
import com.retrobot.kqb.service.MatchMessageUpdateService
import com.retrobot.kqb.service.MatchMultiMessageUpdateService
import com.retrobot.utility.reaction.MultiMessageReactionListener
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

/**
 * !kqb matches
 */
class MatchesSubCommand : SubCommand() {
    override val labels = setOf("matches", "matchs", "match", "games", "game")
    override val description = "Get KQB match info"
    override val usage = "!kqb matches"

    private val getMatchesUseCase = GetMatchesUseCase()

    // TODO Take args. maybe a date 8/8
    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String) {
        val now = System.currentTimeMillis()
        val oneDayFromNow = now + Duration.DAY
        val matches = getMatchesUseCase.getMatches(now, oneDayFromNow)

        when {
            matches.isEmpty() -> sendNoMatchesMessage(bot, event)
            matches.size == 1 -> sendMatchMessage(bot, event, matches[0])
            else -> sendMultiMatchMessage(bot, event, matches)
        }
    }

    private suspend fun sendNoMatchesMessage(bot: Bot, event: GuildMessageReceivedEvent) {
        val embedColor = bot.guildSettingsRepo.getGuildSettings(event.guild.id).botHighlightColor
        val message = EmbedBuilder()
                .setColor(embedColor)
                .setTitle("There are no matches scheduled.")
                .buildMessage()
        event.channel.sendMessage(message).queue()
    }

    private suspend fun sendMatchMessage(bot: Bot, event: GuildMessageReceivedEvent, match: Match) {
        val embedColor = bot.guildSettingsRepo.getGuildSettings(event.guild.id).botHighlightColor
        val returnMessage = EmbedBuilder(getMatchesUseCase.mapMatchToMessageEmbed(match))
                .setColor(embedColor)
                .buildMessage()
        event.channel.sendMessage(returnMessage).queue { message ->
            bot.serviceHandler.addService(MatchMessageUpdateService(match, message))
        }
    }

    private suspend fun sendMultiMatchMessage(bot: Bot, event: GuildMessageReceivedEvent, matches: List<Match>) {
        val embedColor = bot.guildSettingsRepo.getGuildSettings(event.guild.id).botHighlightColor
        val returnMessageEmbeds = matches.map { getMatchesUseCase.mapMatchToMessageEmbed(it) }
        val returnMessages = returnMessageEmbeds.mapIndexed { index, embed ->
            EmbedBuilder(embed)
                    .setColor(embedColor)
                    .setTitle(embed.title + "   (${index + 1} of ${returnMessageEmbeds.size})")
                    .toMessageBuilder()
                    .setContent("Here are the matches for the next 24 hours:\n(Note: This command must be rerun to see matches > 24 hours from now)")
                    .build()
        }
        event.channel.sendMessage(returnMessages[0]).queue { message ->
            val reactionListener = MultiMessageReactionListener(message, returnMessages)
            bot.reactionHandler.addReactionListener(message.guild.id, message, reactionListener)
            bot.serviceHandler.addService(MatchMultiMessageUpdateService(matches, message, reactionListener))
        }
    }
}