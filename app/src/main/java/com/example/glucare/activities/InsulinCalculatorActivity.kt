package com.example.glucare.activities
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import kotlin.math.round
import android.widget.*

import androidx.lifecycle.lifecycleScope
import com.example.glucare.GlucareApplication
import com.example.glucare.R
import com.example.glucare.databinding.ActivityInsulinCalculatorBinding
import com.example.glucare.models.InsulinDose
import com.example.glucare.repositories.GlucoseRepository
import com.example.glucare.repositories.InsulinRepository
import com.example.glucare.services.MedicalInsulinCalculator
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class InsulinCalculatorActivity : BaseActivity() {

    private lateinit var binding: ActivityInsulinCalculatorBinding
    private lateinit var app: GlucareApplication
    private lateinit var medicalCalculator: MedicalInsulinCalculator

    private var currentRatio            = 10
    private var currentCorrectionFactor = 50
    private var currentTargetGlucose    = 120
    private var lastCalculatedDose      = 0f
    private var lastCarbDose            = 0f
    private var lastCorrectionDose      = 0f
    private var currentUserId           = -1
    private var userWeight              = 70f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInsulinCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app           = application as GlucareApplication
        currentUserId = app.sessionManager.getUserId()

        setupMedicalCalculator()
        initDefaultDisplay()
        loadUserData()
        setupListeners()
        loadEducationalTip()
    }

    private fun setupMedicalCalculator() {
        medicalCalculator = MedicalInsulinCalculator(
            dbHelper    = app.databaseHelper,
            glucoseRepo = GlucoseRepository(app.databaseHelper),
            insulinRepo = InsulinRepository(app.databaseHelper),
            userId      = currentUserId
        )
    }

    private fun initDefaultDisplay() {
        binding.tvTargetGlucose.text = currentTargetGlucose.toString()
        updateRatioDisplay()
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) {
                app.databaseHelper.getUserById(currentUserId)
            }
            user?.let {
                userWeight = it.weight ?: 70f
                val prescription = medicalCalculator.calculatePrescription(userWeight)
                runOnUiThread {
                    binding.tvUserRatio.text      = "1u / ${prescription.carbohydrateRatio}g"
                    binding.tvUserCorrection.text = "1u / ${prescription.correctionFactor} mg/dL"
                    binding.tvUserTarget.text     = "${prescription.targetGlucose} mg/dL"
                    binding.tvUserBasal.text      = String.format("%.1f units", prescription.basalDose)

                    currentRatio            = prescription.carbohydrateRatio
                    currentCorrectionFactor = prescription.correctionFactor
                    currentTargetGlucose    = prescription.targetGlucose

                    binding.seekBarRatio.progress = (currentRatio - 5).coerceIn(0, 15)
                    updateRatioDisplay()
                }
            }
        }
    }

    private fun setupListeners() {
        // Carb quick-pick buttons
        binding.btnMinus.setOnClickListener {
            val cur = binding.etCarbs.text.toString().toIntOrNull() ?: 0
            if (cur >= 5) binding.etCarbs.setText((cur - 5).toString())
            calculateRecommendation()
        }
        binding.btnPlus.setOnClickListener {
            val cur = binding.etCarbs.text.toString().toIntOrNull() ?: 0
            binding.etCarbs.setText((cur + 5).toString())
            calculateRecommendation()
        }
        binding.btnCarb15.setOnClickListener { binding.etCarbs.setText("15"); calculateRecommendation() }
        binding.btnCarb30.setOnClickListener { binding.etCarbs.setText("30"); calculateRecommendation() }
        binding.btnCarb45.setOnClickListener { binding.etCarbs.setText("45"); calculateRecommendation() }
        binding.btnCarb60.setOnClickListener { binding.etCarbs.setText("60"); calculateRecommendation() }
        binding.btnCarb75.setOnClickListener { binding.etCarbs.setText("75"); calculateRecommendation() }

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateGlucoseStatus()
                calculateRecommendation()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        binding.etCurrentGlucose.addTextChangedListener(watcher)
        binding.etCarbs.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = calculateRecommendation()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Ratio seekbar
        binding.seekBarRatio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentRatio = progress + 5
                updateRatioDisplay()
                calculateRecommendation()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnRefresh.setOnClickListener  { loadLatestGlucose() }
        binding.btnCalculate.setOnClickListener {
            calculateRecommendation()
            binding.cardSmartRecommendation.visibility = View.VISIBLE
        }
        binding.btnUseSmartDose.setOnClickListener {
            val dose = binding.tvSmartDose.text.toString().toFloatOrNull() ?: return@setOnClickListener
            showUseDoseDialog(dose)
        }
        binding.btnMoreOptions.setOnClickListener  { showAlternativeDosesDialog() }
        binding.btnSave.setOnClickListener         { saveDoseToHistory() }
        binding.btnHistory.setOnClickListener      { showHistoryDialog() }
        binding.btnInsights.setOnClickListener     { showInsightsDialog() }
        binding.btnNextTip.setOnClickListener      { loadEducationalTip() }
        binding.btnEditSettings.setOnClickListener { showEditSettingsDialog() }
        binding.rgMealType.setOnCheckedChangeListener { _, _ -> calculateRecommendation() }

        // Alternative dose click listeners
        binding.tvAlternativeLow.setOnClickListener {
            binding.tvAlternativeLow.text.toString().toFloatOrNull()?.let { dose ->
                binding.tvSmartDose.text = String.format("%.1f", dose)
                lastCalculatedDose = dose
            }
        }
        binding.tvAlternativeMedium.setOnClickListener {
            binding.tvAlternativeMedium.text.toString().toFloatOrNull()?.let { dose ->
                binding.tvSmartDose.text = String.format("%.1f", dose)
                lastCalculatedDose = dose
            }
        }
        binding.tvAlternativeHigh.setOnClickListener {
            binding.tvAlternativeHigh.text.toString().toFloatOrNull()?.let { dose ->
                binding.tvSmartDose.text = String.format("%.1f", dose)
                lastCalculatedDose = dose
            }
        }
    }

    private fun updateGlucoseStatus() {
        val glucose = binding.etCurrentGlucose.text.toString().toIntOrNull() ?: return
        val (statusText, colorRes, bgRes) = when {
            glucose < 54   -> Triple("● CRITICAL LOW", R.color.error_red, R.drawable.bg_status_red)
            glucose < 70   -> Triple("● LOW",          R.color.error_red, R.drawable.bg_status_red)
            glucose <= 140 -> Triple("● NORMAL",       R.color.success_green, R.drawable.bg_status_green)
            glucose <= 180 -> Triple("● ELEVATED",     R.color.warning_orange, R.drawable.bg_status_orange)
            glucose <= 250 -> Triple("● HIGH",         R.color.error_red, R.drawable.bg_status_red)
            else           -> Triple("● VERY HIGH",    R.color.error_red, R.drawable.bg_status_red)
        }
        binding.tvGlucoseStatus.text = statusText
        binding.tvGlucoseStatus.setTextColor(getColor(colorRes))
        binding.tvGlucoseStatus.setBackgroundResource(bgRes)
    }

    private fun updateRatioDisplay() {
        binding.tvDisplayRatio.text = "1:$currentRatio"
        binding.tvRatioValue.text   = "1u / ${currentRatio}g"
    }

    private fun calculateRecommendation() {
        val glucose = binding.etCurrentGlucose.text.toString().toIntOrNull() ?: return
        val carbs   = binding.etCarbs.text.toString().toIntOrNull() ?: return

        val mealType = when (binding.rgMealType.checkedRadioButtonId) {
            R.id.rbBreakfast -> "Breakfast"
            R.id.rbLunch     -> "Lunch"
            R.id.rbDinner    -> "Dinner"
            R.id.rbSnack     -> "Snack"
            else             -> null
        }

        // Calculate carb dose
        lastCarbDose = carbs.toFloat() / currentRatio

        // Calculate correction dose (only if glucose is above target)
        val glucoseDiff = glucose - currentTargetGlucose
        lastCorrectionDose = if (glucoseDiff > 0) {
            glucoseDiff.toFloat() / currentCorrectionFactor
        } else {
            0f  // Never subtract insulin for low glucose - treat with carbs instead
        }

        // Total dose = carb dose + correction dose
        val raw = lastCarbDose + lastCorrectionDose

        // Round to nearest 0.5 units (standard for insulin dosing)
        lastCalculatedDose = roundToHalfUnit(raw)

        // Update UI
        binding.tvSmartDose.text = String.format("%.1f", lastCalculatedDose)
        binding.tvCarbDose.text = String.format("%.1f", lastCarbDose)
        binding.tvCorrectionDose.text = String.format("%.1f", lastCorrectionDose)
        binding.tvDisplayCorrection.text = "1:$currentCorrectionFactor"
        binding.tvTargetGlucose.text = currentTargetGlucose.toString()

        // Update alternative doses
        val conservative = roundToHalfUnit(lastCalculatedDose * 0.8f)
        val aggressive = roundToHalfUnit(lastCalculatedDose * 1.2f)

        binding.tvAlternativeLow.text = String.format("%.1f", conservative)
        binding.tvAlternativeMedium.text = String.format("%.1f", lastCalculatedDose)
        binding.tvAlternativeHigh.text = String.format("%.1f", aggressive)

        // Get AI recommendation message
        val recommendation = medicalCalculator.calculateMealtimeDose(
            currentGlucose        = glucose,
            carbs                 = carbs,
            mealType              = mealType,
            userWeight            = userWeight,
            customRatio           = currentRatio,
            customCorrectionFactor = currentCorrectionFactor,
            targetGlucose         = currentTargetGlucose
        )

        binding.layoutConfidence.visibility = View.VISIBLE
        binding.tvSmartMessage.text = recommendation.message
        binding.tvConfidenceLevel.text = when (recommendation.confidenceLevel) {
            MedicalInsulinCalculator.ConfidenceLevel.HIGH       -> "🔵 High Confidence"
            MedicalInsulinCalculator.ConfidenceLevel.MEDIUM     -> "🟢 Medium Confidence"
            MedicalInsulinCalculator.ConfidenceLevel.LOW        -> "🟡 Low Confidence"
            MedicalInsulinCalculator.ConfidenceLevel.FIRST_TIME -> "⚪ First Time"
        }

        // Add safety warning for critical values
        when {
            glucose < 54 -> {
                binding.tvSmartMessage.text = "🚨 CRITICAL LOW! Seek emergency help or use glucagon!"
            }
            glucose < 70 -> {
                binding.tvSmartMessage.text = "⚠️ LOW GLUCOSE! Treat with 15g fast carbs first, do NOT take insulin!"
            }
            glucose > 250 -> {
                binding.tvSmartMessage.text = "⚠️ HIGH GLUCOSE! Check ketones and stay hydrated."
            }
        }
    }

    /**
     * Round to nearest 0.5 units (standard insulin pen/pump precision)
     */
    private fun roundToHalfUnit(value: Float): Float {
        return kotlin.math.round(value * 2) / 2f
    }

    private fun loadLatestGlucose() {
        lifecycleScope.launch {
            val reading = withContext(Dispatchers.IO) {
                app.databaseHelper.getLatestGlucoseReading(currentUserId)
            }
            runOnUiThread {
                if (reading != null) {
                    binding.etCurrentGlucose.setText(reading.value.toString())
                    updateGlucoseStatus()
                } else {
                    Toast.makeText(this@InsulinCalculatorActivity,
                        "No recent glucose readings", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveDoseToHistory() {
        if (lastCalculatedDose <= 0f) {
            Toast.makeText(this, "Calculate a dose first", Toast.LENGTH_SHORT).show()
            return
        }

        val glucose = binding.etCurrentGlucose.text.toString().toIntOrNull()

        // Don't save if glucose is low (safety check)
        if (glucose != null && glucose < 70) {
            Toast.makeText(this, "⚠️ Cannot save - glucose is too low for insulin", Toast.LENGTH_LONG).show()
            return
        }

        val carbs = binding.etCarbs.text.toString().toIntOrNull() ?: 0
        val dose = InsulinDose(
            userId         = currentUserId,
            glucoseValue   = glucose,
            carbs          = carbs,
            insulinRatio   = currentRatio,
            calculatedDose = lastCalculatedDose,
            actualDose     = lastCalculatedDose,
            mealType       = getSelectedMealType()
        )
        lifecycleScope.launch {
            val id = withContext(Dispatchers.IO) { app.databaseHelper.insertInsulinDose(dose) }
            runOnUiThread {
                if (id > 0) {
                    Toast.makeText(this@InsulinCalculatorActivity,
                        "✅ Dose saved! Check glucose in 2 hours", Toast.LENGTH_SHORT).show()
                    showFeedbackOption(id.toInt())
                }
            }
        }
    }

    private fun getSelectedMealType(): String? = when (binding.rgMealType.checkedRadioButtonId) {
        R.id.rbBreakfast -> "Breakfast"
        R.id.rbLunch     -> "Lunch"
        R.id.rbDinner    -> "Dinner"
        R.id.rbSnack     -> "Snack"
        else             -> null
    }

    private fun showFeedbackOption(doseId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Track Effectiveness")
            .setMessage("Record your glucose in 2 hours to see how well this dose worked?")
            .setPositiveButton("Yes") { _, _ -> showGlucoseFeedbackDialog(doseId) }
            .setNegativeButton("Not now", null)
            .show()
    }

    private fun showGlucoseFeedbackDialog(doseId: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_glucose_feedback, null)
        val etFb = dialogView.findViewById<TextInputEditText>(R.id.etFeedbackGlucose)
        AlertDialog.Builder(this)
            .setTitle("2-Hour Glucose Check")
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val glucose = etFb.text.toString().toIntOrNull()
                if (glucose == null) {
                    Toast.makeText(this, "Enter a valid glucose value", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) { medicalCalculator.recordDoseFeedback(doseId, glucose) }
                    runOnUiThread {
                        Toast.makeText(this@InsulinCalculatorActivity,
                            "Feedback recorded! This helps improve future recommendations.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showHistoryDialog() {
        lifecycleScope.launch {
            val history = withContext(Dispatchers.IO) {
                app.databaseHelper.getInsulinHistory(currentUserId)
            }
            runOnUiThread {
                if (history.isEmpty()) {
                    AlertDialog.Builder(this@InsulinCalculatorActivity)
                        .setTitle("Insulin History")
                        .setMessage("No history yet. Save some doses to see patterns!")
                        .setPositiveButton("OK", null)
                        .show()
                    return@runOnUiThread
                }
                val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                val items = history.take(20).map { dose ->
                    val date = sdf.format(Date(dose.timestamp * 1000))
                    val icon = dose.effectiveness?.let {
                        when { it >= 0.8f -> "✅"; it >= 0.5f -> "🟡"; else -> "❌" }
                    } ?: "⏳"
                    "$icon $date: ${dose.calculatedDose}u (G:${dose.glucoseValue}, C:${dose.carbs}g)"
                }.toTypedArray()
                AlertDialog.Builder(this@InsulinCalculatorActivity)
                    .setTitle("📋 Insulin History")
                    .setItems(items) { _, _ -> }
                    .setPositiveButton("Close", null)
                    .show()
            }
        }
    }

    private fun showInsightsDialog() {
        val insights = medicalCalculator.getInsights()
        AlertDialog.Builder(this)
            .setTitle("📊 Your Insulin Insights")
            .setMessage(if (insights.isEmpty())
                "Log more doses to see insights about your insulin patterns!"
            else insights.joinToString("\n\n"))
            .setPositiveButton("OK", null)
            .show()
    }

    private fun loadEducationalTip() {
        binding.tvEducationalTip.text = medicalCalculator.getEducationalTip()
    }

    private fun showEditSettingsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_insulin_settings, null)
        val etRatio      = dialogView.findViewById<EditText>(R.id.etInsulinRatio)
        val etCorrection = dialogView.findViewById<EditText>(R.id.etCorrectionFactor)
        val etTarget     = dialogView.findViewById<EditText>(R.id.etTargetGlucose)
        val etWeight     = dialogView.findViewById<EditText>(R.id.etWeight)
        etRatio.setText(currentRatio.toString())
        etCorrection.setText(currentCorrectionFactor.toString())
        etTarget.setText(currentTargetGlucose.toString())
        etWeight.setText(userWeight.toString())
        AlertDialog.Builder(this)
            .setTitle("⚙️ Insulin Settings")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                currentRatio            = etRatio.text.toString().toIntOrNull()      ?: currentRatio
                currentCorrectionFactor = etCorrection.text.toString().toIntOrNull() ?: currentCorrectionFactor
                currentTargetGlucose    = etTarget.text.toString().toIntOrNull()     ?: currentTargetGlucose
                userWeight              = etWeight.text.toString().toFloatOrNull()   ?: userWeight
                binding.seekBarRatio.progress = (currentRatio - 5).coerceIn(0, 15)
                binding.tvDisplayCorrection.text = "1:$currentCorrectionFactor"
                binding.tvTargetGlucose.text = currentTargetGlucose.toString()
                binding.tvUserTarget.text = "$currentTargetGlucose mg/dL"
                updateRatioDisplay()
                calculateRecommendation()
                Toast.makeText(this, "Settings updated", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showUseDoseDialog(dose: Float) {
        AlertDialog.Builder(this)
            .setTitle("Use This Dose?")
            .setMessage("Take $dose units of insulin?")
            .setPositiveButton("Yes") { _, _ ->
                Toast.makeText(this, "✅ Dose recorded: $dose units", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showAlternativeDosesDialog() {
        val conservative = roundToHalfUnit(lastCalculatedDose * 0.8f)
        val aggressive = roundToHalfUnit(lastCalculatedDose * 1.2f)

        val items = arrayOf(
            "🟢 Conservative (-20%): ${String.format("%.1f", conservative)} units",
            "🔵 Recommended: ${String.format("%.1f", lastCalculatedDose)} units",
            "🟡 Aggressive (+20%): ${String.format("%.1f", aggressive)} units"
        )
        AlertDialog.Builder(this)
            .setTitle("Alternative Doses")
            .setItems(items) { _, which ->
                val selectedDose = when (which) {
                    0 -> conservative
                    1 -> lastCalculatedDose
                    else -> aggressive
                }
                binding.tvSmartDose.text = String.format("%.1f", selectedDose)
                lastCalculatedDose = selectedDose
            }
            .setPositiveButton("Close", null)
            .show()
    }
}