package com.retrobot.calendar.data

import com.retrobot.calendar.domain.Event
import com.retrobot.core.domain.TimePeriod

interface EventRepository {
    suspend fun getByCalendar(calendarId: String): List<Event>
    suspend fun getByCalendarAndTimePeriod(calendarId: String, timePeriod: TimePeriod): List<Event>
    suspend fun insert(event: Event)
    suspend fun remove(calendarId: String, eventName: String)
    suspend fun removeAllForCalendar(calendarId: String)
}