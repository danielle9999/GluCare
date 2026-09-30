package com.example.glucare.activities

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.ImageView
import android.widget.TextView
import com.example.glucare.R

class PrivacyPolicyActivity : BaseActivity() {

    private lateinit var tvPrivacyContent: TextView
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        tvPrivacyContent = findViewById(R.id.tvPrivacyContent)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        tvPrivacyContent.movementMethod = ScrollingMovementMethod()
        tvPrivacyContent.text = getPrivacyText()
    }

    private fun getPrivacyText(): String {
        return """
            GLUCARE - PRIVACY POLICY
            ================================
            
            Last Updated: April 1, 2026
            
            1. INTRODUCTION
            -------------------------------
            Glucare ("we," "our," or "us") is committed to protecting your privacy. This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you use our mobile application ("App").
            
            Please read this privacy policy carefully. By using the App, you consent to the data practices described in this policy.
            
            2. INFORMATION WE COLLECT
            -------------------------------
            We collect the following types of information:
            
            A. Personal Information:
            • Name and email address
            • Phone number
            • Age, gender, height, weight
            • Diabetes type
            
            B. Health Data:
            • Blood glucose readings
            • Insulin doses and calculations
            • Carbohydrate intake
            • Calendar events (medications, appointments, meals)
            • Food scan history
            • Favorite recipes
            
            C. Device Information:
            • Device model and OS version
            • App usage statistics
            • Crash reports
            
            D. Automatically Collected Information:
            • IP address
            • Timestamps
            • App interactions
            
            3. HOW WE USE YOUR INFORMATION
            -------------------------------
            We use your information to:
            • Provide and maintain the App's functionality
            • Calculate insulin recommendations
            • Generate health reports and trends
            • Send medication reminders and notifications
            • Improve and optimize the App
            • Provide customer support
            • Comply with legal obligations
            
            4. DATA STORAGE AND SECURITY
            -------------------------------
            • Your data is stored locally on your device using SQLite database
            • If you enable cloud backup, data is encrypted and stored securely with Firebase
            • We implement industry-standard security measures to protect your data
            • No method of transmission over the internet is 100% secure
            
            5. DATA SHARING
            -------------------------------
            We do NOT sell, rent, or trade your personal information.
            
            We may share data in the following situations:
            • With your explicit consent
            • To comply with legal requirements
            • To protect our rights and safety
            • With service providers who help operate the App (Firebase, etc.)
            
            6. CLOUD SYNC (OPTIONAL)
            -------------------------------
            You have the option to enable cloud backup using your Google account. When enabled:
            • Your data is synced to Firebase Firestore
            • Data is encrypted during transmission
            • You can restore your data if you reinstall the App
            
            You can disable cloud sync at any time in Settings.
            
            7. NOTIFICATIONS AND REMINDERS
            -------------------------------
            The App may send:
            • Medication reminders
            • Appointment alerts
            • Blood sugar check reminders
            
            You can manage notification preferences in Settings.
            
            8. PERMISSIONS
            -------------------------------
            The App requires the following permissions:
            • Camera: For food barcode scanning
            • Notifications: For reminders
            • Schedule Exact Alarms: For timely reminders
            
            You can revoke permissions at any time in your device settings.
            
            9. CHILDREN'S PRIVACY
            -------------------------------
            The App is not intended for children under 13. We do not knowingly collect personal information from children under 13.
            
            10. YOUR RIGHTS
            -------------------------------
            You have the right to:
            • Access your personal data
            • Correct inaccurate data
            • Delete your data
            • Export your data (CSV/PDF reports)
            • Opt out of notifications
            
            11. DATA RETENTION
            -------------------------------
            We retain your data as long as your account is active. You can delete your account and all associated data at any time through the App.
            
            12. THIRD-PARTY SERVICES
            -------------------------------
            The App uses the following third-party services:
            • Firebase (Authentication, Firestore, Analytics)
            • MPAndroidChart (for reports)
            
            These services have their own privacy policies.
            
            13. CHANGES TO THIS POLICY
            -------------------------------
            We may update this Privacy Policy from time to time. We will notify you of any changes by posting the new policy in the App and updating the "Last Updated" date.
            
            14. CONTACT US
            -------------------------------
            If you have questions about this Privacy Policy, contact us:
            • Email: privacy@glucare.com
            
            15. CONSENT
            -------------------------------
            By using Glucare, you consent to this Privacy Policy and agree to its terms.
            
            © 2026 Glucare. All rights reserved.
        """.trimIndent()
    }
}