package com.retrobot.kqb.command

import com.retrobot.core.Bot
import com.retrobot.core.Duration
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.domain.command.SubCommand
import com.retrobot.core.util.*
import com.retrobot.kqb.data.CasterRepository
import com.retrobot.kqb.data.MatchRepository
import com.retrobot.kqb.domain.model.Caster
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.koin.core.inject
import java.time.ZoneId

/**
 * !kqb casters
 * !kqb caster <caster name>
 */
class CastersSubCommand : SubCommand() {
    override val labels = listOf("casters", "caster", "casts", "cast", "streams", "stream")
    override val description = "Get KQB caster info"
    override val usage = "!kqb casters\n!kqb caster <caster name>"

    private val casterRepo: CasterRepository by inject()
    private val matchRepo: MatchRepository by inject()


    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        val embedColor = guildSettings.botHighlightColor
        val returnMessage = EmbedBuilder(buildCasterMessage(args))
                .setColor(embedColor)
                .buildMessage()
        event.channel.sendMessage(returnMessage).queue()
    }

    private suspend fun buildCasterMessage(casterName: String): MessageEmbed {
        if (casterName.isBlank()) return buildAllCastersMessage()

        val casters = casterRepo.getByName(casterName)
        return when {
            casters.isEmpty() -> buildAllCastersMessage()
            casters.size == 1 -> buildCasterInfoMessage(casters.first())
            else -> buildNarrowCastersMessage(casters)
        }
    }

    private suspend fun buildCasterInfoMessage(caster: Caster): MessageEmbed {
        val now = System.currentTimeMillis()
        val oneWeekFromNow = now + Duration.WEEK
        val upcomingMatches = matchRepo.getByCaster(caster.name)
                .filter { it.date in now until oneWeekFromNow }
                .sortedBy { it.date }

        val title = caster.name.sanitize()
        val url = "https://www.${caster.streamLink}".sanitize()
        val fields = upcomingMatches.map { match ->
            val matchup = "${match.awayTeam} vs ${match.homeTeam}".sanitize()
            val date = match.date.convertMillisToTime(ZoneId.of("US/Eastern"))
            val coCasters = match.coCasters.toDelimitedString(", ").sanitize()
            var value = "When: $date"
            if (coCasters.isNotBlank()) {
                value += "\nCo-casters: $coCasters"
            }
            MessageEmbed.Field(matchup, value, false)
        }

        val description = when {
            fields.isEmpty() -> "This caster has no KQB casts set for the next 7 days."
            else -> "Upcoming Casts:"
        }
        return EmbedBuilder()
            .setTitleAndUrl(title, url)
            .setDescription(description)
            .addFields(fields)
            .build()
    }

    private fun buildNarrowCastersMessage(casters: List<Caster>): MessageEmbed {
        val title = "Multiple Casters Found"
        val description = "Please make your search criteria more specific to match only 1 caster\nCasters found:"
        val casterNames = casters.toDelimitedString("\n") { it.name }.sanitize()
        val casterLinks = casters.toDelimitedString("\n") { it.streamLink }.sanitize()
        val casterGamesCasted = casters.toDelimitedString("\n") { it.gamesCasted.toString() }

        return EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .addField("Name", casterNames, true)
                .addField("Stream Link", casterLinks, true)
                .addField("Games Casted", casterGamesCasted, true)
                .build()
    }

    private suspend fun buildAllCastersMessage(): MessageEmbed {
        val casters = casterRepo.getAll()
        val casterNames = casters.toDelimitedString("\n") { it.name }.sanitize()
        val casterLinks = casters.toDelimitedString("\n") { it.streamLink }.sanitize()
        val casterGamesCasted = casters.toDelimitedString("\n") { it.gamesCasted.toString() }
        return EmbedBuilder()
                .setTitle("All Casters")
                .addField("Name", casterNames, true)
                .addField("Stream Link", casterLinks, true)
                .addField("Games Casted", casterGamesCasted, true)
                .build()
    }
}