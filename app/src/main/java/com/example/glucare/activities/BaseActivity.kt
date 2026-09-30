package com.example.glucare.activities

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Apply language ONLY - dark mode is handled in Application class
        super.attachBaseContext(updateBaseContextLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // NO dark mode code here - it's already set in Application
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        // NO dark mode code here - it's already set in Application
    }

    companion object {
        fun updateBaseContextLocale(context: Context): Context {
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val languageCode = prefs.getString("app_language", "en") ?: "en"

            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)

            return context.createConfigurationContext(config)
        }
    }
}