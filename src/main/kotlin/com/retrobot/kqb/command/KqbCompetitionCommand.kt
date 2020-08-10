package com.retrobot.kqb.command

import com.retrobot.core.Bot
import com.retrobot.core.Commands.KQB.Competition.CATEGORY
import com.retrobot.core.Commands.KQB.Competition.COMMAND
import com.retrobot.core.Commands.KQB.Competition.DESCRIPTION
import com.retrobot.core.Commands.KQB.Competition.MESSAGE_INFO
import com.retrobot.core.Commands.KQB.Competition.USAGE
import com.retrobot.core.command.Command
import com.retrobot.core.util.formatGuildInfo
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

/**
 * !kqb
 * !kqb casters
 * !kqb caster <caster name>
 * !kqb matches
 * !kqb player <player name (full)>
 * !kqb teams
 * !kqb team <team name>
 * !kqb standings
 * !kqb standings <circuit>, <division>, <conference> | e.g. !kqb standings West, 1, b
 */
class KqbCompetitionCommand : Command() {
    override val label = COMMAND
    override val category = CATEGORY
    override val description = DESCRIPTION
    override val usage = USAGE

    // TODO Change sub command usage to us Specifiers
    private val subCommands = setOf(
            CastersSubCommand(),
            MatchesSubCommand(),
            PlayersSubCommand(),
            TeamsSubCommand(),
            StandingsSubCommand()
    )

    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String) {
        for (subCommand in subCommands) {
            if (subCommand.handle(bot, event, args)) return
        }

        val guildSettings = bot.guildSettingsRepo.getGuildSettings(event.guild.id)
        val returnMessage = EmbedBuilder()
                .setColor(guildSettings.botHighlightColor)
                .setDescription(MESSAGE_INFO.formatGuildInfo(guildSettings))
                .build()
        event.channel.sendMessage(returnMessage).queue()
    }
}