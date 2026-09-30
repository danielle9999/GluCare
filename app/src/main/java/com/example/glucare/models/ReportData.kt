// models/ReportData.kt
package com.example.glucare.models

import java.util.*

data class GlucoseStats(
    val averageGlucose: Float,
    val minGlucose: Int,
    val maxGlucose: Int,
    val standardDeviation: Float,
    val timeInRange: RangeStats,
    val readingsByTimeOfDay: Map<String, Float> // morning, afternoon, evening, night
)

data class RangeStats(
    val low: Int,        // < 70
    val normal: Int,     // 70-140
    val elevated: Int,   // 141-180
    val high: Int        // > 180
)

data class InsulinStats(
    val totalDailyDose: Float,
    val averageDose: Float,
    val averageCarbs: Int,
    val insulinToCarbRatio: Float,
    val effectivenessScore: Float, // 0-100%
    val dosesByTimeOfDay: Map<String, Float>
)

data class DailySummary(
    val date: String,
    val glucose: Int,
    val insulin: Float,
    val carbs: Int,
    val notes: String
)

data class ReportPeriod(
    val startDate: Date,
    val endDate: Date,
    val label: String // "Last 7 Days", "Last 30 Days", etc.
)