package com.example.glucare.activities

import android.os.Bundle
import android.widget.TextView
import com.example.glucare.R

class TestActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a simple TextView programmatically
        val textView = TextView(this)
        textView.text = "TEST ACTIVITY - If you see this, activity works!"
        textView.textSize = 20f
        textView.setPadding(50, 50, 50, 50)
        setContentView(textView)
    }
}