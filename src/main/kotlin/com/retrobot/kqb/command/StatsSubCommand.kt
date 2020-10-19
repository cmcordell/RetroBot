package com.retrobot.kqb.command

import com.retrobot.core.Bot
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.domain.command.SubCommand
import com.retrobot.core.util.Properties
import com.retrobot.steam.SteamService
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

/**
 * !kqb stats
 */
class StatsSubCommand : SubCommand() {
    override val labels = listOf("stats", "stat", "statistics", "statistic")
    override val description = "Get KQB stats"
    override val usage = "!kqb stats"

    private val KQB_STEAM_GAME_ID = "663670"

    private val steamService: SteamService = SteamService(Properties.config()[Properties.api.steamKey])


    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        val currentPlayerCount = steamService.getNumberOfCurrentPlayers(KQB_STEAM_GAME_ID)
        val gameSchema = steamService.getSchemaForGame(KQB_STEAM_GAME_ID)

        val embedBuilder = EmbedBuilder()
        if (gameSchema != null) {
            embedBuilder.setTitle("Stats - ${gameSchema.gameName}")
                    .addField(MessageEmbed.Field("Player Count   ", currentPlayerCount.toString(), true))
                    .addField(MessageEmbed.Field("Current Version", gameSchema.gameVersion, true))
                    .build()
        } else {
            embedBuilder.setTitle("Stats - Killer Queen Black")
                    .setDescription("Game stat info could not be loaded.")
                    .build()

        }
        val message = embedBuilder.setColor(guildSettings.botHighlightColor).build()
        event.channel.sendMessage(message).queue()
    }
}