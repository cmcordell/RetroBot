package com.retrobot.twitch

import com.github.twitch4j.helix.domain.Game
import com.retrobot.core.Bot
import com.retrobot.core.Colors
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.domain.command.Command
import com.retrobot.core.domain.command.CommandCategory
import com.retrobot.core.domain.reaction.MultiMessageReactionListener
import com.retrobot.core.domain.service.MultiMessageUpdateService
import com.retrobot.core.util.*
import com.retrobot.twitch.domain.Stream
import com.retrobot.twitch.domain.TwitchStreamsUseCase
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.koin.core.inject
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Find active Twitch streams
 * !streams
 * !streams <game name>
 * TODO !streams game <game name>
 * TODO !streams user <user name>
 */
class StreamsCommand : Command() {
    override val label = "streams"
    override val category = CommandCategory.HIDDEN
    override val description = "Find active Twitch streams"
    override val usage = "!streams game <game name>"

    private val twitchStreamsUseCase: TwitchStreamsUseCase by inject()

    override suspend fun run(bot: Bot, event: GuildMessageReceivedEvent, args: String, guildSettings: GuildSettings) {
        if (args.isBlank()) {
            val messages = twitchStreamsUseCase.getTopStreams().map(this::buildStreamMessage)
            event.channel.sendMessage(messages[0]).queue { message ->
                val multiMessageReactionListener = MultiMessageReactionListener(message, messages)
                bot.reactionHandler.addReactionListener(event.guild.id, message, multiMessageReactionListener)
                bot.serviceHandler.addService(
                    buildTopStreamsMessageUpdateService(message.id, multiMessageReactionListener))
            }
        } else {
            val streams = twitchStreamsUseCase.getStreamsByGameQuery(args)
            if (streams.isEmpty()) {
                val embed = EmbedBuilder()
                    .setColor(Colors.TWITCH_PRIMARY)
                    .setTitle("No streams for $args.")
                    .build()
                event.channel.sendMessage(embed).queue()
            } else {
                val messages = streams.map(this::buildStreamMessage)
                event.channel.sendMessage(messages[0]).queue { message ->
                    val multiMessageReactionListener = MultiMessageReactionListener(message, messages)
                    bot.reactionHandler.addReactionListener(event.guild.id, message, multiMessageReactionListener)
                    bot.serviceHandler.addService(
                        buildGameStreamsMessageUpdateService(
                            message.id,
                            multiMessageReactionListener,
                            streams[0].game
                        )
                    )
                }
            }
        }
    }

    private fun buildStreamMessage(stream: Stream): Message {
        return EmbedBuilder()
            .setColor(Colors.TWITCH_PRIMARY)
            .setTitleAndUrl(stream.title, "https://twitch.tv/${stream.userName}")
            .setThumbnail(stream.game.getBoxArtUrl(188, 250))
            .addField("Game", stream.game.name, true)
            .addField("Streamer", stream.userName.sanitize(), true)
            .addField("Viewers", "${stream.viewerCount}", true)
            .addField("Started At", stream.startedAt.toEpochMilli().convertMillisToTime(ZoneId.of("US/Eastern")), true)
            .addField("Uptime", stream.uptime.format(), true)
            .buildMessage()
    }

    private fun buildTopStreamsMessageUpdateService(
        messageId: String,
        multiMessageReactionListener: MultiMessageReactionListener
    ): MultiMessageUpdateService {
        return MultiMessageUpdateService.build(messageId, multiMessageReactionListener, com.retrobot.core.Duration.MINUTE) {
            val updatedStreams = twitchStreamsUseCase.getTopStreams()
            updatedStreams.map(this::buildStreamMessage).mapIndexed { index, message ->
                message.toEmbedBuilders()
                    .firstOrNull()?.setFooter("*Stream ${index + 1} of ${updatedStreams.size}*")?.buildMessage()
            }.filterNotNull()
        }
    }

    private fun buildGameStreamsMessageUpdateService(
        messageId: String,
        multiMessageReactionListener: MultiMessageReactionListener,
        game: Game
    ): MultiMessageUpdateService {
        return MultiMessageUpdateService.build(messageId, multiMessageReactionListener, TimeUnit.MINUTES.toMillis(7)) {
            val updatedStreams = twitchStreamsUseCase.getStreamsByGame(game)
            updatedStreams.map(this::buildStreamMessage).mapIndexed { index, message ->
                message.toEmbedBuilders()
                    .firstOrNull()?.setFooter("*Stream ${index + 1} of ${updatedStreams.size}*")?.buildMessage()
            }.filterNotNull()
        }
    }
}