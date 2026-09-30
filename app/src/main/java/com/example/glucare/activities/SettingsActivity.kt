package com.example.glucare.activities

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.glucare.GlucareApplication
import com.example.glucare.R
import com.example.glucare.databinding.ActivitySettingsBinding
import com.example.glucare.services.ReminderManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var app: GlucareApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = application as GlucareApplication

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)

        loadSettings()
        setupDarkMode()
        setupClickListeners()
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)

        binding.switchDarkMode.isChecked = prefs.getBoolean("dark_mode", false)
        binding.switchNotifications.isChecked = prefs.getBoolean("notifications", true)

        binding.tvSelectedLanguage.text = when (prefs.getString("app_language", "en")) {
            "fr" -> "Français"
            "ar" -> "العربية"
            else -> "English"
        }

        binding.tvSelectedUnit.text = prefs.getString("glucose_units", "mg/dL") ?: "mg/dL"

        val userId = app.sessionManager.getUserId()
        val user = app.databaseHelper.getUserById(userId)

        binding.tvUserName.text = user?.fullName ?: "Not set"
        binding.tvUserEmail.text = user?.email ?: "Not set"

        updateExactAlarmStatus()
    }

    private fun updateExactAlarmStatus() {
        val reminderManager = ReminderManager(this)
        val canSchedule = reminderManager.canScheduleExact()

        binding.tvExactAlarmStatus.text = if (canSchedule) "✅ Enabled" else "⚠️ Disabled"
        binding.tvExactAlarmStatus.setTextColor(
            getColor(if (canSchedule) R.color.success_green else R.color.warning_orange)
        )
    }

    private fun setupDarkMode() {
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            getSharedPreferences("settings", MODE_PRIVATE)
                .edit()
                .putBoolean("dark_mode", isChecked)
                .apply()

            val targetMode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(targetMode)

            Toast.makeText(this, if (isChecked) "Dark mode enabled" else "Light mode enabled", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, WelcomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        // Edit Profile - opens personal info editing dialog
        binding.cardEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        binding.cardLanguage.setOnClickListener {
            startActivity(Intent(this, LanguageSettingsActivity::class.java))
        }

        binding.cardUnits.setOnClickListener { showUnitSelectionDialog() }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            getSharedPreferences("settings", MODE_PRIVATE)
                .edit()
                .putBoolean("notifications", isChecked)
                .apply()
        }

        binding.cardExactAlarms.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }

        binding.cardAbout.setOnClickListener { showAboutDialog() }

        binding.cardPrivacy.setOnClickListener {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
        }

        binding.cardTerms.setOnClickListener {
            startActivity(Intent(this, TermsOfServiceActivity::class.java))
        }

        binding.cardExportData.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }

        binding.cardDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    // Edit Profile - Personal Info (Name, Email, Phone)
    private fun showEditProfileDialog() {
        val userId = app.sessionManager.getUserId()
        val user = app.databaseHelper.getUserById(userId) ?: return

        val options = arrayOf("Edit Name", "Edit Email", "Edit Phone", "Change Password")

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Profile")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditNameDialog(user.fullName)
                    1 -> showEditEmailDialog(user.email)
                    2 -> showEditPhoneDialog(user.phone)
                    3 -> showChangePasswordDialog()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditNameDialog(currentName: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_field, null)
        val til = dialogView.findViewById<TextInputLayout>(R.id.tilField)
        val et = dialogView.findViewById<TextInputEditText>(R.id.etField)

        til.hint = "Full Name"
        et.setText(currentName)

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Name")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = et.text.toString().trim()
                if (newName.isNotEmpty()) {
                    updateUserField("full_name", newName)
                    binding.tvUserName.text = newName
                    app.sessionManager.saveUser(app.sessionManager.getUserId(), newName, app.sessionManager.getUserEmail() ?: "")
                    Toast.makeText(this, "Name updated", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditEmailDialog(currentEmail: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_field, null)
        val til = dialogView.findViewById<TextInputLayout>(R.id.tilField)
        val et = dialogView.findViewById<TextInputEditText>(R.id.etField)

        til.hint = "Email"
        et.setText(currentEmail)
        et.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Email")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newEmail = et.text.toString().trim()
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    updateUserField("email", newEmail)
                    binding.tvUserEmail.text = newEmail
                    Toast.makeText(this, "Email updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditPhoneDialog(currentPhone: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_field, null)
        val til = dialogView.findViewById<TextInputLayout>(R.id.tilField)
        val et = dialogView.findViewById<TextInputEditText>(R.id.etField)

        til.hint = "Phone Number"
        et.setText(currentPhone)
        et.inputType = InputType.TYPE_CLASS_PHONE

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Phone")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newPhone = et.text.toString().trim()
                if (newPhone.isNotEmpty()) {
                    updateUserField("phone", newPhone)
                    Toast.makeText(this, "Phone updated", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etCurrent = dialogView.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNew = dialogView.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirm = dialogView.findViewById<TextInputEditText>(R.id.etConfirmPassword)

        MaterialAlertDialogBuilder(this)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val current = etCurrent.text.toString()
                val new = etNew.text.toString()
                val confirm = etConfirm.text.toString()

                val userId = app.sessionManager.getUserId()
                val user = app.databaseHelper.getUserById(userId)

                if (user?.password != current) {
                    Toast.makeText(this, "Current password incorrect", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (new.length < 8) {
                    Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (new != confirm) {
                    Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                updateUserField("password", new)
                Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateUserField(field: String, value: String) {
        val userId = app.sessionManager.getUserId()
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val db = app.databaseHelper.writableDatabase
                val values = ContentValues().apply { put(field, value) }
                db.update("users", values, "id = ?", arrayOf(userId.toString()))
            }
        }
    }

    private fun showUnitSelectionDialog() {
        val units = arrayOf("mg/dL", "mmol/L")
        val current = binding.tvSelectedUnit.text.toString()
        val checkedItem = units.indexOf(current).coerceAtLeast(0)

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Glucose Unit")
            .setSingleChoiceItems(units, checkedItem) { dialog, which ->
                val selectedUnit = units[which]
                binding.tvSelectedUnit.text = selectedUnit
                getSharedPreferences("settings", MODE_PRIVATE)
                    .edit()
                    .putString("glucose_units", selectedUnit)
                    .apply()
                dialog.dismiss()
                Toast.makeText(this, "Unit changed to $selectedUnit", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("About Glucare")
            .setMessage("""
                Glucare - Diabetes Management App
                Version: 1.0.0
                
                Features:
                • Glucose Tracking & Trends
                • Smart Insulin Calculator
                • Expert Diabetes Assistant
                • Healthy Recipes Database
                • Calendar & Reminders
                • Doctor Directory
                • Reports & Analytics
                
                © 2026 Glucare Team
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showDeleteAccountDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_password, null)
        val etPassword = dialogView.findViewById<TextInputEditText>(R.id.etPassword)

        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Account")
            .setMessage("""
                This will permanently delete:
                • Your profile information
                • All glucose readings
                • All insulin dose history
                • Calendar events and reminders
                • Favorites and meal plans
                
                This action cannot be undone.
            """.trimIndent())
            .setView(dialogView)
            .setPositiveButton("Delete Account") { _, _ ->
                val password = etPassword.text.toString()
                val userId = app.sessionManager.getUserId()
                val user = app.databaseHelper.getUserById(userId)

                if (user?.password != password) {
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Delete all user data
                val db = app.databaseHelper.writableDatabase
                db.delete("users", "id = ?", arrayOf(userId.toString()))
                db.delete("glucose_readings", "user_id = ?", arrayOf(userId.toString()))
                db.delete("insulin_doses", "user_id = ?", arrayOf(userId.toString()))
                db.delete("calendar_events", "user_id = ?", arrayOf(userId.toString()))
                db.delete("favorite_recipes", "user_id = ?", arrayOf(userId.toString()))
                db.delete("meal_plan", "user_id = ?", arrayOf(userId.toString()))

                app.sessionManager.logout()
                Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, WelcomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadSettings()
        updateExactAlarmStatus()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}