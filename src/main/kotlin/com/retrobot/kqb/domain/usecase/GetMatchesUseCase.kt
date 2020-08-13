package com.retrobot.kqb.domain.usecase

import com.retrobot.core.Duration
import com.retrobot.core.Emote
import com.retrobot.core.util.sanitize
import com.retrobot.core.util.toDelimitedString
import com.retrobot.kqb.KqbUtils
import com.retrobot.kqb.data.CasterRepository
import com.retrobot.kqb.data.MatchRepository
import com.retrobot.kqb.data.TeamRepository
import com.retrobot.kqb.domain.model.Caster
import com.retrobot.kqb.domain.model.ColorScheme
import com.retrobot.kqb.domain.model.Match
import com.retrobot.kqb.domain.model.Team
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.util.concurrent.TimeUnit


class GetMatchesUseCase(
    private val casterRepo: CasterRepository,
    private val matchRepo: MatchRepository,
    private val teamRepo: TeamRepository
) {

    // TODO Consider Moving any function that use Message or MessageEmbed to it own KQBMessage util class
    suspend fun getMatches(from: Long, to: Long): List<Match> {
        return matchRepo.getByDate(from, to).sortedBy { it.date }
    }

    suspend fun getMatches(matches: List<Match>): List<Match> {
        return matches.mapNotNull { getMatch(it) }
    }

    suspend fun getMatch(match: Match): Match? {
        return matchRepo.getByWeekAndTeams(match.week, match.awayTeam, match.homeTeam)
    }

    suspend fun mapMatchToMessageEmbed(match: Match): MessageEmbed {
        val awayTeam = teamRepo.getByName(match.awayTeam).firstOrNull()
        val homeTeam = teamRepo.getByName(match.homeTeam).firstOrNull()
        val caster = casterRepo.getByName(match.caster).firstOrNull()

        val title = "${match.awayTeam} vs ${match.homeTeam}".sanitize()
        val whenField = MessageEmbed.Field("When", KqbUtils.convertToEst(match.date), true)
        val countdownField = MessageEmbed.Field("Countdown", getCountdown(match), true)
        val casterField = MessageEmbed.Field("Caster Info", getCasterInfo(caster, match) + "\n", false)
        val awayTeamField = getAwayTeamField(awayTeam, match.colorScheme)
        val homeTeamField = getHomeTeamField(homeTeam, match.colorScheme)

        return EmbedBuilder()
                .setTitle(title)
                .addField(whenField)
                .addField(countdownField)
                .addField(casterField)
                .addField(awayTeamField)
                .addField(homeTeamField)
                .build()
    }

    private fun getCountdown(match: Match): String {
        if (match.winner.isNotBlank()) return "Finished"
        val millis = match.date - System.currentTimeMillis()
        if (millis < 0) return "Started"

        val days = TimeUnit.MILLISECONDS.toDays(millis)
        val hours = TimeUnit.MILLISECONDS.toHours(millis % Duration.DAY)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis % Duration.HOUR)
        val sb = StringBuilder()
        if (days > 0) {
            val unit = if (days == 1L) "Day" else "Days"
            sb.append("$days $unit")
        }
        if (hours > 0 || sb.isNotBlank()) {
            if (sb.isNotBlank()) sb.append(" ")
            val unit = if (hours == 1L) "Hour" else "Hours"
            sb.append("$hours $unit")
        }
        if (minutes > 0 || sb.isNotBlank()) {
            if (sb.isNotBlank()) sb.append(" ")
            val unit = if (minutes == 1L) "Minute" else "Minutes"
            sb.append("$minutes $unit")
        }
        if (sb.isBlank()) {
            sb.clear().append("< 1 Minute")
        }
        return sb.toString()
    }

    private fun getCasterInfo(caster: Caster?, match: Match): String {
        when {
            caster == null -> return if (match.streamLink.isBlank()) "No cast" else match.streamLink
            caster.name.equals("Other", true) -> return match.streamLink
            caster.name.equals("Looking for caster", true) -> return "Looking for caster"
            caster.name.equals("No cast", true) -> return "No cast"
        }

        val sb = StringBuilder(caster?.name?.sanitize())

        val coCasters = match.coCasters.filter(String::isNotBlank)
        if (coCasters.isNotEmpty()) {
            val prefix = if (match.coCasters.size > 1) "CoCasters" else "CoCaster"
            val coCastersLine = match.coCasters.toDelimitedString(", ").sanitize()
            sb.append("\n$prefix: $coCastersLine")
        }
        if (match.streamLink.isNotBlank()) {
            val url = when {
                match.streamLink.startsWith("https://www.", true) -> match.streamLink
                match.streamLink.startsWith("http://www.", true) -> match.streamLink
                match.streamLink.startsWith("www.") -> "https://${match.streamLink}"
                else -> "https://www.${match.streamLink}"
            }
            sb.append("\n$url")
        }

        return sb.toString()
    }

    private fun getAwayTeamField(awayTeam: Team?, colorScheme: ColorScheme): MessageEmbed.Field {
        val away = colorScheme == ColorScheme.DEFAULT
        return getTeamField(awayTeam, away)
    }

    private fun getHomeTeamField(homeTeam: Team?, colorScheme: ColorScheme): MessageEmbed.Field {
        val away = colorScheme == ColorScheme.SWAP
        return getTeamField(homeTeam, away)
    }

    private fun getTeamField(team: Team?, away: Boolean): MessageEmbed.Field {
        if (team == null) return MessageEmbed.Field("Team info missing", "", false)

        val colorEmoji = if (away) Emote.Unicode.SQUARE_BLUE else Emote.Unicode.SQUARE_ORANGE
        val title = "$colorEmoji ${team.name}".sanitize()
        val value = "Match Record: ${team.matchesWon}-${team.matchesLost}   |   Set Record: ${team.setsWon}-${team.setsLost}\n" +
                team.members.toDelimitedString(", ").sanitize()
        return MessageEmbed.Field(title, value, false, false)
    }
}