package com.retrobot.core

import com.retrobot.core.koin.BotModule
import com.retrobot.core.util.Properties
import org.koin.core.context.startKoin

/**
 * Launcher for the Bot.  
 * Any major setup stuff can be done here.
 */
object Launcher {
    @JvmStatic
    fun main(args: Array<String>) {
        startKoin()
        startBot()
    }

    private fun startKoin() {
        startKoin {
            printLogger()
            modules(BotModule.modules)
        }
    }

    private fun startBot() {
        val token = Properties.config()[Properties.bot.token]
        Bot(token).start()
    }
}