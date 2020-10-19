package com.retrobot.core

import com.retrobot.core.data.exposed.BotDatabaseConfig
import com.retrobot.core.domain.command.CommandHandler
import com.retrobot.core.domain.reaction.ReactionHandler
import com.retrobot.core.domain.service.Service
import com.retrobot.core.domain.service.ServiceHandler
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.requests.GatewayIntent
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named

/**
 * The Bot.
 *
 * @param token The Discord provided token associated with this [Bot].
 */
class Bot(
        private val token: String
) : KoinComponent {
    /**
     * Flags used to convey to the Discord API which [Event]s we want access to.
     * We should limit this to only the [GatewayIntent]s we **need**.
     */
    private val gatewayIntents = listOf(
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS
    )

    val commandHandler: CommandHandler by inject()
    val reactionHandler: ReactionHandler by inject()
    val serviceHandler: ServiceHandler by inject()

    val databaseConfig: BotDatabaseConfig by inject()

    val jda = buildJDA()

    // Services to run on Bot startup
    private val services: List<Service> by inject(named("StartupServices"))


    fun start() {
        databaseConfig.init()
        startServices()
    }

    private fun buildJDA(): JDA = JDABuilder.createLight(token, gatewayIntents)
            .addEventListeners(EventListener(this))
            .setAutoReconnect(true)
            .build()

    private fun startServices() {
        services.forEach { service ->
            serviceHandler.addService(service)
        }
    }
}