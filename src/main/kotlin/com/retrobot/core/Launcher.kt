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
            // TODO await fix for Koin on Kotlin 1.4 (https://github.com/InsertKoinIO/koin/issues/847)
//            modules(BotModule.modules)
            koin.loadModules(BotModule.modules)
            koin.createRootScope()
        }
    }

    private fun startBot() {
        val token = Properties.config()[Properties.bot.token]
        Bot(token).start()
    }
}