package com.retrobot.kqb.service

import com.retrobot.core.Duration
import com.retrobot.core.data.GuildSettingsRepository
import com.retrobot.core.domain.reaction.MultiMessageReactionListener
import com.retrobot.core.domain.service.MultiMessageUpdateService
import com.retrobot.core.util.buildMessage
import com.retrobot.core.util.toBuilder
import com.retrobot.kqb.domain.model.Match
import com.retrobot.kqb.domain.usecase.GetMatchesUseCase
import net.dv8tion.jda.api.entities.Message
import org.koin.core.inject


class MatchMultiMessageUpdateService(
        private val matches: List<Match>,
        private val initialMessage: Message,
        multiMessageReactionListener: MultiMessageReactionListener,
        updatePeriod: Long = 15 * Duration.MINUTE, // 15 minutes
        duration: Long = 8 * Duration.HOUR // 8 hours
): MultiMessageUpdateService(initialMessage.id, multiMessageReactionListener, updatePeriod, duration) {

    private val getMatchesUseCase: GetMatchesUseCase by inject()
    private val guildSettingsRepo: GuildSettingsRepository by inject()

    override suspend fun buildNewMessages(): List<Message> {
        val embedColor = guildSettingsRepo.get(initialMessage.guild.id).botHighlightColor
        val messages = matches.map { getMatchesUseCase.mapMatchToMessageEmbed(it) }
        return messages.mapIndexed { index, message ->
            message.toBuilder()
                    .setColor(embedColor)
                    .setFooter("*Match ${index + 1} of ${messages.size}*")
                    .buildMessage()
        }
    }
}