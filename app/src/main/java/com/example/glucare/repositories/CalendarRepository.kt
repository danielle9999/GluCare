package com.example.glucare.repositories

import com.example.glucare.database.DatabaseHelper
import com.example.glucare.models.CalendarEvent
import java.util.*

class CalendarRepository(private val dbHelper: DatabaseHelper) {

    fun addEvent(event: CalendarEvent): Long {
        return dbHelper.insertEvent(event)
    }

    fun getEventsForDay(userId: Int, date: Date): List<CalendarEvent> {
        // Convert Date to Unix timestamp at midnight
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val timestamp = calendar.timeInMillis / 1000

        return dbHelper.getEventsForDay(userId, timestamp)
    }

    fun getEventsForMonth(userId: Int, year: Int, month: Int): Map<Int, List<CalendarEvent>> {
        // This would need a custom query
        return emptyMap()
    }
}