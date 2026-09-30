package com.example.glucare.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.example.glucare.GlucareApplication
import androidx.appcompat.app.AppCompatActivity
import com.example.glucare.databinding.ActivityWelcomeBinding

class WelcomeActivity : BaseActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user has already seen onboarding
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isOnboardingCompleted = prefs.getBoolean("onboarding_completed", false)

        if (isOnboardingCompleted) {
            goToNextScreen()
            return
        }

        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        startAnimations()
    }

    private fun setupClickListeners() {
        binding.btnGetStarted.setOnClickListener {
            // Mark onboarding as seen
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("onboarding_completed", true)
                .apply()

            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        }
    }

    private fun startAnimations() {
        // Simple fade in animation
        binding.logoContainer.alpha = 0f
        binding.appName.alpha = 0f
        binding.tagline.alpha = 0f
        binding.btnGetStarted.alpha = 0f
        binding.versionText.alpha = 0f

        binding.logoContainer.animate().alpha(1f).setDuration(800).start()
        binding.appName.animate().alpha(1f).setDuration(800).setStartDelay(200).start()
        binding.tagline.animate().alpha(1f).setDuration(800).setStartDelay(400).start()
        binding.btnGetStarted.animate().alpha(1f).setDuration(400).setStartDelay(600).start()
        binding.versionText.animate().alpha(0.5f).setDuration(800).setStartDelay(600).start()
    }

    private fun goToNextScreen() {
        val session = (application as GlucareApplication).sessionManager

        if (session.isLoggedIn()) {
            if (!session.isHealthInfoCompleted()) {
                startActivity(Intent(this, HealthInfoActivity::class.java))
            } else {
                startActivity(Intent(this, HomeActivity::class.java))
            }
        } else {
            startActivity(Intent(this, AuthActivity::class.java))
        }
        finish()
    }
}