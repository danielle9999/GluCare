package com.example.glucare.models
import com.example.glucare.R  // Make sure this is present!

data class CalendarEvent(
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val description: String? = null,
    val eventType: EventType,
    val date: Long, // Unix timestamp for the day (midnight)
    val time: String? = null, // Format: "HH:mm"
    val location: String? = null,
    val isCompleted: Boolean = false,
    val notificationTime: Long? = null  // ← ADD THIS LINE
) {
    enum class EventType {
        MEDICATION,
        APPOINTMENT,
        MEAL,
        EXERCISE,
        OTHER
    }

    fun getFormattedDateTime(): String {
        return if (time != null) {
            "$time"
        } else {
            "All day"
        }
    }

    fun getIcon(): Int {
        return when (eventType) {
            EventType.MEDICATION -> R.drawable.ic_glucose
            EventType.APPOINTMENT -> R.drawable.ic_doctor
            EventType.MEAL -> R.drawable.ic_recipe
            EventType.EXERCISE -> R.drawable.ic_steps
            EventType.OTHER -> R.drawable.ic_calendar_premium
        }
    }

    fun getColor(): Int {
        return when (eventType) {
            EventType.MEDICATION -> R.color.primary_blue
            EventType.APPOINTMENT -> R.color.primary_purple
            EventType.MEAL -> R.color.primary_green
            EventType.EXERCISE -> R.color.warning_orange
            EventType.OTHER -> R.color.text_secondary
        }
    }
}