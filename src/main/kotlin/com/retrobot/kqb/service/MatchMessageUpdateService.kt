package com.retrobot.kqb.service

import com.retrobot.core.Duration
import com.retrobot.core.data.GuildSettingsRepository
import com.retrobot.core.data.exposedrepo.ExposedGuildSettingsRepository
import com.retrobot.core.service.MessageUpdateService
import com.retrobot.core.util.buildMessage
import com.retrobot.core.util.toBuilder
import com.retrobot.kqb.GetMatchesUseCase
import com.retrobot.kqb.domain.Match
import net.dv8tion.jda.api.entities.Message


class MatchMessageUpdateService(
        private val match: Match,
        private val initialMessage: Message,
        updatePeriod: Long = 15 * Duration.MINUTE, // 15 minutes
        duration: Long = 8 * Duration.HOUR // 8 hours
): MessageUpdateService(initialMessage, updatePeriod, duration) {

    private val getMatchesUseCase = GetMatchesUseCase()
    private val guildSettingsRepo: GuildSettingsRepository = ExposedGuildSettingsRepository()

    override suspend fun buildNewMessage(): Message? {
        val embedColor = guildSettingsRepo.getGuildSettings(initialMessage.guild.id).botHighlightColor
        val match = getMatchesUseCase.getMatch(match)
        return if (match != null) {
            getMatchesUseCase.mapMatchToMessageEmbed(match)
                    .toBuilder()
                    .setColor(embedColor)
                    .buildMessage()
        } else {
            null
        }
    }
}