package com.example.glucare.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import com.example.glucare.R
import com.example.glucare.databinding.ActivityLanguageSettingsBinding
import java.util.Locale

class LanguageSettingsActivity : BaseActivity() {

    private lateinit var binding: ActivityLanguageSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.language)

        setupLanguageOptions()
    }

    private fun setupLanguageOptions() {
        val currentLang = getSharedPreferences("settings", MODE_PRIVATE)
            .getString("app_language", "en")

        when (currentLang) {
            "en" -> binding.rbEnglish.isChecked = true
            "fr" -> binding.rbFrench.isChecked = true
            "ar" -> binding.rbArabic.isChecked = true
        }

        binding.btnSaveLanguage.setOnClickListener {
            val languageCode = when (binding.rgLanguage.checkedRadioButtonId) {
                R.id.rbEnglish -> "en"
                R.id.rbFrench -> "fr"
                R.id.rbArabic -> "ar"
                else -> "en"
            }
            saveLanguage(languageCode)
        }
    }

    private fun saveLanguage(languageCode: String) {
        // Save language preference
        getSharedPreferences("settings", MODE_PRIVATE)
            .edit()
            .putString("app_language", languageCode)
            .apply()

        // Set locale immediately
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)

        // Update resources with new configuration
        resources.updateConfiguration(config, resources.displayMetrics)

        Toast.makeText(this,
            if (languageCode == "ar") "تم تغيير اللغة"
            else if (languageCode == "fr") "Langue changée"
            else "Language changed",
            Toast.LENGTH_SHORT).show()

        // Restart app completely
        val intent = Intent(this, WelcomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}