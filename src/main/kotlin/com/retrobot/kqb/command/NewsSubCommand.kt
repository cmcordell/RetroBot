package com.retrobot.kqb.command

import com.retrobot.core.Bot
import com.retrobot.core.Discord.Markdown.OP_BOLD
import com.retrobot.core.Discord.Markdown.OP_CODE_BLOCK
import com.retrobot.core.Discord.Markdown.OP_ITALIC
import com.retrobot.core.Discord.Markdown.OP_STRIKE
import com.retrobot.core.Discord.Markdown.OP_UNDERLINE
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.domain.command.SubCommand
import com.retrobot.core.domain.reaction.MultiMessageReactionListener
import com.retrobot.core.util.*
import com.retrobot.steam.SteamService
import com.retrobot.steam.moshi.entity.NewsItem
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.time.ZoneId

/**
 * !kqb news
 * !kqb news 5
 */
class NewsSubCommand : SubCommand() {
    override val labels = listOf("news", "new", "headlines", "headline", "updates", "update")
    override val description = "Get KQB news"
    override val usage = "!kqb news\n!kqb news 5"

    private val KQB_STEAM_GAME_ID = "663670"

    private val steamService: SteamService = SteamService()


    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        val count = args.toIntOrDefault(10).coerceIn(1, 10)
        val newsItems = steamService.getNewsForGame(KQB_STEAM_GAME_ID, count)

        when (newsItems.size) {
            0 -> sendNoNewsMessage(event, guildSettings)
            1 -> sendSingleNewsMessage(event, guildSettings, newsItems.first())
            else -> sendMultipleNewsMessage(bot, event, guildSettings, newsItems)
        }
    }

    private fun sendNoNewsMessage(event: GuildMessageReceivedEvent, guildSettings: GuildSettings) {
        val embedColor = guildSettings.botHighlightColor
        val message = EmbedBuilder()
            .setColor(embedColor)
            .setTitle("There are no KQB news items.")
            .buildMessage()
        event.channel.sendMessage(message).queue()
    }

    private fun sendSingleNewsMessage(event: GuildMessageReceivedEvent, guildSettings: GuildSettings, newsItem: NewsItem) {
        val embedColor = guildSettings.botHighlightColor
        val returnMessage = buildNewsItemMessageEmbed(newsItem)
            .toBuilder()
            .setColor(embedColor)
            .toMessageBuilder()
            .build()
        event.channel.sendMessage(returnMessage).queue()
    }

    private fun sendMultipleNewsMessage(bot: Bot, event: GuildMessageReceivedEvent, guildSettings: GuildSettings, newsItems: List<NewsItem>) {
        val embedColor = guildSettings.botHighlightColor
        val returnMessageEmbeds = buildNewsItemsMessageEmbeds(newsItems)
        val returnMessages = returnMessageEmbeds.mapIndexed { index, embed ->
            EmbedBuilder(embed)
                .setColor(embedColor)
                .setFooter("*News Item ${index + 1} of ${returnMessageEmbeds.size}*")
                .toMessageBuilder()
                .build()
        }
        event.channel.sendMessage(returnMessages[0]).queue { message ->
            val reactionListener = MultiMessageReactionListener(message, returnMessages)
            bot.reactionHandler.addReactionListener(message.guild.id, message, reactionListener)
        }
    }

    private fun buildNewsItemsMessageEmbeds(newsItems: List<NewsItem>): List<MessageEmbed> {
        return newsItems.map(this::buildNewsItemMessageEmbed)
    }

    // TODO Try cutting off long articles around newline characters
    private fun buildNewsItemMessageEmbed(newsItem: NewsItem): MessageEmbed {
        val description = if (newsItem.contents.length > 800) {
            newsItem.contents.take(800).plus(Typography.ellipsis).plus("\n\n(Click link to see full article)")
        } else {
            newsItem.contents
        }
        val publishedDate = newsItem.date.convertMillisToTime(ZoneId.of("US/Eastern"))

        return EmbedBuilder().setTitleAndUrl(newsItem.title, newsItem.url)
            .setDescription(convertSteamTextMarkupToDiscordTextMarkup(description))
            .addField("Author", newsItem.author, true)
            .addField("Feed", newsItem.feedLabel, true)
            .addField("Published", publishedDate, true)
            .build()
    }

    // TODO Create a Steam Format parser. Things like urls, quotes, tables, ordered lists cannot be done with .replace()
    private fun convertSteamTextMarkupToDiscordTextMarkup(text: String): String {
        return text.replace("[h1]", OP_BOLD).replace("[/h1]", OP_BOLD)
            .replace("[h2]", OP_BOLD).replace("[/h2]", OP_BOLD)
            .replace("[h3]", OP_BOLD).replace("[/h3]", OP_BOLD)
            .replace("[b]", OP_BOLD).replace("[/b]", OP_BOLD)
            .replace("[u]", OP_UNDERLINE).replace("[/u]", OP_UNDERLINE)
            .replace("[i]", OP_ITALIC).replace("[/i]", OP_ITALIC)
            .replace("[strike]", OP_STRIKE).replace("[/strike]", OP_STRIKE)
            .replace("[list]", "").replace("[/list]", "")
            .replace("[*]", "•")
            .replace("[code]", OP_CODE_BLOCK).replace("[/code]", OP_CODE_BLOCK)
    }
}