package com.retrobot.kqb.command

import com.retrobot.core.Bot
import com.retrobot.core.Duration
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.domain.TimePeriod
import com.retrobot.core.domain.command.SubCommand
import com.retrobot.core.domain.reaction.MultiMessageReactionListener
import com.retrobot.core.util.buildMessage
import com.retrobot.core.util.toMessageBuilder
import com.retrobot.kqb.domain.model.Match
import com.retrobot.kqb.domain.usecase.GetMatchesUseCase
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.koin.core.inject
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * !kqb replays
 * !kqb replay last
 */
class ReplaysSubCommand : SubCommand() {
    override val labels = listOf("replays", "replay")
    override val description = "Get past KQB match info"
    override val usage = "!kqb replays\n" +
            "!kqb replay last"

    private val getMatchesUseCase: GetMatchesUseCase by inject()

    open class ReplaysMessage
    data class NoReplaysMessage(val message: String = ""): ReplaysMessage()
    data class SingleReplayMessage(val replay: Match, val message: String = ""): ReplaysMessage()
    data class MultipleReplaysMessage(val replays: List<Match>, val message: String = ""): ReplaysMessage()


    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        val messageType = when {
            args.contains("last", true) -> determineLastReplayMessage()
            else -> determineTimePeriodReplaysMessage(args)
        }

        when (messageType) {
            is NoReplaysMessage -> sendNoReplaysMessage(event, guildSettings, messageType)
            is SingleReplayMessage -> sendReplayMessage(event, guildSettings, messageType)
            is MultipleReplaysMessage -> sendMultiReplayMessage(bot, event, guildSettings, messageType)
        }
    }

    private suspend fun determineLastReplayMessage(): ReplaysMessage {
        return when (val replay = getMatchesUseCase.getLastMatch()) {
            null -> NoReplaysMessage("There aren't anny replays yet this season.")
            else -> SingleReplayMessage(replay, "Here is the last replay:")
        }
    }

    private suspend fun determineTimePeriodReplaysMessage(args: String): ReplaysMessage {
        var dateString = args
        var timePeriod = getTimePeriod(args)
        if (timePeriod == null) {
            dateString = ""
            timePeriod = getDefaultPeriod()
        }

        val replays = getMatchesUseCase.getMatches(timePeriod.start, timePeriod.end)
        return when {
            replays.isEmpty() -> {
                val content = when {
                    dateString.isBlank() -> "There are no replays for the past 24 hours."
                    else -> "There are no replays for $dateString."
                }
                NoReplaysMessage(content)
            }
            replays.size == 1 -> {
                val content = when {
                    dateString.isBlank() -> "Here is the only replay from the past 24 hours:"
                    else -> "Here is the only replay from $dateString:"
                }
                SingleReplayMessage(replays.first(), content)
            }
            else -> {
                val content = when {
                    dateString.isBlank() -> "Here are the replays from the past 24 hours:"
                    else -> "Here are the replays from $dateString:"
                }
                MultipleReplaysMessage(replays, content)
            }
        }
    }

    private fun getTimePeriod(dateString: String = ""): TimePeriod? {
        if (dateString.isNotBlank()) {
            try {
                val zoneId = ZoneId.of("US/Eastern")
                val year = Calendar.getInstance(TimeZone.getTimeZone(zoneId)).get(Calendar.YEAR)
                val date = LocalDate.parse("$year/$dateString", DateTimeFormatter.ofPattern("yyyy/M/d"))
                val start = if (date.isAfter(LocalDate.now(zoneId).atStartOfDay().toLocalDate())) {
                    date.minusYears(1).atStartOfDay(zoneId).plusHours(6).toEpochSecond() * 1000
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
     * Get the default [TimePeriod], which is 24 hours before now until 1 hour before now.
     */
    private fun getDefaultPeriod(): TimePeriod {
        val now = System.currentTimeMillis()
        val oneDayBeforeNow = now - Duration.DAY
        val oneHourBeforeNow = now - Duration.HOUR
        return TimePeriod(oneDayBeforeNow, oneHourBeforeNow)
    }

    private fun sendNoReplaysMessage(event: GuildMessageReceivedEvent, guildSettings: GuildSettings, replaysMessage: NoReplaysMessage) {
        val embedColor = guildSettings.botHighlightColor
        val message = EmbedBuilder()
                .setColor(embedColor)
                .setTitle(replaysMessage.message)
                .buildMessage()
        event.channel.sendMessage(message).queue()
    }

    private suspend fun sendReplayMessage(event: GuildMessageReceivedEvent, guildSettings: GuildSettings, replayMessage: SingleReplayMessage) {
        val embedColor = guildSettings.botHighlightColor
        val returnMessage = EmbedBuilder(getMatchesUseCase.mapReplayToMessageEmbed(replayMessage.replay))
                .setColor(embedColor)
                .toMessageBuilder()
                .setContent(replayMessage.message)
                .build()
        event.channel.sendMessage(returnMessage).queue()
    }

    private suspend fun sendMultiReplayMessage(bot: Bot, event: GuildMessageReceivedEvent, guildSettings: GuildSettings, replayMessage: MultipleReplaysMessage) {
        val embedColor = guildSettings.botHighlightColor
        val returnMessageEmbeds = replayMessage.replays.map { getMatchesUseCase.mapReplayToMessageEmbed(it) }
        val returnMessages = returnMessageEmbeds.mapIndexed { index, embed ->
            EmbedBuilder(embed)
                    .setColor(embedColor)
                    .setFooter("*Replay ${index + 1} of ${returnMessageEmbeds.size}*")
                    .toMessageBuilder()
                    .setContent(replayMessage.message)
                    .build()
        }
        event.channel.sendMessage(returnMessages[0]).queue { message ->
            val reactionListener =
                    MultiMessageReactionListener(
                            message,
                            returnMessages
                    )
            bot.reactionHandler.addReactionListener(message.guild.id, message, reactionListener)
        }
    }
}