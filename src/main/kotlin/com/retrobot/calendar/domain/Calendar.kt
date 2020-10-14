package com.retrobot.calendar.domain

import java.util.*

data class Calendar(
        val id: String = UUID.randomUUID().toString(),
        val guildId: String,
        val name: String
)