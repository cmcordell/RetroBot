package com.retrobot.core

object Launcher {
    @JvmStatic
    fun main(args: Array<String>) {
        val token = Properties.config()[Properties.bot.token]
        Bot(token).start()
    }
}