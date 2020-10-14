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
import java.util.concurrent.TimeUnit

/**
 * !kqb matches
 * !kqb matches 9/22
 * !kqb match next
 */
class MatchesSubCommand : SubCommand() {
    override val labels = setOf("matches", "matchs", "match", "games", "game")
    override val description = "Get KQB match info"
    override val usage = "!kqb matches\n" +
            "!kqb matches <month>/<day> i.e. 9/22\n" +
            "!kqb match next"

    private val getMatchesUseCase: GetMatchesUseCase by inject()

    open class MatchesMessage
    data class NoMatchesMessage(val message: String = ""): MatchesMessage()
    data class SingleMatchMessage(val match: Match, val message: String = ""): MatchesMessage()
    data class MultipleMatchesMessage(val matches: List<Match>, val message: String = ""): MatchesMessage()


    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        val messageType = when {
            args.contains("next", true) -> determineNextMatchMessage()
            else -> determineTimePeriodMatchesMessage(args)
        }

        when (messageType) {
            is NoMatchesMessage -> sendNoMatchesMessage(event, guildSettings, messageType)
            is SingleMatchMessage -> sendMatchMessage(bot, event, guildSettings, messageType)
            is MultipleMatchesMessage -> sendMultiMatchMessage(bot, event, guildSettings, messageType)
        }
    }

    private suspend fun determineNextMatchMessage(): MatchesMessage {
        return when (val match = getMatchesUseCase.getNextMatch()) {
            null -> NoMatchesMessage("There are no matches scheduled for the foreseeable future.")
            else -> SingleMatchMessage(match, "Here is the next match:")
        }
    }

    private suspend fun determineTimePeriodMatchesMessage(args: String): MatchesMessage {
        var dateString = args
        var timePeriod = getTimePeriod(args)
        if (timePeriod == null) {
            dateString = ""
            timePeriod = getDefaultPeriod()
        }

        val matches = getMatchesUseCase.getMatches(timePeriod.start, timePeriod.end)
        return when {
            matches.isEmpty() -> {
                val content = when {
                    dateString.isBlank() -> "There are no matches scheduled today."
                    else -> "There are no matches scheduled for $dateString."
                }
                NoMatchesMessage(content)
            }
            matches.size == 1 -> {
                val content = when {
                    dateString.isBlank() -> "Here is the only match scheduled today:"
                    else -> "Here is the only match scheduled for $dateString:"
                }
                SingleMatchMessage(matches.first(), content)
            }
            else -> {
                val content = when {
                    dateString.isBlank() -> "Here are the matches scheduled today:"
                    else -> "Here are the matches scheduled for $dateString:"
                }
                MultipleMatchesMessage(matches, content)
            }
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

    private fun sendNoMatchesMessage(event: GuildMessageReceivedEvent, guildSettings: GuildSettings, matchesMessage: NoMatchesMessage) {
        val embedColor = guildSettings.botHighlightColor
        val message = EmbedBuilder()
                .setColor(embedColor)
                .setTitle(matchesMessage.message)
                .buildMessage()
        event.channel.sendMessage(message).queue()
    }

    private suspend fun sendMatchMessage(bot: Bot, event: GuildMessageReceivedEvent, guildSettings: GuildSettings, matchMessage: SingleMatchMessage) {
        val embedColor = guildSettings.botHighlightColor
        val returnMessage = EmbedBuilder(getMatchesUseCase.mapMatchToMessageEmbed(matchMessage.match))
                .setColor(embedColor)
                .toMessageBuilder()
                .setContent(matchMessage.message)
                .build()
        event.channel.sendMessage(returnMessage).queue { message ->
            bot.serviceHandler.addService(MatchMessageUpdateService(matchMessage.match, message))
        }
    }

    private suspend fun sendMultiMatchMessage(bot: Bot, event: GuildMessageReceivedEvent, guildSettings: GuildSettings, matchMessage: MultipleMatchesMessage) {
        val embedColor = guildSettings.botHighlightColor
        val returnMessageEmbeds = matchMessage.matches.map { getMatchesUseCase.mapMatchToMessageEmbed(it) }
        val returnMessages = returnMessageEmbeds.mapIndexed { index, embed ->
            EmbedBuilder(embed)
                    .setColor(embedColor)
                    .setFooter("*Match ${index + 1} of ${returnMessageEmbeds.size}*")
                    .toMessageBuilder()
                    .setContent(matchMessage.message)
                    .build()
        }
        event.channel.sendMessage(returnMessages[0]).queue { message ->
            val reactionListener =
                MultiMessageReactionListener(
                    message,
                    returnMessages
                )
            bot.reactionHandler.addReactionListener(message.guild.id, message, reactionListener)
            bot.serviceHandler.addService(MatchMultiMessageUpdateService(matchMessage.matches, message, reactionListener, TimeUnit.MINUTES.toMillis(7)))
        }
    }
}