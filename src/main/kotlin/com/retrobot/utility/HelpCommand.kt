package com.retrobot.utility

import com.retrobot.core.Bot
import com.retrobot.core.Commands.Utils.Help.CATEGORY
import com.retrobot.core.Commands.Utils.Help.COMMAND
import com.retrobot.core.Commands.Utils.Help.DESCRIPTION
import com.retrobot.core.Commands.Utils.Help.DESCRIPTION_CATEGORIES
import com.retrobot.core.Commands.Utils.Help.DESCRIPTION_COMMANDS
import com.retrobot.core.Commands.Utils.Help.TITLE_CATEGORIES
import com.retrobot.core.Commands.Utils.Help.TITLE_COMMANDS
import com.retrobot.core.Commands.Utils.Help.USAGE
import com.retrobot.core.Discord.Markdown.OP_BOLD
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.domain.command.Command
import com.retrobot.core.domain.command.CommandCategory
import com.retrobot.core.util.formatGuildInfo
import com.retrobot.core.util.toDelimitedString
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.lang.String.format

/**
 * Returns a message with all know commands
 */
class HelpCommand : Command() {
    override val label = COMMAND
    override val category = CATEGORY
    override val description = DESCRIPTION
    override val usage = USAGE


    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        val category = if (args.isEmpty()) null else bot.commandHandler.getCategoryByAlias(args)
        val message = when (category) {
            null -> buildCategoryHelpMessage(bot.commandHandler.categories, guildSettings)
            else -> buildCommandHelpMessage(category, bot.commandHandler.getCommandsByCategory(category), guildSettings)
        }

        event.channel.sendMessage(message).queue()
    }

    private fun buildCategoryHelpMessage(categories: List<CommandCategory>, guildSettings: GuildSettings): Message {
        val sb = StringBuilder(DESCRIPTION_CATEGORIES.formatGuildInfo(guildSettings)).append("\n\n")
        val categoryInfo = categories.filter { it.title.isNotEmpty() }.toDelimitedString("\n") { category ->
            "$OP_BOLD${category.title}$OP_BOLD\n" +
            "${category.description}\n"
        }
        sb.append(categoryInfo)

        val messageEmbed = EmbedBuilder()
                .setTitle(format(TITLE_CATEGORIES).formatGuildInfo(guildSettings))
                .setDescription(sb.toString().formatGuildInfo(guildSettings))
                .setColor(guildSettings.botHighlightColor)
                .build()
        return MessageBuilder(messageEmbed).build()
    }

    private fun buildCommandHelpMessage(category: CommandCategory, commands: List<Command>, guildSettings: GuildSettings): Message {
        val sb = StringBuilder(format(DESCRIPTION_COMMANDS, category.title)).append("\n\n")
        val commandInfo = commands.filter { it.usage.isNotEmpty() }
                .toDelimitedString("\n") { it.getCommandHelpInfo() }
        sb.append(commandInfo)

        val messageEmbed = EmbedBuilder()
                .setTitle(format(TITLE_COMMANDS, category.title).formatGuildInfo(guildSettings))
                .setDescription(sb.toString().formatGuildInfo(guildSettings))
                .setColor(guildSettings.botHighlightColor)
        return MessageBuilder(messageEmbed).build()
    }
}