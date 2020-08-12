package com.retrobot.core

import com.retrobot.core.command.CommandSet
import com.retrobot.core.data.DatabaseFactory
import com.retrobot.core.data.GuildSettingsRepository
import com.retrobot.core.data.exposedrepo.ExposedGuildSettingsRepository
import com.retrobot.core.reactionhandler.ReactionHandler
import com.retrobot.core.service.ReactionListenerCleanupService
import com.retrobot.core.service.ServiceCleanupService
import com.retrobot.core.service.ServiceHandler
import com.retrobot.kqb.command.KqbCompetitionCommand
import com.retrobot.kqb.service.KqbAlmanacService
import com.retrobot.polls.command.PollCommand
import com.retrobot.settings.ColorCommand
import com.retrobot.settings.NicknameCommand
import com.retrobot.settings.PrefixCommand
import com.retrobot.utility.command.*
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent

/**
 * The Bot.
 *
 * @param token The Discord provided token associated with this [Bot].
 */
class Bot(
        private val token: String
) {
    /**
     * Flags used to convey to the Discord API which [Event]s we want access to.
     * We should limit this to only the [GatewayIntent]s we **need**.
     */
    private val gatewayIntents = setOf(
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS
    )

    val commandSet = CommandSet(
            // Utilities
            HelpCommand(),
            PingCommand(),
            CoinflipCommand(),
            RandomCommand(),
            JumboCommand(),
            PollCommand(),

            // Settings
            ColorCommand(),
            NicknameCommand(),
            PrefixCommand(),

            // Game specific
            KqbCompetitionCommand()
    )

    val serviceHandler = ServiceHandler()
    val reactionHandler = ReactionHandler()
    val jda = buildJDA()

    // Services to run on Bot startup
    private val services = listOf(
            ServiceCleanupService(serviceHandler),
            ReactionListenerCleanupService(reactionHandler),
            KqbAlmanacService()
    )


    fun start() {
        startDatabases()
        startServices()
    }

    private fun buildJDA(): JDA = JDABuilder.createLight(token, gatewayIntents)
            .addEventListeners(EventListener(this))
            .setAutoReconnect(true)
            .build()

    private fun startDatabases() {
        DatabaseFactory.connect()
    }

    private fun startServices() {
        services.forEach { service ->
            serviceHandler.addService(service)
        }
    }
}