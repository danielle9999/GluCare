// services/ReminderManager.kt - FIXED
package com.example.glucare.services

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.glucare.R
import com.example.glucare.activities.CalendarActivity
import com.example.glucare.models.CalendarEvent
import java.util.Calendar

class ReminderManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "glucare_reminders"
        const val CHANNEL_NAME = "Glucare Reminders"
        const val ACTION_REMINDER = "com.example.glucare.REMINDER_ACTION"
        private const val TAG = "ReminderManager"
        private const val EXACT_ALARM_OFFSET = 100_000
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for medications, appointments, and glucose checks"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                enableLights(true)
                setShowBadge(true)
            }
            notificationManager().createNotificationChannel(channel)
        }
    }


    fun scheduleReminder(event: CalendarEvent): Boolean {
        val time = event.time
        if (time.isNullOrEmpty()) {
            Log.w(TAG, "No time specified for event: ${event.title}")
            return false
        }

        return try {
            val parts = time.split(":")
            if (parts.size < 2) return false

            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            // Build Calendar for event time
            val eventCal = Calendar.getInstance().apply {
                timeInMillis = event.date
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val eventTimeMillis = eventCal.timeInMillis
            val now = System.currentTimeMillis()

            // Don't schedule if event is in the past
            if (eventTimeMillis <= now) {
                Log.w(TAG, "Event time is in the past: ${event.title}")
                return false
            }

            val thirtyMinBefore = eventTimeMillis - 30 * 60 * 1000L
            var scheduled = false

            // Alarm 1: 30 minutes before (only if still in the future)
            if (thirtyMinBefore > now) {
                setAlarm(
                    fireAt = thirtyMinBefore,
                    requestCode = event.id,
                    event = event,
                    isExact = false
                )
                scheduled = true
                Log.d(TAG, "30-min reminder scheduled for: ${event.title} at $thirtyMinBefore")
            }

            if (eventTimeMillis > now) {
                setAlarm(
                    fireAt = eventTimeMillis,
                    requestCode = event.id + EXACT_ALARM_OFFSET,
                    event = event,
                    isExact = true
                )
                scheduled = true
                Log.d(TAG, "Exact reminder scheduled for: ${event.title} at $eventTimeMillis")
            }

            scheduled
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling reminder: ${e.message}", e)
            false
        }
    }

    private fun setAlarm(fireAt: Long, requestCode: Int, event: CalendarEvent, isExact: Boolean) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra("event_id", event.id)
            putExtra("event_title", event.title)
            putExtra("event_description", event.description)
            putExtra("event_time", event.time)
            putExtra("event_type", event.eventType.name)
            putExtra("is_exact", isExact)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pi = PendingIntent.getBroadcast(context, requestCode, intent, flags)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    // Android 12+ requires exact alarm permission
                    if (am.canScheduleExactAlarms()) {
                        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt, pi)
                    } else {
                        // Fallback to inexact alarm
                        Log.w(TAG, "Exact alarm permission not granted, using inexact alarm")
                        am.set(AlarmManager.RTC_WAKEUP, fireAt, pi)
                    }
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt, pi)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                    am.setExact(AlarmManager.RTC_WAKEUP, fireAt, pi)
                }
                else -> {
                    am.set(AlarmManager.RTC_WAKEUP, fireAt, pi)
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception when setting alarm: ${e.message}")
            // Fallback to inexact alarm
            am.set(AlarmManager.RTC_WAKEUP, fireAt, pi)
        }
    }


    fun cancelReminder(eventId: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        listOf(eventId, eventId + EXACT_ALARM_OFFSET).forEach { reqCode ->
            try {
                val intent = Intent(context, ReminderReceiver::class.java)
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_NO_CREATE
                }

                val pi = PendingIntent.getBroadcast(context, reqCode, intent, flags)
                pi?.let {
                    am.cancel(it)
                    it.cancel()
                    Log.d(TAG, "Cancelled alarm requestCode=$reqCode")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling reminder: ${e.message}")
            }
        }
    }


    fun canScheduleExact(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return try {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.canScheduleExactAlarms()
        } catch (e: Exception) {
            false
        }
    }


    fun exactAlarmPermissionIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        } else {
            Intent()
        }
    }

    private fun notificationManager() =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderManager.ACTION_REMINDER) return

        val eventId = intent.getIntExtra("event_id", 0)
        val title = intent.getStringExtra("event_title") ?: "Reminder"
        val description = intent.getStringExtra("event_description")
        val time = intent.getStringExtra("event_time") ?: ""
        val eventType = intent.getStringExtra("event_type") ?: "OTHER"
        val isExact = intent.getBooleanExtra("is_exact", false)

        showNotification(context, eventId, title, description, time, eventType, isExact)
    }

    private fun showNotification(
        context: Context,
        eventId: Int,
        title: String,
        description: String?,
        time: String,
        eventType: String,
        isExact: Boolean
    ) {
        val tapIntent = Intent(context, CalendarActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val tapPi = PendingIntent.getActivity(context, eventId, tapIntent, flags)

        // Choose icon based on event type
        val icon = when (eventType) {
            "MEDICATION" -> R.drawable.ic_insulin_small
            "APPOINTMENT" -> R.drawable.ic_doctor
            "MEAL" -> R.drawable.ic_recipe
            "EXERCISE" -> R.drawable.ic_run
            else -> R.drawable.ic_calendar_premium
        }

        // Different message based on reminder type
        val contentText = if (isExact) {
            "🔔 Starting now"
        } else {
            "⏰ Starting in 30 minutes"
        }

        val timeDisplay = if (time.isNotEmpty()) " at $time" else ""

        val notification = NotificationCompat.Builder(context, ReminderManager.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("$contentText$timeDisplay")
            .setSmallIcon(icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(tapPi)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "$contentText$timeDisplay\n\n${description ?: ""}"
            ))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationId = if (isExact) eventId + 100000 else eventId
        notificationManager.notify(notificationId, notification)

        Log.d("ReminderReceiver", "Notification shown: $title | exact=$isExact | id=$notificationId")
    }
}