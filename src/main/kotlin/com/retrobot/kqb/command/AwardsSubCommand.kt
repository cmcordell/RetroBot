package com.retrobot.kqb.command

import com.retrobot.core.Bot
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.domain.command.SubCommand
import com.retrobot.core.domain.reaction.MultiMessageReactionListener
import com.retrobot.core.util.addFields
import com.retrobot.core.util.buildMessage
import com.retrobot.kqb.KqbUtils.getCircuitCode
import com.retrobot.kqb.KqbUtils.getCircuitName
import com.retrobot.kqb.data.AwardRepository
import com.retrobot.kqb.domain.model.Award
import com.retrobot.kqb.domain.model.AwardType
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.koin.core.inject
import java.text.DecimalFormat

/**
 * !kqb awards
 * !kqb awards <circuit>, <division>, <conference>
 */
class AwardsSubCommand : SubCommand() {
    override val labels = listOf("awards", "award", "medals", "medal")
    override val description = "Get KQB IGL award info"
    override val usage = "!kqb awards\n!kqb awards <circuit>, <division>, <conference>"

    private val awardRepo: AwardRepository by inject()


    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        val embedColor = guildSettings.botHighlightColor
        val returnMessageEmbeds = buildAwardsMessage(args)

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
                    message.guild.id, message, MultiMessageReactionListener(message, returnMessages)
                )
            }
        }
    }

    private suspend fun buildAwardsMessage(circuitInfo: String) : List<MessageEmbed> {
        val circuitInfoSplit = circuitInfo.split(",")
        val circuit = if (circuitInfoSplit.isNotEmpty()) getCircuitCode(circuitInfoSplit[0].trim()) else ""
        val division = if (circuitInfoSplit.size > 1) circuitInfoSplit[1].trim() else ""
        val conference = if (circuitInfoSplit.size > 2) circuitInfoSplit[2].trim() else ""

        return when {
            circuit.isEmpty() -> getThisWeeksAwardPages(awardRepo.getAll())
            else -> getThisWeeksAwardPages(awardRepo.getByCircuit(circuit, division, conference))
        }
    }

    private fun getThisWeeksAwardPages(awards: Collection<Award>) : List<MessageEmbed> {
        val week = awards.maxOf { it.week }
        val awardTypeCount = AwardType.values().size
        val awardsSorted = awards.filter { it.week == week }
            .sortedWith(compareByDescending<Award> { it.week }
                .thenBy { it.division }
                .thenBy { it.conference }
                .thenBy { it.circuit }
                .thenBy { it.awardType })

        val pages = mutableListOf<MessageEmbed>()
        var title = "Awards - Week ${awardsSorted.first().week}"
        var description = "${getCircuitName(awardsSorted.first().circuit)} Circuit - Tier ${awardsSorted.first().division}${awardsSorted.first().conference}"

        val fields = mutableListOf<MessageEmbed.Field>()
        awardsSorted.forEachIndexed { index, award ->
            if (index > 0) {
                if (index % awardTypeCount == 0) {
                    pages.add(EmbedBuilder()
                        .setTitle(title)
                        .setDescription(description)
                        .addFields(fields)
                        .build())
                    title = "Awards - Week ${award.week}"
                    description = "${getCircuitName(award.circuit)} Circuit - Tier ${award.division}${award.conference}"
                    fields.clear()
                }
            }
            val fieldTitle = "${award.awardType.emoji}  ${award.awardType.title}"
            val df = DecimalFormat("0.#")
            val fieldValue = "${award.player} with a ${award.stats.first().name} of ${df.format(award.stats.first().value)}"
            fields.add(MessageEmbed.Field(fieldTitle, fieldValue, true))
        }

        pages.add(EmbedBuilder()
            .setTitle(title)
            .setDescription(description)
            .addFields(fields)
            .build())

        return pages
    }
}