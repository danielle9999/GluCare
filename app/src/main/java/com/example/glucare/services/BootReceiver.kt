package com.example.glucare.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.glucare.database.DatabaseHelper
import com.example.glucare.utils.SessionManager

/**
 * BootReceiver — reschedules all active calendar reminders after a device reboot.
 *
 * AlarmManager alarms are cleared on reboot; this receiver restores them so the
 * user still gets notifications even after restarting the phone.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON") return

        Log.d("BootReceiver", "Device rebooted — rescheduling reminders")

        try {
            val session = SessionManager(context)
            if (!session.isLoggedIn()) return

            val userId  = session.getUserId()
            val db      = DatabaseHelper(context)
            val manager = ReminderManager(context)

            // Get all upcoming event dates and reschedule their reminders
            val now = System.currentTimeMillis()
            val eventDates = db.getEventDates(userId)
            var rescheduled = 0

            for (date in eventDates) {
                if (date < now) continue          // skip past days
                val events = db.getEventsForDay(userId, date)
                for (event in events) {
                    if (!event.time.isNullOrEmpty()) {
                        manager.scheduleReminder(event)
                        rescheduled++
                    }
                }
            }
            Log.d("BootReceiver", "Rescheduled $rescheduled reminders")
        } catch (e: Exception) {
            Log.e("BootReceiver", "Error rescheduling: ${e.message}", e)
        }
    }
}