package com.example.glucare.repositories

import com.example.glucare.database.DatabaseHelper
import com.example.glucare.models.GlucoseReading

class GlucoseRepository(private val dbHelper: DatabaseHelper) {

    fun addReading(reading: GlucoseReading): Long {
        return dbHelper.insertGlucoseReading(reading)
    }

    fun getLatestReading(userId: Int): GlucoseReading? {
        return dbHelper.getLatestGlucoseReading(userId)
    }

    fun getReadingsForPeriod(userId: Int, days: Int): List<GlucoseReading> {
        // This would need a custom query in DatabaseHelper
        // For now, return empty list
        return emptyList()
    }

    fun getAverageGlucose(userId: Int, days: Int): Float? {
        val readings = getReadingsForPeriod(userId, days)
        if (readings.isEmpty()) return null
        return readings.map { it.value.toFloat() }.average().toFloat()
    }
}