package com.retrobot.kqb.command

import com.retrobot.core.Bot
import com.retrobot.core.Duration
import com.retrobot.core.domain.command.SubCommand
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.util.addFields
import com.retrobot.core.util.buildMessage
import com.retrobot.core.util.sanitize
import com.retrobot.core.util.toDelimitedString
import com.retrobot.kqb.KqbUtils.convertToEst
import com.retrobot.kqb.data.CasterRepository
import com.retrobot.kqb.data.MatchRepository
import com.retrobot.kqb.data.exposedrepo.ExposedCasterRepository
import com.retrobot.kqb.data.exposedrepo.ExposedMatchRepository
import com.retrobot.kqb.domain.Caster
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

/**
 * !kqb casters
 * !kqb caster <caster name>
 */
class CastersSubCommand : SubCommand() {
    override val labels = setOf("casters", "caster")
    override val description = "Get KQB caster info"
    override val usage = "!kqb casters\n!kqb caster <caster name>"

    private val casterRepo: CasterRepository = ExposedCasterRepository()
    private val matchRepo: MatchRepository = ExposedMatchRepository()


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
            val date = convertToEst(match.date).sanitize()
            val coCasters = match.coCasters.toDelimitedString(", ").sanitize()
            var value = "When: $date"
            if (coCasters.isNotBlank()) {
                value += "\nCo-casters: $coCasters"
            }
            MessageEmbed.Field(matchup, value, false)
        }

        val embedBuilder = EmbedBuilder()

        try {
            embedBuilder.setTitle(title, url)
        } catch (e: Exception) {
            embedBuilder.setTitle(title)
        }

        return when {
            fields.isEmpty() -> embedBuilder.setDescription("This caster has no KQB casts set for the next 7 days.").build()
            else -> embedBuilder.setDescription("Upcoming Casts:").addFields(fields).build()
        }
    }

    private fun buildNarrowCastersMessage(casters: Set<Caster>): MessageEmbed {
        val title = "Multiple Casters Found"
        val description = "Please make your search criteria more specific to match only 1 caster\nCasters found:"
        val casterNames = casters.toDelimitedString("\n") { it.name }.sanitize()
        val casterLinks = casters.toDelimitedString("\n") { it.streamLink }.sanitize()

        return EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .addField("Name", casterNames, true)
                .addField("Stream Link", casterLinks, true)
                .build()
    }

    private suspend fun buildAllCastersMessage(): MessageEmbed {
        val casters = casterRepo.getAll()
        val casterNames = casters.toDelimitedString("\n") { it.name }.sanitize()
        val casterLinks = casters.toDelimitedString("\n") { it.streamLink }.sanitize()
        return EmbedBuilder()
                .setTitle("All Casters")
                .addField("Name", casterNames, true)
                .addField("Stream Link", casterLinks, true)
                .build()
    }
}