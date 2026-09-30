package com.example.glucare.activities

import android.os.Bundle
import com.example.glucare.R
import com.example.glucare.adapters.AuthPagerAdapter

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.glucare.databinding.ActivityAuthBinding

class AuthActivity : BaseActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var authPagerAdapter: AuthPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupTabs()
    }

    private fun setupViewPager() {
        authPagerAdapter = AuthPagerAdapter(this)
        binding.viewPagerAuth.adapter = authPagerAdapter

        binding.viewPagerAuth.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateTabSelection(position)
            }
        })
    }

    private fun setupTabs() {
        binding.tvLoginTab.setOnClickListener {
            binding.viewPagerAuth.currentItem = 0
            updateTabSelection(0)
        }

        binding.tvSignupTab.setOnClickListener {
            binding.viewPagerAuth.currentItem = 1
            updateTabSelection(1)
        }

        // Select login tab by default
        updateTabSelection(0)
    }

    private fun updateTabSelection(position: Int) {
        if (position == 0) {
            binding.tvLoginTab.background = ContextCompat.getDrawable(this, R.drawable.bg_tab_indicator)
            binding.tvLoginTab.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.tvSignupTab.background = null
            binding.tvSignupTab.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        } else {
            binding.tvSignupTab.background = ContextCompat.getDrawable(this, R.drawable.bg_tab_indicator)
            binding.tvSignupTab.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.tvLoginTab.background = null
            binding.tvLoginTab.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }
    }

    fun switchToTab(position: Int) {
        binding.viewPagerAuth.currentItem = position
        updateTabSelection(position)
    }
}