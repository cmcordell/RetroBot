package com.retrobot.kqb.command

import com.retrobot.core.Bot
import com.retrobot.core.Duration
import com.retrobot.core.command.SubCommand
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.util.Markdown
import com.retrobot.core.util.buildMessage
import com.retrobot.core.util.sanitize
import com.retrobot.core.util.toDelimitedString
import com.retrobot.kqb.KqbUtils.convertToEst
import com.retrobot.kqb.KqbUtils.getCircuitName
import com.retrobot.kqb.KqbUtils.percent
import com.retrobot.kqb.data.MatchRepository
import com.retrobot.kqb.data.TeamRepository
import com.retrobot.kqb.data.exposedrepo.ExposedMatchRepository
import com.retrobot.kqb.data.exposedrepo.ExposedTeamRepository
import com.retrobot.kqb.domain.Team
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.lang.String.format

/**
 * !kqb teams
 * !kqb team <team name>
 */
class TeamsSubCommand : SubCommand() {
    override val labels = setOf("teams", "team")
    override val description = "Get KQB team info"
    override val usage = "!kqb teams\n!kqb team <team name>"

    private val matchRepo: MatchRepository = ExposedMatchRepository()
    private val teamRepo: TeamRepository = ExposedTeamRepository()


    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        val embedColor = guildSettings.botHighlightColor
        val returnMessage = EmbedBuilder(buildTeamMessage(args))
                .setColor(embedColor)
                .buildMessage()
        event.channel.sendMessage(returnMessage).queue()
    }

    private suspend fun buildTeamMessage(teamName: String): MessageEmbed {
        if (teamName.isBlank()) return buildAllTeamsMessage()

        val teams = teamRepo.getByName(teamName)
        return when {
            teams.isEmpty() -> buildAllTeamsMessage()
            teams.size == 1 -> buildTeamInfoMessage(teams.first())
            else -> buildNarrowTeamsMessage(teams)
        }
    }

    private suspend fun buildTeamInfoMessage(team: Team): MessageEmbed {
        val title = team.name.sanitize()
        val description = "${team.season} ${getCircuitName(team.circuit)} Circuit - Tier ${team.division}${team.conference}\n" +
                "Captain: ${team.captain.sanitize()}\n" +
                "Members: ${team.members.toDelimitedString().sanitize()}"

        val stats = "Matches: Wins: ${team.matchesWon} | Losses: ${team.matchesLost} | Win%: ${format("%.1f", percent(team.matchesWon, team.matchesPlayed))}%\n" +
                "Sets: Wins: ${team.setsWon} | Losses: ${team.setsLost} | Win%: ${format("%.1f", percent(team.setsWon, team.setsPlayed))}%"

        val now = System.currentTimeMillis()
        val oneWeekFromNow = now + Duration.WEEK
        val upcomingMatches = matchRepo.getByTeam(team.name)
                .filter { it.date in now until oneWeekFromNow }
                .sortedBy { it.date }
        val matchesFieldSb = StringBuilder()
        upcomingMatches.forEachIndexed { index, match ->
            if (index > 0) matchesFieldSb.append("\n")
            matchesFieldSb.append(Markdown.embolden("${match.awayTeam.sanitize()} vs ${match.homeTeam.sanitize()}\n"))
            matchesFieldSb.append("When: ${convertToEst(match.date).sanitize()}\n")
            matchesFieldSb.append("Caster: ${match.caster.sanitize()}")
        }
        if (upcomingMatches.isEmpty()) {
            matchesFieldSb.clear().append("This team has no matches scheduled for the next week.")
        }

        return EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .addField("Stats", stats, false)
                .addField("Upcoming Matches:", matchesFieldSb.toString(), false)
                .build()
    }

    private fun buildNarrowTeamsMessage(teams: Set<Team>): MessageEmbed {
        val title = "Multiple Teams Found"
        val description = "Please make your search criteria more specific to match only 1 team\nTeams found:"
        val teamNames = teams.toDelimitedString("\n") { it.name }.sanitize()
        val teamCircuits = teams.toDelimitedString("\n") {
            "${getCircuitName(it.circuit)} ${it.division}${it.conference}"
        }

        return EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .addField("Name", teamNames, true)
                .addField("Circuit", teamCircuits, true)
                .build()
    }

    private suspend fun buildAllTeamsMessage(): MessageEmbed {
        val eastField = teamRepo.getByCircuit("E").toDelimitedString("\n") { it.name }.sanitize()
        val westField = teamRepo.getByCircuit("W").toDelimitedString("\n") { it.name }.sanitize()

        return EmbedBuilder()
                .setTitle("All Teams")
                .addField("East", eastField, true)
                .addField("West", westField, true)
                .build()
    }
}