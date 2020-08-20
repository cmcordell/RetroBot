package com.retrobot.core.domain.command

import com.retrobot.core.Bot
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.util.removePrefixIgnoreCase
import com.retrobot.kqb.command.KqbCompetitionCommand
import com.retrobot.polls.command.PollCommand
import com.retrobot.settings.ColorCommand
import com.retrobot.settings.NicknameCommand
import com.retrobot.settings.PrefixCommand
import com.retrobot.twitch.StreamsCommand
import com.retrobot.utility.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

/**
 * Handler for all [Command]s.
 */
class CommandHandler {
    private val helpCommand = HelpCommand()
    private val commands = listOf(
            // Utilities
            helpCommand,
            PingCommand(),
            CoinflipCommand(),
            RandomCommand(),
            JumboCommand(),
            PollCommand(),

            // Settings
            ColorCommand(),
            NicknameCommand(),
            PrefixCommand(),

            // Twitch
            StreamsCommand(),

            // Killer Queen Black
            KqbCompetitionCommand()
    )

    private val commandMap = commands.map { it.label to it }.toMap()
    private val categoryMap = commands.groupBy { it.category }.mapValues { it.value.toSet() }
    val categories = categoryMap.keys

    /**
     * Attempts to perform a [Command].
     *
     * @param bot The Bot
     * @param event The JDA Event
     * @param guildSettings The [GuildSettings] for the associated [Guild]
     */
    suspend fun perform(bot: Bot, event: GuildMessageReceivedEvent, guildSettings: GuildSettings) {
        if (event.message.contentRaw.startsWith(guildSettings.commandPrefix, true)) {
            val message = event.message.contentRaw.removePrefixIgnoreCase(guildSettings.commandPrefix)
            val label = message.substringBefore(" ")
            val command = commandMap[label]
            if (command != null) {
                val args = message.removePrefix(label).trim()
                command.run(bot, event, args, guildSettings)
            } else {
                helpCommand.run(bot, event, "", guildSettings)
            }
        }
    }

    fun getCategoryByAlias(alias: String) : CommandCategory? {
        categoryMap.keys.forEach { category ->
            category.aliases.forEach {  categoryAlias ->
                if (alias.equals(categoryAlias, true)) {
                    return category
                }
            }
        }
        return null
    }

    fun getCommandsByCategory(category: CommandCategory) : Set<Command> = categoryMap[category] ?: error("Category not in map.")
}