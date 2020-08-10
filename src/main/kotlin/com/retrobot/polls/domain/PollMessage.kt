package com.retrobot.polls.domain

import com.retrobot.core.Emote
import com.retrobot.core.util.Emotes
import com.retrobot.core.util.Markdown
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.time.Instant


object PollMessage {
    fun buildMessageEmbed(poll: Poll) : MessageEmbed {
        val embedBuilder = EmbedBuilder()
                .setTitle("${Emote.BALLOT_BOX} ${Markdown.embolden(poll.title)}")

        for (option in poll.options.values) {
            val fieldTitle = "${Emotes.getEmoteFor(option.number)}  ${Markdown.embolden(option.title)}"
            val total = if (poll.votes < 1) 1 else poll.votes
            val fieldValue = "Votes: ${option.votes} [${String.format("%.02f", option.votes.toDouble()/total*100)}%]\n" +
                    Markdown.codeLine(Emotes.makeProgressBar(poll.votes, option.votes))
            embedBuilder.addField(fieldTitle, fieldValue, true)
        }

        return embedBuilder.setFooter("Last vote cast")
                .setTimestamp(Instant.now())
                .build()
    }
}