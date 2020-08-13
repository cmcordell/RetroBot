package com.retrobot.kqb.command

import com.retrobot.core.Bot
import com.retrobot.core.Duration
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.domain.command.SubCommand
import com.retrobot.core.util.Markdown
import com.retrobot.core.util.addFields
import com.retrobot.core.util.buildMessage
import com.retrobot.core.util.sanitize
import com.retrobot.kqb.KqbUtils.convertToEst
import com.retrobot.kqb.data.MatchRepository
import com.retrobot.kqb.data.TeamRepository
import com.retrobot.kqb.domain.model.Team
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.koin.core.inject

/**
 * !kqb player <player name>
 */
class PlayersSubCommand : SubCommand() {
    override val labels = setOf("players", "player")
    override val description = "Get KQB player info"
    override val usage = "!kqb player <player name>"

    private val matchRepo: MatchRepository by inject()
    private val teamRepo: TeamRepository by inject()

// TODO Maybe add multi player message if too many players are found with matching names
    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        val embedColor = guildSettings.botHighlightColor
        val returnMessage = EmbedBuilder(buildPlayerMessage(args))
                .setColor(embedColor)
                .buildMessage()
        event.channel.sendMessage(returnMessage).queue()
    }

    private suspend fun buildPlayerMessage(playerName: String) : MessageEmbed {
        val teams = teamRepo.getByMember(playerName).filter { team -> isPlayerMemberOfTeam(playerName, team) }
        if (teams.isEmpty()) {
            val title = "No player found with name $playerName".sanitize()
            return EmbedBuilder()
                    .setTitle(title)
                    .build()
        }

        val correctPlayerName = getMatchingMember(playerName, teams[0]).sanitize()
        val now = System.currentTimeMillis()
        val oneWeekFromNow = now + Duration.WEEK
        val teamMatchFields = mutableListOf<MessageEmbed.Field>()
        for (team in teams) {
            var value = ""
            val upcomingMatches = matchRepo.getByTeam(team.name)
                    .filter { it.date in now until oneWeekFromNow }
                    .sortedBy { it.date }
                    .mapIndexed { index, match ->
                        val matchup = when {
                            team.name.equals(match.awayTeam, true) -> "${Markdown.italicize(match.awayTeam.sanitize())} vs ${match.homeTeam.sanitize()}"
                            team.name.equals(match.homeTeam, true) -> "${match.awayTeam.sanitize()} vs ${Markdown.italicize(match.homeTeam.sanitize())}"
                            else -> "${match.awayTeam} vs ${match.homeTeam}".sanitize()
                        }

                        val date = convertToEst(match.date).sanitize()
                        if (index > 0) { value += "\n\n" }
                        value += Markdown.quoteLine(matchup) + "\n" +
                                Markdown.quoteLine("When: $date") + "\n" +
                                Markdown.quoteLine("Caster: ${match.caster.sanitize()}")
                    }

            if (upcomingMatches.isEmpty()) {
                value = Markdown.quoteLine("This team does not have any matches scheduled for the next week.")
            }

            teamMatchFields.add(MessageEmbed.Field(team.name, value, false))
        }

        return EmbedBuilder()
                .setTitle(correctPlayerName)
                .setDescription("Upcoming Matches:")
                .addFields(teamMatchFields)
                .build()
    }

    private fun isPlayerMemberOfTeam(player: String, team: Team) : Boolean {
        for (member in team.members) {
            if (member.equals(player, true)) {
                return true
            }
        }
        return false
    }

    private fun getMatchingMember(player: String, team: Team) : String {
        for (member in team.members) {
            if (member.equals(player, true)) {
                return member
            }
        }
        return player
    }
}