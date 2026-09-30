package com.example.glucare.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.glucare.GlucareApplication
import com.example.glucare.R
import com.example.glucare.databinding.ActivityProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var app: GlucareApplication

    // Photo picker launchers
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                saveProfilePhoto(uri)
            }
        }
    }

    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                saveProfilePhoto(uri)
            }
        }
    }

    private var currentPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = application as GlucareApplication
        currentPhotoUri = savedInstanceState?.getParcelable("photo_uri")

        setupClickListeners()
        loadUserData()
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("photo_uri", currentPhotoUri)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, HealthInfoActivity::class.java))
        }
        binding.btnLogout.setOnClickListener { showLogoutConfirmation() }

        // Profile photo click
        binding.profileImage.setOnClickListener {
            showPhotoOptionsDialog()
        }

        binding.cardGlucoseHistory.setOnClickListener {
            startActivity(Intent(this, GlucoseHistoryActivity::class.java))
        }

        binding.cardHealthStatus.setOnClickListener {
            showHealthStatusDialog()
        }

        binding.cardAverageStatus.setOnClickListener {
            showAverageStatusDialog()
        }

        binding.cardAverageGlucose.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }

        binding.layoutEmail.setOnClickListener {
            copyToClipboard(binding.tvEmail.text.toString())
        }
        binding.layoutPhone.setOnClickListener {
            dialPhoneNumber(binding.tvPhone.text.toString())
        }
    }

    private fun loadUserData() {
        val userId = app.sessionManager.getUserId()
        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) { app.databaseHelper.getUserById(userId) }
            val readings = withContext(Dispatchers.IO) { app.databaseHelper.getGlucoseReadings(userId) }
            val avgGlucose = withContext(Dispatchers.IO) { app.databaseHelper.getAverageGlucose(userId) }

            user?.let {
                binding.tvUserName.text = it.fullName
                binding.tvEmail.text = it.email
                binding.tvPhone.text = it.phone.ifEmpty { "Not set" }

                // Load profile photo
                loadProfilePhoto()

                if (avgGlucose > 0) {
                    binding.tvAverageGlucoseValue.text =
                        String.format("%.0f mg/dL", avgGlucose)
                } else {
                    binding.tvAverageGlucoseValue.text = "No data"
                }

                if (it.height != null && it.weight != null) {
                    val h = it.height / 100f
                    val bmi = it.weight / (h * h)
                    val cat = when {
                        bmi < 18.5f -> "Underweight"
                        bmi < 25f -> "Normal"
                        bmi < 30f -> "Overweight"
                        else -> "Obese"
                    }
                    binding.tvHealthStatusSubtitle.text = "BMI: ${"%.1f".format(bmi)} · $cat"
                } else {
                    binding.tvHealthStatusSubtitle.text = "Complete profile"
                }

                val sevenDaysAgo = System.currentTimeMillis() / 1000 - 7 * 86400
                val recent = readings.count { r -> r.timestamp >= sevenDaysAgo }
                binding.tvAverageStatusSubtitle.text = "$recent readings (7d)"
            }
        }
    }

    private fun showPhotoOptionsDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Remove Photo")
        MaterialAlertDialogBuilder(this)
            .setTitle("Profile Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> pickFromGallery()
                    2 -> removeProfilePhoto()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun takePhoto() {
        val dir = externalCacheDir ?: cacheDir
        val photoFile = File(dir, "temp_photo_${System.currentTimeMillis()}.jpg")
        currentPhotoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        takePhotoLauncher.launch(currentPhotoUri!!)
    }
    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun saveProfilePhoto(uri: Uri) {
        lifecycleScope.launch {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val userId = app.sessionManager.getUserId()
                val photoFile = File(filesDir, "profile_photo_$userId.jpg")

                inputStream?.use { input ->
                    FileOutputStream(photoFile).use { output ->
                        input.copyTo(output)
                    }
                }

                withContext(Dispatchers.Main) {
                    loadProfilePhoto()
                    Toast.makeText(this@ProfileActivity, "Photo updated", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Failed to save photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadProfilePhoto() {
        val userId = app.sessionManager.getUserId()
        val photoFile = File(filesDir, "profile_photo_$userId.jpg")
        if (photoFile.exists()) {
            binding.profileImage.setImageURI(Uri.fromFile(photoFile))
        } else {
            binding.profileImage.setImageResource(R.drawable.ic_profile)
        }
    }

    private fun removeProfilePhoto() {
        val userId = app.sessionManager.getUserId()
        val photoFile = File(filesDir, "profile_photo_$userId.jpg")
        if (photoFile.exists()) {
            photoFile.delete()
        }
        loadProfilePhoto()
        Toast.makeText(this, "Photo removed", Toast.LENGTH_SHORT).show()
    }

    private fun showHealthStatusDialog() {
        val userId = app.sessionManager.getUserId()
        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) { app.databaseHelper.getUserById(userId) }
            val avgGlucose = withContext(Dispatchers.IO) { app.databaseHelper.getAverageGlucose(userId) }
            val latestGlucose = withContext(Dispatchers.IO) { app.databaseHelper.getLatestGlucoseReading(userId) }

            val bmiSection = user?.let {
                if (it.height != null && it.weight != null) {
                    val h = it.height / 100f
                    val bmi = it.weight / (h * h)
                    val cat = when {
                        bmi < 18.5f -> "Underweight ⚠️"
                        bmi < 25f -> "Normal ✅"
                        bmi < 30f -> "Overweight ⚠️"
                        else -> "Obese 🔴"
                    }
                    """
Height: ${it.height} cm
Weight: ${it.weight} kg
BMI: ${"%.1f".format(bmi)} → $cat
                    """.trimIndent()
                } else "Height/weight not set — tap Edit Profile to add"
            } ?: "No profile data"

            val diabetesSection = user?.let {
                """
Diabetes Type: ${it.diabetesType ?: "Not specified"}
Gender: ${it.gender ?: "Not specified"}
Age: ${it.age?.toString() ?: "Not specified"}
                """.trimIndent()
            } ?: ""

            val glucoseSection = if (avgGlucose > 0) {
                val level = when {
                    avgGlucose < 70 -> "Low 🔴"
                    avgGlucose < 140 -> "Normal ✅"
                    avgGlucose < 180 -> "Elevated ⚠️"
                    else -> "High 🔴"
                }
                "Avg Glucose: ${"%.0f".format(avgGlucose)} mg/dL → $level"
            } else "No glucose readings yet"

            val latestSection = latestGlucose?.let {
                val status = when {
                    it.value < 70 -> "LOW 🔴"
                    it.value <= 140 -> "NORMAL ✅"
                    it.value <= 180 -> "ELEVATED ⚠️"
                    else -> "HIGH 🔴"
                }
                "Latest Reading: ${it.value} mg/dL → $status"
            } ?: "No recent reading"

            runOnUiThread {
                AlertDialog.Builder(this@ProfileActivity)
                    .setTitle("🏥 Your Health Status")
                    .setMessage("$bmiSection\n\n$diabetesSection\n\n$glucoseSection\n\n$latestSection")
                    .setPositiveButton("OK", null)
                    .setNeutralButton("Edit Profile") { _, _ ->
                        startActivity(Intent(this@ProfileActivity, HealthInfoActivity::class.java))
                    }
                    .show()
            }
        }
    }

    private fun showAverageStatusDialog() {
        val userId = app.sessionManager.getUserId()
        lifecycleScope.launch {
            val all = withContext(Dispatchers.IO) { app.databaseHelper.getGlucoseReadings(userId) }
            val now = System.currentTimeMillis() / 1000

            fun stats(readings: List<com.example.glucare.models.GlucoseReading>): String {
                if (readings.isEmpty()) return "No data"
                val avg = readings.map { it.value }.average()
                val low = readings.count { it.value < 70 }
                val norm = readings.count { it.value in 70..140 }
                val elev = readings.count { it.value in 141..180 }
                val high = readings.count { it.value > 180 }
                val n = readings.size.toFloat()
                return """
Avg: ${"%.0f".format(avg)} mg/dL (${readings.size} readings)
🟢 Normal (70–140):   ${"%.0f".format(norm / n * 100)}%
🟡 Elevated (141–180): ${"%.0f".format(elev / n * 100)}%
🔴 High (>180):        ${"%.0f".format(high / n * 100)}%
⬇️ Low (<70):          ${"%.0f".format(low / n * 100)}%
                """.trimIndent()
            }

            val d7 = all.filter { it.timestamp >= now - 7 * 86400 }
            val d30 = all.filter { it.timestamp >= now - 30 * 86400 }

            val message = "📅 LAST 7 DAYS\n${stats(d7)}\n\n📅 LAST 30 DAYS\n${stats(d30)}"

            runOnUiThread {
                AlertDialog.Builder(this@ProfileActivity)
                    .setTitle("📊 Glucose Average Status")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .setNeutralButton("Full Reports") { _, _ ->
                        startActivity(Intent(this@ProfileActivity, ReportsActivity::class.java))
                    }
                    .show()
            }
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { _, _ ->
                app.sessionManager.logout()
                startActivity(Intent(this, WelcomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun copyToClipboard(text: String) {
        (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            .setPrimaryClip(ClipData.newPlainText("text", text))
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun dialPhoneNumber(phoneNumber: String) {
        if (phoneNumber.isNotEmpty() && phoneNumber != "Not set") {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")))
        } else {
            Toast.makeText(this, "No phone number set", Toast.LENGTH_SHORT).show()
        }
    }
}