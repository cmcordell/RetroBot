package com.retrobot.steam

import com.retrobot.core.Bot
import com.retrobot.core.Commands.Steam.Steam.CATEGORY
import com.retrobot.core.Commands.Steam.Steam.COMMAND
import com.retrobot.core.Commands.Steam.Steam.DESCRIPTION
import com.retrobot.core.Commands.Steam.Steam.USAGE
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.domain.command.Command
import com.retrobot.core.domain.command.SubCommand
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

/**
 * !steam
 */
class SteamCommand : Command() {
    override val label = COMMAND
    override val category = CATEGORY
    override val description = DESCRIPTION
    override val usage = USAGE

    private val subCommands = listOf<SubCommand>()


    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
//        for (subCommand in subCommands) {
//            if (subCommand.handle(bot, event, args, guildSettings)) return
//        }
//
//        val returnMessage = EmbedBuilder()
//            .setColor(guildSettings.botHighlightColor)
//            .setDescription(MESSAGE_INFO.formatGuildInfo(guildSettings))
//            .build()
//        event.channel.sendMessage(returnMessage).queue()



        val steamService = SteamService()
        val kqbNews = steamService.getNewsForGame("663670")
        println(kqbNews)
    }
}