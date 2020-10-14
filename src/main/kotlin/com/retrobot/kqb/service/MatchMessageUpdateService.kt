package com.retrobot.kqb.service

import com.retrobot.core.Duration
import com.retrobot.core.data.GuildSettingsRepository
import com.retrobot.core.domain.service.MessageUpdateService
import com.retrobot.core.util.buildMessage
import com.retrobot.core.util.toBuilder
import com.retrobot.kqb.domain.usecase.GetMatchesUseCase
import com.retrobot.kqb.domain.model.Match
import net.dv8tion.jda.api.entities.Message
import org.koin.core.inject


class MatchMessageUpdateService(
        private val match: Match,
        private val initialMessage: Message,
        updatePeriod: Long = 15 * Duration.MINUTE, // 15 minutes
        duration: Long = 8 * Duration.HOUR // 8 hours
): MessageUpdateService(initialMessage, updatePeriod, duration) {

    private val getMatchesUseCase: GetMatchesUseCase by inject()
    private val guildSettingsRepo: GuildSettingsRepository by inject()

    override suspend fun buildNewMessage(): Message? {
        val embedColor = guildSettingsRepo.get(initialMessage.guild.id).botHighlightColor
        return getMatchesUseCase.mapMatchToMessageEmbed(match)
                .toBuilder()
                .setColor(embedColor)
                .buildMessage()
    }
}