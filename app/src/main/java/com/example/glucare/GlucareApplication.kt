package com.example.glucare

import android.app.Application
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.example.glucare.database.DatabaseHelper
import com.example.glucare.utils.SessionManager
import com.example.glucare.services.SyncManager
import java.util.Locale

class GlucareApplication : Application() {

    lateinit var databaseHelper: DatabaseHelper
    lateinit var sessionManager: SessionManager
    lateinit var syncManager: SyncManager

    override fun onCreate() {
        // STEP 1: Apply dark mode FIRST - before anything else
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate()

        databaseHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)
        syncManager = SyncManager(this, databaseHelper, sessionManager)

        // Apply saved language to application context
        applySavedLanguage()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applySavedLanguage()
    }

    private fun applySavedLanguage() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val languageCode = prefs.getString("app_language", "en") ?: "en"

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}