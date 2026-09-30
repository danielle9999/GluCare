package com.example.glucare.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.example.glucare.GlucareApplication
import com.example.glucare.R
import com.example.glucare.utils.BiometricHelper

class ForgotPasswordActivity : BaseActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnResetPassword: Button
    private lateinit var btnVerifyBiometric: Button
    private lateinit var layoutPasswordSection: LinearLayout

    private var biometricVerified = false
    private lateinit var app: GlucareApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        app = application as GlucareApplication

        etUsername = findViewById(R.id.etForgotUsername)
        etEmail = findViewById(R.id.etForgotEmail)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmNewPassword)
        btnResetPassword = findViewById(R.id.btnResetPassword)
        btnVerifyBiometric = findViewById(R.id.btnVerifyBiometric)
        layoutPasswordSection = findViewById(R.id.layoutPasswordSection)

        // Initially hide password section
        layoutPasswordSection.visibility = View.GONE
        btnVerifyBiometric.visibility = View.GONE

        findViewById<View>(R.id.btnForgotBack).setOnClickListener { finish() }

        btnVerifyBiometric.setOnClickListener { startBiometricVerification() }
        btnResetPassword.setOnClickListener { attemptReset() }
    }

    private fun startBiometricVerification() {
        if (!BiometricHelper.isBiometricAvailable(this)) {
            val reason = BiometricHelper.getUnavailableReason(this)
            Toast.makeText(this, reason, Toast.LENGTH_LONG).show()
            return
        }

        BiometricHelper.authenticate(
            this,
            "Verify Your Identity",
            "Confirm fingerprint to reset password",
            onSuccess = {
                biometricVerified = true
                runOnUiThread {
                    layoutPasswordSection.visibility = View.VISIBLE
                    btnVerifyBiometric.visibility = View.GONE
                    etNewPassword.requestFocus()
                    Toast.makeText(this, "Identity verified! Set your new password.", Toast.LENGTH_SHORT).show()
                }
            },
            onFailed = {
                runOnUiThread {
                    Toast.makeText(this, "Fingerprint not recognized. Try again.", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { errorCode, errorMessage ->
                runOnUiThread {
                    if (errorCode == 5) return@runOnUiThread // User cancelled
                    if (errorCode == 7) {
                        Toast.makeText(this, "Too many attempts. Try again in 30 seconds.", Toast.LENGTH_LONG).show()
                    } else if (errorCode == 10) {
                        Toast.makeText(this, "Biometric locked. Use device PIN to unlock first.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    private fun attemptReset() {
        etUsername.error = null
        etEmail.error = null
        etNewPassword.error = null
        etConfirmPassword.error = null

        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()

        if (TextUtils.isEmpty(username)) {
            etUsername.error = "Username is required"
            etUsername.requestFocus()
            return
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email address"
            etEmail.requestFocus()
            return
        }

        // Check if account exists
        if (!app.databaseHelper.verifyUsernameAndEmail(username, email)) {
            Toast.makeText(this, "No account found with that username and email.", Toast.LENGTH_LONG).show()
            return
        }

        // If biometric not yet verified, show the biometric button
        if (!biometricVerified) {
            btnVerifyBiometric.visibility = View.VISIBLE
            etUsername.isEnabled = false
            etEmail.isEnabled = false
            Toast.makeText(this, "Account found! Verify your identity to continue.", Toast.LENGTH_SHORT).show()
            return
        }

        // Biometric verified - now check passwords
        val newPass = etNewPassword.text.toString().trim()
        val confirmPass = etConfirmPassword.text.toString().trim()

        if (TextUtils.isEmpty(newPass)) {
            etNewPassword.error = "New password is required"
            etNewPassword.requestFocus()
            return
        }

        if (newPass.length < 8) {
            etNewPassword.error = "Password must be at least 8 characters"
            etNewPassword.requestFocus()
            return
        }

        if (confirmPass != newPass) {
            etConfirmPassword.error = "Passwords do not match"
            etConfirmPassword.requestFocus()
            return
        }

        val success = app.databaseHelper.resetPassword(username, email, newPass)

        if (success) {
            Toast.makeText(this, "Password reset successfully! Please log in.", Toast.LENGTH_LONG).show()
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else {
            Toast.makeText(this, "Reset failed. Try again.", Toast.LENGTH_SHORT).show()
        }
    }
}