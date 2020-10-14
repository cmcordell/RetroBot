package com.retrobot.calendar.data.exposedrepo

import com.retrobot.calendar.data.CalendarRepository
import com.retrobot.calendar.data.exposedrepo.CalendarDatabase.Calendars
import com.retrobot.calendar.domain.Calendar
import com.retrobot.core.data.cache.Cache
import com.retrobot.core.util.dbActionQuery
import com.retrobot.core.util.dbQuery
import com.retrobot.core.util.upsert
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select

class ExposedCalendarRepository(
        calendarDatabase: CalendarDatabase,
        private val calendarCache: Cache<String, Calendar>
) : CalendarRepository {

    private val database = Database.connect(calendarDatabase.dataSource)

    override suspend fun getByGuild(guildId: String): Calendar {
        val cachedCalendar = calendarCache[guildId]
        return if (cachedCalendar == null) {
            val calendar = getOrCreateCalendar(guildId)
            calendarCache[guildId] = calendar
            calendar
        } else {
            cachedCalendar
        }
    }

    override suspend fun removeByGuild(guildId: String) {
        calendarCache.remove(guildId)
        dbActionQuery(database) {
            Calendars.deleteWhere { Calendars.guildId eq guildId }
        }
    }

    private suspend fun getOrCreateCalendar(guildId: String): Calendar {
        var calendar = dbQuery(database) {
            Calendars.select { Calendars.guildId eq guildId }
                    .map(this::toCalendar)
                    .firstOrNull()
        }
        if (calendar == null) {
            calendar = Calendar(guildId = guildId, name = "")
            dbActionQuery(database) {
                Calendars.upsert(Calendars.columns) { table ->
                    table[id] = calendar.id
                    table[Calendars.guildId] = calendar.guildId
                    table[name] = calendar.name
                }
            }
        }
        return calendar
    }

    private fun toCalendar(row: ResultRow): Calendar = Calendar(
            id = row[Calendars.id],
            guildId = row[Calendars.guildId],
            name = row[Calendars.name]
    )
}