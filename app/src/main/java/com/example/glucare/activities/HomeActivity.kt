package com.example.glucare.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.glucare.GlucareApplication
import com.example.glucare.R
import com.example.glucare.data.TipsDatabase
import com.example.glucare.database.DatabaseHelper
import com.example.glucare.models.GlucoseReading
import com.example.glucare.repositories.GlucoseRepository
import com.example.glucare.repositories.InsulinRepository
import com.example.glucare.services.MedicalInsulinCalculator
import com.example.glucare.services.ReminderManager
import com.example.glucare.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : BaseActivity() {

    private lateinit var app: GlucareApplication
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var medicalCalculator: MedicalInsulinCalculator
    private lateinit var reminderManager: ReminderManager

    private lateinit var tvGreeting: TextView
    private lateinit var tvUserName: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvGlucoseValue: TextView
    private lateinit var tvGlucoseStatus: TextView
    private lateinit var tvUpcomingCount: TextView
    private lateinit var remindersContainer: LinearLayout
    private lateinit var tvAvgGlucose: TextView
    private lateinit var tvTotalInsulin: TextView
    private lateinit var tvTrend: TextView
    private lateinit var tvInsight: TextView
    private lateinit var tipCard: FrameLayout

    private lateinit var btnLogGlucose: MaterialButton
    private lateinit var cardInsulin: CardView
    private lateinit var cardFood: CardView        // → Chatbot (Expert System)
    private lateinit var cardCalendar: CardView
    private lateinit var cardRecipes: CardView
    private lateinit var summaryCard: CardView     // → Doctors
    private lateinit var cardReports: CardView
    private lateinit var profileImage: ImageView

    // ─────────────────────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)

        app             = application as GlucareApplication
        dbHelper        = app.databaseHelper
        sessionManager  = app.sessionManager
        reminderManager = ReminderManager(this)
        medicalCalculator = MedicalInsulinCalculator(
            dbHelper    = dbHelper,
            glucoseRepo = GlucoseRepository(dbHelper),
            insulinRepo = InsulinRepository(dbHelper),
            userId      = sessionManager.getUserId()
        )

        initViews()
        loadUserData()
        setupClickListeners()
        loadDailyTip()
    }

    // onResume refreshes glucose, events and tip every time we return to home
    override fun onResume() {
        super.onResume()
        loadUserData()
        loadDailyTip()
        // Force refresh of upcoming events
        loadUpcomingEvents()
    }

    // ─────────────────────────────────────────────────────────────────────────
    private fun initViews() {
        tvGreeting       = findViewById(R.id.tvGreeting)
        tvUserName       = findViewById(R.id.tvUserName)
        tvDate           = findViewById(R.id.tvDate)
        tvGlucoseValue   = findViewById(R.id.tvGlucoseValue)
        tvGlucoseStatus  = findViewById(R.id.tvGlucoseStatus)
        tvUpcomingCount  = findViewById(R.id.tvUpcomingCount)
        remindersContainer = findViewById(R.id.remindersContainer)
        tvAvgGlucose     = findViewById(R.id.tvAvgGlucose)
        tvTotalInsulin   = findViewById(R.id.tvTotalInsulin)
        tvTrend          = findViewById(R.id.tvTrend)
        tvInsight        = findViewById(R.id.tvInsight)
        tipCard          = findViewById(R.id.tipCard)

        btnLogGlucose    = findViewById(R.id.btnLogGlucose)
        cardInsulin      = findViewById(R.id.cardInsulin)
        cardFood         = findViewById(R.id.cardFood)
        cardCalendar     = findViewById(R.id.cardCalendar)
        cardRecipes      = findViewById(R.id.cardRecipes)
        summaryCard      = findViewById(R.id.summaryCard)
        profileImage     = findViewById(R.id.profileImage)
        cardReports      = findViewById(R.id.cardReports)
    }

    // ─────────────────────────────────────────────────────────────────────────
    private fun setupClickListeners() {
        btnLogGlucose.setOnClickListener { showGlucoseLogDialog() }

        cardInsulin.setOnClickListener  { safeStart(InsulinCalculatorActivity::class.java) }

        // ── FIXED: cardFood now opens the Chatbot, NOT ScannerActivity ──────
        cardFood.setOnClickListener     { safeStart(ChatbotActivity::class.java) }

        cardCalendar.setOnClickListener { safeStart(CalendarActivity::class.java) }
        cardRecipes.setOnClickListener  { safeStart(RecipesActivity::class.java) }
        summaryCard.setOnClickListener  { safeStart(DoctorsActivity::class.java) }
        profileImage.setOnClickListener { safeStart(ProfileActivity::class.java) }
        cardReports.setOnClickListener  { safeStart(ReportsActivity::class.java) }
    }

    private fun <T> safeStart(cls: Class<T>) {
        try {
            startActivity(Intent(this, cls))
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("HomeActivity", "safeStart ${cls.simpleName}: ${e.message}", e)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    private fun loadUserData() {
        tvUserName.text = sessionManager.getUserName() ?: "User"
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        tvGreeting.text = when (hour) {
            in 0..11  -> "Good Morning,"
            in 12..16 -> "Good Afternoon,"
            else      -> "Good Evening,"
        }
        tvDate.text = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())

        loadGlucoseData()
        loadUpcomingEvents()     // ← called every time; refreshes home upcoming list
        loadWeeklyStats()
        loadInsight()
    }

    private fun loadGlucoseData() {
        val userId = sessionManager.getUserId()
        val latest = dbHelper.getLatestGlucoseReading(userId)

        if (latest != null) {
            tvGlucoseValue.text = latest.value.toString()
            val (status, color) = when {
                latest.value < 70   -> "● LOW"      to R.color.error_red
                latest.value <= 140 -> "● NORMAL"   to R.color.success_green
                latest.value <= 180 -> "● ELEVATED" to R.color.warning_orange
                else                -> "● HIGH"     to R.color.error_red
            }
            tvGlucoseStatus.text = status
            tvGlucoseStatus.setTextColor(ContextCompat.getColor(this, color))
        } else {
            tvGlucoseValue.text  = "--"
            tvGlucoseStatus.text = "● No reading yet"
        }

        val todayStart = todayMidnightMs()
        val count = dbHelper.getEventsForDay(userId, todayStart).size
        tvUpcomingCount.text = "$count event${if (count != 1) "s" else ""} today"
    }

    // ── FIXED: runs every onResume → home screen always reflects new events ──
    private fun loadUpcomingEvents() {
        lifecycleScope.launch {
            val userId     = sessionManager.getUserId()
            val now        = System.currentTimeMillis()
            val todayStart = todayMidnightMs()

            val todayEvents = withContext(Dispatchers.IO) {
                dbHelper.getEventsForDay(userId, todayStart)
            }

            val upcoming = todayEvents
                .filter { event ->
                    val et = event.date + (event.time?.let { parseTimeToMillis(it) } ?: 0L)
                    et > now
                }
                .sortedBy { it.date + (it.time?.let { t -> parseTimeToMillis(t) } ?: 0L) }
                .take(5)

            runOnUiThread {
                remindersContainer.removeAllViews()
                if (upcoming.isEmpty()) {
                    val v = layoutInflater.inflate(
                        R.layout.item_empty_reminder, remindersContainer, false)
                    remindersContainer.addView(v)
                } else {
                    upcoming.forEach { event ->
                        val v = layoutInflater.inflate(
                            R.layout.item_upcoming_reminder, remindersContainer, false)
                        v.findViewById<ImageView>(R.id.ivIcon)?.apply {
                            setImageResource(event.getIcon())
                            setColorFilter(ContextCompat.getColor(
                                this@HomeActivity, event.getColor()))
                        }
                        v.findViewById<TextView>(R.id.tvTitle)?.text = event.title
                        v.findViewById<TextView>(R.id.tvTime)?.text  =
                            if (!event.time.isNullOrEmpty()) event.time else "All day"
                        val et   = event.date + (event.time?.let { parseTimeToMillis(it) } ?: 0L)
                        val diff = ((et - now) / 60_000).toInt()
                        v.findViewById<TextView>(R.id.tvWhen)?.text = when {
                            diff < 60  -> "in $diff min"
                            diff < 120 -> "in 1 hour"
                            else       -> "in ${diff / 60} hours"
                        }
                        v.setOnClickListener {
                            startActivity(Intent(this@HomeActivity,
                                CalendarActivity::class.java).apply {
                                putExtra("selected_date", event.date)
                            })
                        }
                        remindersContainer.addView(v)
                    }
                }
            }
        }
    }

    private fun loadWeeklyStats() {
        val userId     = sessionManager.getUserId()
        val oneWeekAgo = System.currentTimeMillis() / 1000 - 7 * 86400

        val readings = dbHelper.getGlucoseReadings(userId)
            .filter { it.timestamp >= oneWeekAgo }

        if (readings.isNotEmpty()) {
            tvAvgGlucose.text = readings.map { it.value }.average().toInt().toString()
            val first = readings.minByOrNull { it.timestamp }?.value ?: 0
            val last  = readings.maxByOrNull { it.timestamp }?.value ?: 0
            tvTrend.text = when {
                last < first - 10 -> "📉 Improving"
                last > first + 10 -> "📈 Rising"
                else              -> "📊 Stable"
            }
        } else {
            tvAvgGlucose.text = "--"
            tvTrend.text      = "📊 No data"
        }

        val insulin = dbHelper.getInsulinHistory(userId)
            .filter { it.timestamp >= oneWeekAgo }
        tvTotalInsulin.text = insulin.sumOf { it.calculatedDose.toDouble() }.toInt().toString()
    }

    private fun loadInsight() {
        tvInsight.text = try {
            val insights = medicalCalculator.getInsights()
            if (insights.isNotEmpty()) "✨ ${insights.random()}"
            else "✨ Log more data to see personalized insights"
        } catch (e: Exception) { "✨ Welcome! Log your glucose to see insights" }
    }

    // ─────────────────────────────────────────────────────────────────────────
    private fun showGlucoseLogDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_log_glucose, null)
        val etGlucose  = dialogView.findViewById<TextInputEditText>(R.id.etGlucose)
        val rgMeal     = dialogView.findViewById<RadioGroup>(R.id.rgMealRelation)

        AlertDialog.Builder(this)
            .setTitle("Log Glucose Reading")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val value = etGlucose.text.toString().toIntOrNull()
                if (value == null) {
                    Toast.makeText(this, "Enter a valid glucose value", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val type = when (rgMeal.checkedRadioButtonId) {
                    R.id.rbFasting    -> GlucoseReading.ReadingType.FASTING
                    R.id.rbBeforeMeal -> GlucoseReading.ReadingType.BEFORE_MEAL
                    R.id.rbAfterMeal  -> GlucoseReading.ReadingType.AFTER_MEAL
                    else              -> GlucoseReading.ReadingType.RANDOM
                }
                val reading = GlucoseReading(
                    userId = sessionManager.getUserId(), value = value, readingType = type)
                dbHelper.insertGlucoseReading(reading)
                loadUserData()   // refreshes glucose display AND event list
                Toast.makeText(this, "Glucose logged!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadDailyTip() {
        try {
            tipCard.removeAllViews()
            val tipView   = layoutInflater.inflate(R.layout.item_tip_card, tipCard, true)
            val randomTip = TipsDatabase.getRandomTip(this)
            tipView.findViewById<TextView>(R.id.tvTipIcon)?.text     = randomTip.icon
            tipView.findViewById<TextView>(R.id.tvTipTitle)?.text    = randomTip.title
            tipView.findViewById<TextView>(R.id.tvTipContent)?.text  = randomTip.content
            tipView.findViewById<TextView>(R.id.tvTipCategory)?.text = randomTip.category
            tipView.findViewById<TextView>(R.id.btnNewTip)?.setOnClickListener { loadDailyTip() }
        } catch (e: Exception) {
            Log.e("HomeActivity", "loadDailyTip error: ${e.message}")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    private fun todayMidnightMs(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun parseTimeToMillis(time: String): Long = try {
        val p = time.split(":")
        ((p[0].toInt() * 3600 + p[1].toInt() * 60) * 1000).toLong()
    } catch (e: Exception) { 0L }
}