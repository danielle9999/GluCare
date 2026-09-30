// activities/ReportsActivity.kt
package com.example.glucare.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.glucare.R
import com.example.glucare.adapters.ReportPagerAdapter
import com.example.glucare.databinding.ActivityReportsBinding
import com.google.android.material.tabs.TabLayoutMediator

class ReportsActivity : BaseActivity() {

    private lateinit var binding: ActivityReportsBinding
    private lateinit var reportPagerAdapter: ReportPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupToolbar()
    }

    private fun setupViewPager() {
        reportPagerAdapter = ReportPagerAdapter(this)
        binding.viewPager.adapter = reportPagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Glucose"
                1 -> "Insulin"
                else -> "Summary"
            }
        }.attach()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Reports & Analytics"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}