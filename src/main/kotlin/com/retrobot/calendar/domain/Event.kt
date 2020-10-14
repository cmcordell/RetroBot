package com.retrobot.calendar.domain

import java.util.*

data class Event(
        val id: String = UUID.randomUUID().toString(),
        val calendarId: String,
        val name: String,
        val start: Long,
        val end: Long,
        val details: String
) {
    val duration: Long = end - start
}