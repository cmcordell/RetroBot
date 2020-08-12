package com.retrobot.core

import com.retrobot.core.koin.botModules
import com.retrobot.core.util.Properties
import org.koin.core.context.startKoin

object Launcher {
    @JvmStatic
    fun main(args: Array<String>) {
        startKoin()
        startBot()
    }

    private fun startKoin() {
        startKoin {
            printLogger()
            modules(botModules)
        }
    }

    private fun startBot() {
        val token = Properties.config()[Properties.bot.token]
        Bot(token).start()
    }
}