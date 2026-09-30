// models/InsulinDose.kt
package com.example.glucare.models

import java.util.*

data class InsulinDose(
    val id: Int = 0,
    val userId: Int,
    val glucoseValue: Int?,
    val carbs: Int,
    val insulinRatio: Int,
    val calculatedDose: Float,
    val actualDose: Float? = null,
    val timestamp: Long = System.currentTimeMillis() / 1000,
    val timeOfDay: String = getTimeOfDay(timestamp),
    val mealType: String? = null,
    val notes: String? = null,
    val effectiveness: Float? = null,
    val nextGlucoseValue: Int? = null // Glucose reading 2 hours after dose
) {
    companion object {
        fun getTimeOfDay(timestamp: Long): String {
            val cal = Calendar.getInstance()
            cal.timeInMillis = timestamp * 1000
            val hour = cal.get(Calendar.HOUR_OF_DAY)

            return when (hour) {
                in 5..10 -> "morning"
                in 11..14 -> "afternoon"
                in 15..17 -> "evening"
                else -> "night"
            }
        }
    }

    fun getEffectivenessLevel(): String = when {
        effectiveness == null -> "Unknown"
        effectiveness >= 0.8 -> "Excellent"
        effectiveness >= 0.6 -> "Good"
        effectiveness >= 0.4 -> "Fair"
        else -> "Poor"
    }
}