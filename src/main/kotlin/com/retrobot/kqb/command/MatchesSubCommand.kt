package com.retrobot.kqb.command

import com.retrobot.core.Bot
import com.retrobot.core.Duration
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.domain.TimePeriod
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
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * !kqb matches
 */
class MatchesSubCommand : SubCommand() {
    override val labels = setOf("matches", "matchs", "match", "games", "game")
    override val description = "Get KQB match info"
    override val usage = "!kqb matches\n" +
            "!kqb matches <month>/<day> i.e. 9/22"

    private val getMatchesUseCase: GetMatchesUseCase by inject()

    // TODO Maybe make title hyperlink
    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        var dateString = args
        var timePeriod = getTimePeriod(args)
        if (timePeriod == null) {
            dateString = ""
            timePeriod = getDefaultPeriod()
        }

        val matches = getMatchesUseCase.getMatches(timePeriod.start, timePeriod.end)

        when {
            matches.isEmpty() -> sendNoMatchesMessage(event, guildSettings)
            matches.size == 1 -> sendMatchMessage(bot, event, matches[0], guildSettings, dateString)
            else -> sendMultiMatchMessage(bot, event, matches, guildSettings, dateString)
        }
    }

    private fun getTimePeriod(dateString: String = ""): TimePeriod? {
        if (dateString.isNotBlank()) {
            try {
                val zoneId = ZoneId.of("US/Eastern")
                val year = Calendar.getInstance(TimeZone.getTimeZone(zoneId)).get(Calendar.YEAR)
                val date = LocalDate.parse("$year/$dateString", DateTimeFormatter.ofPattern("yyyy/M/d"))
                val start = if (date.isBefore(LocalDate.now(zoneId).atStartOfDay().toLocalDate())) {
                    date.plusYears(1).atStartOfDay(zoneId).plusHours(6).toEpochSecond() * 1000
                } else {
                    date.atStartOfDay(ZoneId.of("US/Eastern")).plusHours(6).toEpochSecond() * 1000
                }
                val end = start + Duration.DAY + (6 * Duration.HOUR)
                return TimePeriod(start, end)
            } catch (e: Exception) { }
        }

        return null
    }

    /**
     * Get the default [TimePeriod], which is 1 hour before now until 24 hours after now.
     */
    private fun getDefaultPeriod(): TimePeriod {
        val now = System.currentTimeMillis()
        val oneHourBeforeNow = now - Duration.HOUR
        val oneDayFromNow = now + Duration.DAY
        return TimePeriod(oneHourBeforeNow, oneDayFromNow)
    }

    private fun sendNoMatchesMessage(event: GuildMessageReceivedEvent, guildSettings: GuildSettings) {
        val embedColor = guildSettings.botHighlightColor
        val message = EmbedBuilder()
                .setColor(embedColor)
                .setTitle("There are no matches scheduled.")
                .buildMessage()
        event.channel.sendMessage(message).queue()
    }

    private suspend fun sendMatchMessage(bot: Bot, event: GuildMessageReceivedEvent, match: Match, guildSettings: GuildSettings, dateString: String = "") {
        val content = when {
            dateString.isBlank() -> "Here is the only match for the next 24 hours:"
            else -> "Here is the only match on $dateString:"
        }

        val embedColor = guildSettings.botHighlightColor
        val returnMessage = EmbedBuilder(getMatchesUseCase.mapMatchToMessageEmbed(match))
                .setColor(embedColor)
                .toMessageBuilder()
                .setContent(content)
                .build()
        event.channel.sendMessage(returnMessage).queue { message ->
            bot.serviceHandler.addService(MatchMessageUpdateService(match, message))
        }
    }

    private suspend fun sendMultiMatchMessage(bot: Bot, event: GuildMessageReceivedEvent, matches: List<Match>, guildSettings: GuildSettings, dateString: String = "") {
        val content = when {
            dateString.isBlank() -> "Here are the matches for the next 24 hours:"
            else -> "Here are the matches on $dateString:"
        }

        val embedColor = guildSettings.botHighlightColor
        val returnMessageEmbeds = matches.map { getMatchesUseCase.mapMatchToMessageEmbed(it) }
        val returnMessages = returnMessageEmbeds.mapIndexed { index, embed ->
            EmbedBuilder(embed)
                    .setColor(embedColor)
                    .setTitle(embed.title + "   (${index + 1} of ${returnMessageEmbeds.size})")
                    .toMessageBuilder()
                    .setContent(content)
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