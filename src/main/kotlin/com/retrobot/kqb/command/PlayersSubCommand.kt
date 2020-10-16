package com.retrobot.kqb.command

import com.retrobot.core.Bot
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.domain.command.SubCommand
import com.retrobot.core.util.*
import com.retrobot.kqb.domain.model.Award
import com.retrobot.kqb.domain.model.AwardType
import com.retrobot.kqb.domain.model.TeamWithMatches
import com.retrobot.kqb.domain.usecase.GetPlayerUseCase
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.koin.core.inject
import java.time.ZoneId

/**
 * !kqb player <player name>
 */
class PlayersSubCommand : SubCommand() {
    override val labels = setOf("players", "player")
    override val description = "Get KQB player info"
    override val usage = "!kqb player <player name>"

    private val getPlayerUseCase: GetPlayerUseCase by inject()

// TODO Maybe add multi player message if too many players are found with matching names
    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        val embedColor = guildSettings.botHighlightColor
        val returnMessage = EmbedBuilder(buildPlayerMessage(args))
                .setColor(embedColor)
                .buildMessage()
        event.channel.sendMessage(returnMessage).queue()
    }

    private suspend fun buildPlayerMessage(playerName: String) : MessageEmbed {
        val player = getPlayerUseCase.getPlayerByName(playerName)
        if (player == null) {
            val title = "No player found with name $playerName".sanitize()
            return EmbedBuilder()
                .setTitle(title)
                .build()
        }

        val correctPlayerName = player.name.sanitize()
        val teamMatchFields = buildTeamsMatchFields(player.teamsWithMatches)
        val awardsField = buildAwardsField(player.awards)

        return EmbedBuilder()
            .setTitle(correctPlayerName)
            .setDescription("Upcoming Matches:")
            .addFields(teamMatchFields)
            .addField(awardsField)
            .build()
    }

    private fun buildTeamsMatchFields(teamsWithMatches: List<TeamWithMatches>): List<MessageEmbed.Field> {
        val teamMatchFields = mutableListOf<MessageEmbed.Field>()
        teamsWithMatches.forEach { teamMatchFields.add(buildTeamMatchField(it)) }
        return teamMatchFields
    }

    private fun buildTeamMatchField(teamWithMatches: TeamWithMatches): MessageEmbed.Field {
        val sb = StringBuilder()
        teamWithMatches.matches.mapIndexed { index, match ->
            val matchup = when {
                teamWithMatches.team.name.equals(match.awayTeam, true) ->
                    "${Markdown.italicize(match.awayTeam.sanitize())} vs ${match.homeTeam.sanitize()}"
                teamWithMatches.team.name.equals(match.homeTeam, true) ->
                    "${match.awayTeam.sanitize()} vs ${Markdown.italicize(match.homeTeam.sanitize())}"
                else -> "${match.awayTeam} vs ${match.homeTeam}".sanitize()
            }

            val date = match.date.convertMillisToTime(ZoneId.of("US/Eastern"))
            if (index > 0) { sb.append("\n\n") }
            sb.append(Markdown.quoteLine(matchup) + "\n" +
                    Markdown.quoteLine("When: $date") + "\n" +
                    Markdown.quoteLine("Caster: ${match.caster.sanitize()}"))
        }

        if (teamWithMatches.matches.isEmpty()) {
            sb.append(Markdown.quoteLine("This team does not have any upcoming matches."))
        }

        return MessageEmbed.Field(teamWithMatches.team.name, sb.toString(), false)
    }

    private fun buildAwardsField(awards: List<Award>): MessageEmbed.Field? {
        if (awards.isEmpty()) return null

        val awardCounts = mutableMapOf<AwardType, Int>()
        awards.forEach { award ->
            awardCounts[award.awardType] = awardCounts[award.awardType]?.let { it + 1 } ?: 1
        }

        val sb = StringBuilder()
        awardCounts.forEachIndexed { index, entry ->
            if (index > 0) sb.append("\n")
            sb.append(Markdown.quoteLine("${entry.key.emoji} ${entry.key.title} x ${entry.value}"))
        }

        return MessageEmbed.Field("Awards", sb.toString(), false)
    }
}