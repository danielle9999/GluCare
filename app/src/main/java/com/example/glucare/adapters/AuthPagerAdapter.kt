package com.example.glucare.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.glucare.fragments.SignupFragment
import com.example.glucare.fragments.LoginFragment   // Note: SignupFragment (with 'u') not SignUpFragment

class AuthPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            LoginFragment()
        } else {
            SignupFragment()  // Make sure this matches your fragment class name
        }
    }
}