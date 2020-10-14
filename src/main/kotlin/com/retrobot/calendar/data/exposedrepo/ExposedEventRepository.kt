package com.retrobot.calendar.data.exposedrepo

import com.retrobot.calendar.data.EventRepository
import com.retrobot.calendar.data.exposedrepo.CalendarDatabase.Events
import com.retrobot.calendar.domain.Event
import com.retrobot.core.domain.TimePeriod
import com.retrobot.core.util.dbActionQuery
import com.retrobot.core.util.dbQuery
import com.retrobot.core.util.upsert
import org.jetbrains.exposed.sql.*


class ExposedEventRepository(
        calendarDatabase: CalendarDatabase
) : EventRepository {

    private val database = Database.connect(calendarDatabase.dataSource)

    override suspend fun getByCalendar(calendarId: String): List<Event> = dbQuery(database) {
        Events.select { Events.calendarId eq calendarId }
                .map(this::toEvent)
                .toList()
    }

    override suspend fun getByCalendarAndTimePeriod(calendarId: String, timePeriod: TimePeriod): List<Event> = dbQuery(database) {
        Events.select { Events.calendarId eq calendarId }
                .andWhere { Events.start greaterEq timePeriod.start }
                .andWhere { Events.end lessEq timePeriod.end }
                .map(this::toEvent)
                .toList()
    }

    override suspend fun insert(event: Event) = dbActionQuery(database) {
        Events.upsert(Events.columns) { table ->
            table[id] = event.id
            table[calendarId] = event.calendarId
            table[name] = event.name
            table[start] = event.start
            table[end] = event.end
            table[details] = event.details
        }
    }

    override suspend fun remove(calendarId: String, eventName: String) = dbActionQuery(database) {
        Events.deleteWhere { (Events.calendarId eq calendarId) and (Events.name.upperCase() eq eventName.toUpperCase()) }
    }

    override suspend fun removeAllForCalendar(calendarId: String) = dbActionQuery(database) {
        Events.deleteWhere { Events.calendarId eq calendarId }
    }

    private fun toEvent(row: ResultRow) = Event(
            id = row[Events.id],
            calendarId = row[Events.calendarId],
            name = row[Events.name],
            start = row[Events.start],
            end = row[Events.end],
            details = row[Events.details]
    )
}