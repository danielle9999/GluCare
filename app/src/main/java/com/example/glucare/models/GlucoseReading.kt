package com.example.glucare.models

data class GlucoseReading(
    val id: Int = 0,
    val userId: Int,
    val value: Int,
    val readingType: ReadingType,
    val timestamp: Long = System.currentTimeMillis() / 1000,
    val notes: String? = null
) {
    enum class ReadingType {
        FASTING,
        BEFORE_MEAL,
        AFTER_MEAL,
        BEDTIME,
        RANDOM
    }

    fun getStatus(): GlucoseStatus {
        return when {
            value < 70 -> GlucoseStatus.LOW
            value in 70..140 -> GlucoseStatus.NORMAL
            value in 141..180 -> GlucoseStatus.ELEVATED
            else -> GlucoseStatus.HIGH
        }
    }

    enum class GlucoseStatus {
        LOW, NORMAL, ELEVATED, HIGH
    }
}