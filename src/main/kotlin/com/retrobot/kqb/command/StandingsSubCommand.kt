package com.retrobot.kqb.command

import com.retrobot.core.Bot
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.domain.command.SubCommand
import com.retrobot.core.domain.reaction.MultiMessageReactionListener
import com.retrobot.core.util.Markdown.codeBlock
import com.retrobot.core.util.buildMessage
import com.retrobot.kqb.KqbUtils.getCircuitCode
import com.retrobot.kqb.KqbUtils.getCircuitName
import com.retrobot.kqb.KqbUtils.percent
import com.retrobot.kqb.data.TeamRepository
import com.retrobot.kqb.domain.model.Team
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.koin.core.inject
import java.lang.String.format

/**
 * !kqb standings
 * !kqb standings <circuit>, <division>, <conference>
 */
class StandingsSubCommand : SubCommand() {
    override val labels = setOf("standings", "standing")
    override val description = "Get KQB team standings info"
    override val usage = "!kqb standings\n!kqb standings <circuit>, <division>, <conference>"

    private val teamRepo: TeamRepository by inject()

// TODO If we return this as a plain message instead of message embed, standings can be much wider
    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        val embedColor = guildSettings.botHighlightColor
        val returnMessageEmbeds = buildStandingsMessage(args)

        if (returnMessageEmbeds.size == 1) {
            val returnMessage = EmbedBuilder(returnMessageEmbeds[0])
                    .setColor(embedColor)
                    .buildMessage()
            event.channel.sendMessage(returnMessage).queue()
        } else {
            val returnMessages = returnMessageEmbeds.mapIndexed { index, messageEmbed ->
                EmbedBuilder(messageEmbed)
                        .setColor(embedColor)
                        .setTitle(messageEmbed.title)
                        .setFooter("*${index + 1} of ${returnMessageEmbeds.size}*")
                        .buildMessage()
            }
            event.channel.sendMessage(returnMessages[0]).queue { message ->
                bot.reactionHandler.addReactionListener(
                        message.guild.id, message, MultiMessageReactionListener(message, returnMessages))
            }
        }
    }

    private suspend fun buildStandingsMessage(circuitInfo: String) : List<MessageEmbed> {
        val circuitInfoSplit = circuitInfo.split(",")
        val circuit = if (circuitInfoSplit.isNotEmpty()) getCircuitCode(circuitInfoSplit[0].trim()) else ""
        val division = if (circuitInfoSplit.size > 1) circuitInfoSplit[1].trim() else ""
        val conference = if (circuitInfoSplit.size > 2) circuitInfoSplit[2].trim() else ""

        return when {
            circuit.isEmpty() -> buildAllTeamStandingsMessage()
            else -> buildCircuitTeamStandingsMessage(circuit, division, conference)
        }
    }

    private suspend fun buildCircuitTeamStandingsMessage(circuit: String, division: String, conference: String) : List<MessageEmbed> {
        val teams = teamRepo.getByCircuit(circuit, division, conference)
        val title = "Team Standings - ${getCircuitName(circuit)} $division$conference"

        return if (teams.isEmpty()) {
            val errorMessage = "Could not find circuit based on input.\nExample input: !kqb standings West, 1, b\n"
            buildAllTeamStandingsMessage().map { message ->
                EmbedBuilder(message)
                        .setDescription(errorMessage + message.description)
                        .build()
            }
        } else {
            buildTeamStandingsMessage(title, teams)
        }
    }

    private suspend fun buildAllTeamStandingsMessage() : List<MessageEmbed> {
        val teams = teamRepo.getAll()
        val title = "Team Standings - Overall"
        return buildTeamStandingsMessage(title, teams)
    }

    private fun buildTeamStandingsMessage(title: String, teams: Collection<Team>) : List<MessageEmbed> {
        val sortedTeams = teams.sortedWith(compareByDescending<Team> { percent(it.matchesWon, it.matchesPlayed) }
                .thenBy { it.matchesLost }
                .thenByDescending { it.matchesWon }
                .thenByDescending { percent(it.setsWon, it.setsPlayed ) }
                .thenBy { it.setsLost }
                .thenByDescending { it.setsWon }
                .thenBy { it.division })
        val pages = getTeamStandingsPages(sortedTeams)
        val messages = mutableListOf<MessageEmbed>()
        pages.forEach { page ->
            messages.add(
                    EmbedBuilder()
                            .setTitle(title)
                            .setDescription(page)
                            .build()
            )
        }
        return messages
    }

    private fun getTeamStandingsPages(teams: Collection<Team>) : List<String> {
        val rowHeaders = "   |       |                          |Match|Match|Set  |Set \n" +
                         "   |Circuit|Team Name                 |W-L  |Win% |W-L  |Win%\n" +
                         "---+-------+--------------------------+-----+-----+-----+----"
        val rowFormat = "%3s|%-7s|%-26.26s|%5s|%5s|%5s|%4s"
        val page = 25
        val pages = mutableListOf<String>()
        val sb = StringBuilder(rowHeaders)
        teams.forEachIndexed { index, team ->
            if (index > 0 && index % page == 0) {
                pages.add(codeBlock(sb.toString()))
                sb.clear().append(rowHeaders)
            }
            sb.append("\n")
            sb.append(format(rowFormat, index+1, getCircuitInfo(team), team.name, getMatchRecord(team), getMatchWinPercent(team), getSetRecord(team), getSetWinPercent(team)))
        }
        pages.add(codeBlock(sb.toString()))

        return pages
    }

    private fun getCircuitInfo(team: Team) : String {
        return "${getCircuitName(team.circuit)} ${team.division}${team.conference}"
    }

    private fun getMatchRecord(team: Team) : String {
        return "${team.matchesWon}-${team.matchesLost}"
    }
    
    private fun getMatchWinPercent(team: Team) : String {
        return format("%.0f%%", percent(team.matchesWon, team.matchesPlayed))
    }

    private fun getSetRecord(team: Team) : String {
        return "${team.setsWon}-${team.setsLost}"
    }

    private fun getSetWinPercent(team: Team) : String {
        return format("%.0f%%", percent(team.setsWon, team.setsPlayed))
    }
}