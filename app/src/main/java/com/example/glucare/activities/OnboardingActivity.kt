package com.example.glucare.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.glucare.R
import com.example.glucare.adapters.OnboardingAdapter
import com.example.glucare.databinding.ActivityOnboardingBinding
import com.google.android.material.button.MaterialButton

class OnboardingActivity : BaseActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var onboardingAdapter: OnboardingAdapter

    private val titles = listOf(
        "Track Your Glucose",
        "Smart Insulin Calculator",
        "Advisor Expert System",
        "Stay Organized with Personalized Reports"
    )

    private val descriptions = listOf(
        "Monitor your blood glucose levels easily and see trends over time",
        "Calculate your insulin doses based on carbs and current glucose levels",
        "Enter your questions and get answers from our expert system",
        "Keep track of appointments, medications, and meals with the calendar"
    )

    private val icons = listOf(
        R.drawable.ic_glucose,
        R.drawable.ic_insulin_premium,
        R.drawable.ai,
        R.drawable.ic_calendar_premium
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupDots()
        setupListeners()
    }

    private fun setupViewPager() {
        onboardingAdapter = OnboardingAdapter(titles, descriptions, icons)
        binding.viewPager2.adapter = onboardingAdapter

        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
                updateButton(position)
            }
        })
    }

    private fun setupDots() {
        val dots = arrayOfNulls<TextView>(titles.size)
        binding.dotsContainer.removeAllViews()

        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i]?.text = "•"
            dots[i]?.textSize = 35f
            dots[i]?.setTextColor(if (i == 0) getColor(R.color.primary_blue) else getColor(R.color.light_gray))
            binding.dotsContainer.addView(dots[i])
        }
    }

    private fun updateDots(position: Int) {
        for (i in 0 until binding.dotsContainer.childCount) {
            val dot = binding.dotsContainer.getChildAt(i) as TextView
            dot.setTextColor(if (i == position) getColor(R.color.primary_blue) else getColor(R.color.light_gray))
        }
    }

    private fun updateButton(position: Int) {
        if (position == titles.size - 1) {
            binding.btnNext.text = "Get Started"
        } else {
            binding.btnNext.text = "Next"
        }
    }

    private fun setupListeners() {
        binding.btnNext.setOnClickListener {
            if (binding.viewPager2.currentItem == titles.size - 1) {
                // Mark onboarding as completed
                getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("onboarding_completed", true)
                    .apply()

                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            } else {
                binding.viewPager2.currentItem += 1
            }
        }

        binding.btnSkip.setOnClickListener {
            // Mark onboarding as completed
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("onboarding_completed", true)
                .apply()

            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }
}