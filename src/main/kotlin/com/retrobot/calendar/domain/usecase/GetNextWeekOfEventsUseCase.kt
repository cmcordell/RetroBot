package com.retrobot.calendar.domain.usecase

import com.retrobot.calendar.data.CalendarRepository
import com.retrobot.calendar.data.EventRepository
import com.retrobot.calendar.domain.Event
import com.retrobot.core.Duration
import com.retrobot.core.domain.TimePeriod
import java.time.LocalDate
import java.time.ZoneId

class GetNextWeekOfEventsUseCase(
        private val calendarRepository: CalendarRepository,
        private val eventRepository: EventRepository
) {
    suspend fun execute(guildId: String): List<Event> {
        val start = LocalDate.now(ZoneId.of("US/Eastern")).atStartOfDay(ZoneId.of("US/Eastern")).toEpochSecond() * 1000
        val end = start + Duration.WEEK
        val calendarId = calendarRepository.getByGuild(guildId).guildId
        return eventRepository.getByCalendarAndTimePeriod(calendarId, TimePeriod(start, end))
    }
}