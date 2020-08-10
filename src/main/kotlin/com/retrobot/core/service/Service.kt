package com.retrobot.core.service

interface Service {
    val key: String

    fun start()
    fun stop()
    fun isActive(): Boolean
}