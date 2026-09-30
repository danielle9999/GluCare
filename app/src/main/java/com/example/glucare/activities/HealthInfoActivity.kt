package com.example.glucare.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.glucare.GlucareApplication
import com.example.glucare.R
import com.example.glucare.databinding.FragmentHealthInfoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class HealthInfoActivity : BaseActivity() {

    private lateinit var binding: FragmentHealthInfoBinding
    private lateinit var app: GlucareApplication
    private var selectedGender: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentHealthInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as GlucareApplication

        setupUI()
        setupListeners()
        loadUserData()
    }

    private fun setupUI() {
        // Setup diabetes type dropdown
        val diabetesTypes = arrayOf("Type 1", "Type 2", "Gestational", "Pre-diabetes", "Other")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, diabetesTypes)
        binding.actvDiabetesType.setAdapter(adapter)
    }

    private fun setupListeners() {
        // Gender selection
        binding.rbMale.setOnClickListener {
            selectedGender = "Male"
        }
        binding.rbFemale.setOnClickListener {
            selectedGender = "Female"
        }
        binding.rbOther.setOnClickListener {
            selectedGender = "Other"
        }

        // BMI calculation on text change
        binding.etHeight.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                calculateBMI()
            }
        }
        binding.etWeight.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                calculateBMI()
            }
        }

        // Next button
        binding.btnNext.setOnClickListener {
            saveHealthInfo()
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            val userId = app.sessionManager.getUserId()
            if (userId != -1) {
                val user = withContext(Dispatchers.IO) {
                    app.databaseHelper.getUserById(userId)
                }

                user?.let {
                    // Pre-fill data if exists
                    if (it.age != null) {
                        binding.etAge.setText(it.age.toString())
                    }
                    if (it.gender != null) {
                        selectedGender = it.gender
                        when (it.gender) {
                            "Male" -> binding.rbMale.isChecked = true
                            "Female" -> binding.rbFemale.isChecked = true
                            "Other" -> binding.rbOther.isChecked = true
                        }
                    }
                    if (it.height != null) {
                        binding.etHeight.setText(it.height.toString())
                    }
                    if (it.weight != null) {
                        binding.etWeight.setText(it.weight.toString())
                    }
                    if (it.diabetesType != null) {
                        binding.actvDiabetesType.setText(it.diabetesType, false)
                    }
                    if (it.avgBloodSugar != null) {
                        binding.etBloodSugar.setText(it.avgBloodSugar.toString())
                    }

                    calculateBMI()
                }
            }
        }
    }

    private fun calculateBMI() {
        val heightStr = binding.etHeight.text.toString()
        val weightStr = binding.etWeight.text.toString()

        if (heightStr.isNotEmpty() && weightStr.isNotEmpty()) {
            try {
                val height = heightStr.toFloat() / 100 // convert cm to meters
                val weight = weightStr.toFloat()

                if (height > 0 && weight > 0) {
                    val bmi = weight / (height * height)
                    binding.tvBmiValue.text = String.format("%.1f", bmi)

                    // Determine BMI category
                    val category = when {
                        bmi < 18.5 -> "Underweight"
                        bmi < 25 -> "Normal"
                        bmi < 30 -> "Overweight"
                        else -> "Obese"
                    }
                    binding.tvBmiCategory.text = category

                    // Set color based on category
                    val color = when (category) {
                        "Normal" -> R.color.success_green
                        "Underweight" -> R.color.warning_orange
                        "Overweight" -> R.color.warning_orange
                        else -> R.color.error_red
                    }
                    binding.tvBmiCategory.setTextColor(getColor(color))
                }
            } catch (e: NumberFormatException) {
                binding.tvBmiValue.text = "--"
                binding.tvBmiCategory.text = "Invalid input"
            }
        } else {
            binding.tvBmiValue.text = "--"
            binding.tvBmiCategory.text = "Not calculated"
        }
    }

    private fun saveHealthInfo() {
        // Get values
        val ageStr = binding.etAge.text.toString()
        val heightStr = binding.etHeight.text.toString()
        val weightStr = binding.etWeight.text.toString()
        val diabetesType = binding.actvDiabetesType.text.toString()
        val bloodSugarStr = binding.etBloodSugar.text.toString()

        // Validate
        if (TextUtils.isEmpty(ageStr)) {
            binding.etAge.error = "Age required"
            binding.etAge.requestFocus()
            return
        }

        if (selectedGender == null) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show()
            return
        }

        if (TextUtils.isEmpty(heightStr)) {
            binding.etHeight.error = "Height required"
            binding.etHeight.requestFocus()
            return
        }

        if (TextUtils.isEmpty(weightStr)) {
            binding.etWeight.error = "Weight required"
            binding.etWeight.requestFocus()
            return
        }

        // Show loading
        binding.btnNext.isEnabled = false
        binding.btnNext.text = ""

        lifecycleScope.launch {
            try {
                val userId = app.sessionManager.getUserId()
                val age = ageStr.toIntOrNull()
                val height = heightStr.toFloatOrNull()
                val weight = weightStr.toFloatOrNull()
                val bloodSugar = if (bloodSugarStr.isNotEmpty()) bloodSugarStr.toFloatOrNull() else null

                val success = withContext(Dispatchers.IO) {
                    app.databaseHelper.updateUserHealthInfo(
                        userId = userId,
                        age = age,
                        gender = selectedGender,
                        height = height,
                        weight = weight,
                        diabetesType = diabetesType.ifEmpty { null },
                        avgBloodSugar = bloodSugar
                    )
                }

                if (success) {
                    withContext(Dispatchers.Main) {
                        app.sessionManager.setHealthInfoCompleted(true)
                        Toast.makeText(this@HealthInfoActivity, "Profile completed!", Toast.LENGTH_SHORT).show()

                        // Navigate to main activity
                        startActivity(Intent(this@HealthInfoActivity,HomeActivity::class.java))
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@HealthInfoActivity, "Failed to save", Toast.LENGTH_SHORT).show()
                        binding.btnNext.isEnabled = true
                        binding.btnNext.text = "COMPLETE PROFILE"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HealthInfoActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.btnNext.isEnabled = true
                    binding.btnNext.text = "COMPLETE PROFILE"
                }
            }
        }
    }
}