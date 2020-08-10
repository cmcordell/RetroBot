package com.retrobot.kqb.service

import com.retrobot.core.Duration
import com.retrobot.core.data.GuildSettingsRepository
import com.retrobot.core.data.exposedrepo.ExposedGuildSettingsRepository
import com.retrobot.core.service.MultiMessageUpdateService
import com.retrobot.core.util.buildMessage
import com.retrobot.core.util.toBuilder
import com.retrobot.kqb.GetMatchesUseCase
import com.retrobot.kqb.domain.Match
import com.retrobot.utility.reaction.MultiMessageReactionListener
import net.dv8tion.jda.api.entities.Message


class MatchMultiMessageUpdateService(
        private val matches: List<Match>,
        private val initialMessage: Message,
        multiMessageReactionListener: MultiMessageReactionListener,
        updatePeriod: Long = 15 * Duration.MINUTE, // 15 minutes
        duration: Long = 8 * Duration.HOUR // 8 hours
): MultiMessageUpdateService(initialMessage.id, multiMessageReactionListener, updatePeriod, duration) {

    private val getMatchesUseCase = GetMatchesUseCase()
    private val guildSettingsRepo: GuildSettingsRepository = ExposedGuildSettingsRepository()

    override suspend fun buildNewMessages(): List<Message> {
        val embedColor = guildSettingsRepo.getGuildSettings(initialMessage.guild.id).botHighlightColor
        val messages = getMatchesUseCase.getMatches(matches).map {
            getMatchesUseCase.mapMatchToMessageEmbed(it)
        }
        return messages.mapIndexed { index, message ->
            message.toBuilder()
                    .setColor(embedColor)
                    .setTitle(message.title + "   (${index + 1} of ${messages.size})")
                    .buildMessage()
        }
    }
}