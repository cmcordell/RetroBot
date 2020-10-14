package com.retrobot.calendar.data

import com.retrobot.calendar.domain.Calendar

interface CalendarRepository {
    suspend fun getByGuild(guildId: String): Calendar
    suspend fun removeByGuild(guildId: String)
}