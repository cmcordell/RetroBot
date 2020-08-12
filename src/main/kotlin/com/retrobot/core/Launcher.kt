package com.retrobot.core

import com.retrobot.core.util.Properties

object Launcher {
    @JvmStatic
    fun main(args: Array<String>) {
        val token = Properties.config()[Properties.bot.token]
        Bot(token).start()
    }
}