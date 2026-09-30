package com.example.glucare.activities

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.app.AlarmManager
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.glucare.GlucareApplication
import com.example.glucare.R
import com.example.glucare.adapters.CalendarEventAdapter
import com.example.glucare.database.DatabaseHelper
import com.example.glucare.models.CalendarEvent
import com.example.glucare.services.ReminderManager
import com.example.glucare.utils.SessionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : BaseActivity() {

    private lateinit var app: GlucareApplication
    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager
    private lateinit var eventsAdapter: CalendarEventAdapter
    private lateinit var tvMonthYear: TextView
    private lateinit var tvSelectedDate: TextView
    private lateinit var tvNoEvents: TextView
    private lateinit var gridCalendar: GridView
    private lateinit var rvEvents: RecyclerView
    private lateinit var fabAddEvent: FloatingActionButton
    private lateinit var reminderManager: ReminderManager

    private val calendar = Calendar.getInstance()
    private var selectedDateTimestamp = 0L
    private var eventDates = mutableSetOf<Long>()
    private var glucoseDates = mutableSetOf<Long>()

    private val sdfDisplay = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    private val sdfMonthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val sdfTime = SimpleDateFormat("h:mm a", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        app = application as GlucareApplication
        db = app.databaseHelper
        session = app.sessionManager
        reminderManager = ReminderManager(this)

        requestNotificationPermission()
        initViews()
        setupRecyclerView()
        setupClickListeners()

        val jumpDate = intent.getLongExtra("selected_date", 0L)
        if (jumpDate != 0L) {
            val cal = Calendar.getInstance().apply { timeInMillis = jumpDate }
            calendar.set(Calendar.YEAR, cal.get(Calendar.YEAR))
            calendar.set(Calendar.MONTH, cal.get(Calendar.MONTH))
            updateSelectedDate(cal)
        } else {
            updateSelectedDate(Calendar.getInstance())
        }

        loadEventDatesForMonth()
        loadGlucoseDatesForMonth()
    }

    override fun onResume() {
        super.onResume()
        loadEventDatesForMonth()
        loadGlucoseDatesForMonth()
        loadEventsForDate(selectedDateTimestamp)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(
                    this,
                    "Enable exact alarms in Settings → Apps → Glucare → Alarms & reminders for reliable reminders",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun initViews() {
        tvMonthYear = findViewById(R.id.tvMonthYear)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        tvNoEvents = findViewById(R.id.tvNoEvents)
        rvEvents = findViewById(R.id.eventsRecyclerView)
        gridCalendar = findViewById(R.id.calendarView)
        fabAddEvent = findViewById(R.id.fabAddEvent)
    }

    private fun setupRecyclerView() {
        eventsAdapter = CalendarEventAdapter(mutableListOf()) { event ->
            showEventOptionsDialog(event)
        }
        rvEvents.layoutManager = LinearLayoutManager(this)
        rvEvents.adapter = eventsAdapter
    }

    private fun setupClickListeners() {
        findViewById<ImageView>(R.id.btnPrevMonth).setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            loadEventDatesForMonth()
            loadGlucoseDatesForMonth()
        }
        findViewById<ImageView>(R.id.btnNextMonth).setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            loadEventDatesForMonth()
            loadGlucoseDatesForMonth()
        }
        fabAddEvent.setOnClickListener { showAddEditEventDialog(null) }
    }

    private fun renderCalendar() {
        tvMonthYear.text = sdfMonthYear.format(calendar.time)
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDay = cal.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val days = mutableListOf<CalendarDay>()
        repeat(firstDay) { days.add(CalendarDay.empty()) }
        (1..daysInMonth).forEach { num ->
            val ts = Calendar.getInstance().apply {
                set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, num)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            days.add(
                CalendarDay(
                    num, ts,
                    hasEvents = eventDates.contains(ts),
                    hasGlucose = glucoseDates.contains(ts),
                    isToday = ts == todayMidnight(),
                    isSelected = ts == selectedDateTimestamp
                )
            )
        }
        gridCalendar.adapter = CalendarGridAdapter(days) { day ->
            if (!day.isEmpty) updateSelectedDate(
                Calendar.getInstance().also { it.timeInMillis = day.timestamp })
        }
    }

    private fun updateSelectedDate(cal: Calendar) {
        selectedDateTimestamp = midnight(cal)
        tvSelectedDate.text = "📅 " + sdfDisplay.format(cal.time)
        loadEventsForDate(selectedDateTimestamp)
    }

    private fun midnight(cal: Calendar): Long = (cal.clone() as Calendar).also {
        it.set(Calendar.HOUR_OF_DAY, 0)
        it.set(Calendar.MINUTE, 0)
        it.set(Calendar.SECOND, 0)
        it.set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun todayMidnight() = midnight(Calendar.getInstance())

    private fun loadEventDatesForMonth() {
        lifecycleScope.launch {
            val dates = withContext(Dispatchers.IO) {
                db.getEventDatesForMonth(
                    session.getUserId(),
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH)
                )
            }
            eventDates.clear()
            eventDates.addAll(dates)
            renderCalendar()
        }
    }

    private fun loadGlucoseDatesForMonth() {
        lifecycleScope.launch {
            val dates = withContext(Dispatchers.IO) {
                db.getGlucoseDatesForMonth(
                    session.getUserId(),
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH)
                )
            }
            glucoseDates.clear()
            glucoseDates.addAll(dates)
            renderCalendar()
        }
    }

    private fun loadEventsForDate(ts: Long) {
        lifecycleScope.launch {
            val events = withContext(Dispatchers.IO) {
                db.getEventsForDay(session.getUserId(), ts)
            }
            tvNoEvents.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
            rvEvents.visibility = if (events.isEmpty()) View.GONE else View.VISIBLE
            eventsAdapter.updateData(events)
        }
    }

    private fun showAddEditEventDialog(existing: CalendarEvent?) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_event, null)
        val etTitle = view.findViewById<TextInputEditText>(R.id.etEventTitle)
        val etDesc = view.findViewById<TextInputEditText>(R.id.etEventDescription)
        val etTime = view.findViewById<TextInputEditText>(R.id.etEventTime)
        val etLoc = view.findViewById<TextInputEditText>(R.id.etEventLocation)
        val rgType = view.findViewById<RadioGroup>(R.id.rgEventType)
        val cbRem = view.findViewById<CheckBox>(R.id.cbSetReminder)

        existing?.let {
            etTitle.setText(it.title)
            etDesc.setText(it.description)
            etTime.setText(it.time)
            etLoc.setText(it.location)
            when (it.eventType) {
                CalendarEvent.EventType.APPOINTMENT -> rgType.check(R.id.rbAppointment)
                CalendarEvent.EventType.MEAL -> rgType.check(R.id.rbMeal)
                CalendarEvent.EventType.EXERCISE -> rgType.check(R.id.rbExercise)
                else -> rgType.check(R.id.rbMedication)
            }
        } ?: rgType.check(R.id.rbMedication)

        etTime.setOnClickListener {
            val now = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, h, m ->
                    etTime.setText(String.format("%02d:%02d", h, m))
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
            ).show()
        }

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Add Event" else "Edit Event")
            .setView(view)
            .setPositiveButton(if (existing == null) "Add" else "Save") { _, _ ->
                val title = etTitle.text.toString().trim()
                if (title.isEmpty()) {
                    Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val type = when (rgType.checkedRadioButtonId) {
                    R.id.rbAppointment -> CalendarEvent.EventType.APPOINTMENT
                    R.id.rbMeal -> CalendarEvent.EventType.MEAL
                    R.id.rbExercise -> CalendarEvent.EventType.EXERCISE
                    else -> CalendarEvent.EventType.MEDICATION
                }

                val event = CalendarEvent(
                    id = existing?.id ?: 0,
                    userId = session.getUserId(),
                    title = title,
                    description = etDesc.text.toString().ifEmpty { null },
                    eventType = type,
                    date = selectedDateTimestamp,
                    time = etTime.text.toString().ifEmpty { null },
                    location = etLoc.text.toString().ifEmpty { null }
                )

                lifecycleScope.launch {
                    val savedId = withContext(Dispatchers.IO) {
                        if (existing == null) {
                            db.insertEvent(event).toInt()
                        } else {
                            db.updateEvent(event)
                            existing.id
                        }
                    }

                    if (savedId <= 0) {
                        runOnUiThread {
                            Toast.makeText(this@CalendarActivity, "Failed to save event", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    val savedEvent = event.copy(id = savedId)

                    // ========== FIX: Schedule BOTH reminders ==========
                    if (cbRem.isChecked && !savedEvent.time.isNullOrEmpty()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                            if (!alarmManager.canScheduleExactAlarms()) {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@CalendarActivity,
                                        "Exact alarm permission needed for reminders. Please grant permission in Settings.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                startActivity(reminderManager.exactAlarmPermissionIntent())
                            } else {
                                // THIS schedules BOTH reminders (30-min AND exact time)
                                val success = reminderManager.scheduleReminder(savedEvent)
                                runOnUiThread {
                                    if (success) {
                                        Toast.makeText(
                                            this@CalendarActivity,
                                            "✅ Reminders set: 30 min before and at ${savedEvent.time}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this@CalendarActivity,
                                            "⚠️ Could not set reminders. Time may be too soon or in the past.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        } else {
                            val success = reminderManager.scheduleReminder(savedEvent)
                            runOnUiThread {
                                if (success) {
                                    Toast.makeText(
                                        this@CalendarActivity,
                                        "✅ Reminders set: 30 min before and at ${savedEvent.time}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@CalendarActivity,
                                        "⚠️ Could not set reminders. Time may be too soon or in the past.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    } else if (cbRem.isChecked && savedEvent.time.isNullOrEmpty()) {
                        runOnUiThread {
                            Toast.makeText(
                                this@CalendarActivity,
                                "Please set a time to enable reminders",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    runOnUiThread {
                        loadEventDatesForMonth()
                        loadEventsForDate(selectedDateTimestamp)
                        Toast.makeText(
                            this@CalendarActivity,
                            if (existing == null) "Event added!" else "Event updated!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEventOptionsDialog(event: CalendarEvent) {
        AlertDialog.Builder(this)
            .setTitle(event.title)
            .setItems(arrayOf("View Details", "Edit", "Delete", "Set Reminder")) { _, which ->
                when (which) {
                    0 -> showEventDetails(event)
                    1 -> showAddEditEventDialog(event)
                    2 -> confirmDelete(event)
                    3 -> {
                        if (!event.time.isNullOrEmpty()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                if (!alarmManager.canScheduleExactAlarms()) {
                                    Toast.makeText(this, "Exact alarm permission needed", Toast.LENGTH_SHORT).show()
                                    startActivity(reminderManager.exactAlarmPermissionIntent())
                                    return@setItems
                                }
                            }
                            // THIS schedules BOTH reminders
                            val success = reminderManager.scheduleReminder(event)
                            if (success) {
                                Toast.makeText(this, "✅ Reminders set (30 min before and at event time)", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "⚠️ Could not set reminders", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "Set a time first", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }.show()
    }

    private fun showEventDetails(event: CalendarEvent) {
        val dateStr = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            .format(Date(event.date))
        val timeStr = event.time?.let {
            try {
                val p = it.split(":")
                val c = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, p[0].toInt())
                    set(Calendar.MINUTE, p[1].toInt())
                }
                sdfTime.format(c.time)
            } catch (e: Exception) {
                it
            }
        } ?: "All day"

        val icon = when (event.eventType) {
            CalendarEvent.EventType.MEDICATION -> "💊"
            CalendarEvent.EventType.APPOINTMENT -> "👨‍⚕️"
            CalendarEvent.EventType.MEAL -> "🍽️"
            CalendarEvent.EventType.EXERCISE -> "🏃"
            else -> "📌"
        }

        AlertDialog.Builder(this)
            .setTitle("Event Details")
            .setMessage("$icon ${event.title}\n\n📅 $dateStr\n⏰ $timeStr\n📍 ${event.location ?: "No location"}\n📝 ${event.description ?: "No description"}")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun confirmDelete(event: CalendarEvent) {
        AlertDialog.Builder(this)
            .setTitle("Delete Event")
            .setMessage("Delete '${event.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                reminderManager.cancelReminder(event.id)
                lifecycleScope.launch {
                    val ok = withContext(Dispatchers.IO) { db.deleteEvent(event.id) }
                    if (ok) runOnUiThread {
                        loadEventDatesForMonth()
                        loadEventsForDate(selectedDateTimestamp)
                        Toast.makeText(this@CalendarActivity, "Deleted", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null).show()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            Toast.makeText(
                this,
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED)
                    "Notifications enabled!" else "Reminders disabled.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    data class CalendarDay(
        val day: Int = 0,
        val timestamp: Long = 0L,
        val hasEvents: Boolean = false,
        val hasGlucose: Boolean = false,
        val isToday: Boolean = false,
        val isSelected: Boolean = false,
        val isEmpty: Boolean = false
    ) {
        companion object {
            fun empty() = CalendarDay(isEmpty = true)
        }
    }

    inner class CalendarGridAdapter(
        private val days: List<CalendarDay>,
        private val onClick: (CalendarDay) -> Unit
    ) : BaseAdapter() {
        override fun getCount() = days.size
        override fun getItem(pos: Int) = days[pos]
        override fun getItemId(pos: Int) = pos.toLong()

        override fun getView(pos: Int, cv: View?, parent: ViewGroup): View {
            val view = cv ?: LayoutInflater.from(parent.context)
                .inflate(R.layout.item_calendar_day, parent, false)
            val tvDay = view.findViewById<TextView>(R.id.tvDay)
            val vIndicator = view.findViewById<View>(R.id.vIndicator)
            val vGlucoseDot = view.findViewById<View>(R.id.vGlucoseDot)
            val day = days[pos]

            if (day.isEmpty) {
                tvDay.text = ""
                vIndicator.visibility = View.INVISIBLE
                vGlucoseDot.visibility = View.INVISIBLE
                view.isClickable = false
                return view
            }

            tvDay.text = day.day.toString()
            when {
                day.isSelected -> {
                    view.setBackgroundResource(R.drawable.circle_blue)
                    tvDay.setTextColor(ContextCompat.getColor(parent.context, R.color.white))
                }
                day.isToday -> {
                    view.setBackgroundResource(R.drawable.circle_light_blue)
                    tvDay.setTextColor(ContextCompat.getColor(parent.context, R.color.primary_blue))
                }
                else -> {
                    view.setBackgroundColor(
                        ContextCompat.getColor(parent.context, android.R.color.transparent)
                    )
                    tvDay.setTextColor(ContextCompat.getColor(parent.context, R.color.text_primary))
                }
            }

            vIndicator.visibility = if (day.hasEvents) View.VISIBLE else View.INVISIBLE
            vGlucoseDot.visibility = if (day.hasGlucose) View.VISIBLE else View.INVISIBLE

            if (day.hasEvents) vIndicator.setBackgroundResource(R.drawable.circle_light_blue)
            if (day.hasGlucose) vGlucoseDot.setBackgroundResource(R.drawable.circle_light_green)

            view.setOnClickListener { onClick(day) }
            return view
        }
    }
}