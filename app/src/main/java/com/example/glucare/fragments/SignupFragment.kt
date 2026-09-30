package com.example.glucare.fragments

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.glucare.GlucareApplication
import com.example.glucare.activities.AuthActivity
import com.example.glucare.activities.HealthInfoActivity
import com.example.glucare.activities.TermsOfServiceActivity
import com.example.glucare.databinding.FragmentSignupBinding
import com.example.glucare.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnCreateAccount.setOnClickListener { attemptSignup() }
        binding.tvLoginLink.setOnClickListener {
            (requireActivity() as? AuthActivity)?.switchToTab(0)
        }

        binding.tvTermsLink.setOnClickListener {
            val intent = Intent(requireContext(), TermsOfServiceActivity::class.java)
            startActivity(intent)
        }
    }

    private fun attemptSignup() {
        // Clear errors
        binding.signupNameLayout.error = null
        binding.signupEmailLayout.error = null
        binding.signupPhoneLayout.error = null
        binding.signupPasswordLayout.error = null
        binding.signupConfirmPasswordLayout.error = null

        val fullName = binding.etSignupName.text.toString().trim()
        val email = binding.etSignupEmail.text.toString().trim()
        val phone = binding.etSignupPhone.text.toString().trim()
        val password = binding.etSignupPassword.text.toString().trim()
        val confirmPassword = binding.etSignupConfirmPassword.text.toString().trim()
        val termsAccepted = binding.cbTermsAgree.isChecked

        var cancel = false
        var focusView: View? = null

        if (!termsAccepted) {
            Toast.makeText(requireContext(),
                when (getAppLanguage()) {
                    "fr" -> "Vous devez accepter les conditions d'utilisation"
                    "ar" -> "يجب عليك الموافقة على شروط الخدمة"
                    else -> "You must agree to the Terms of Service"
                },
                Toast.LENGTH_LONG).show()
            focusView = binding.cbTermsAgree
            cancel = true
        }

        if (TextUtils.isEmpty(fullName)) {
            binding.signupNameLayout.error = "Full name is required"
            focusView = binding.etSignupName
            cancel = true
        }
        if (TextUtils.isEmpty(email)) {
            binding.signupEmailLayout.error = "Email is required"
            focusView = binding.etSignupEmail
            cancel = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.signupEmailLayout.error = "Enter a valid email address"
            focusView = binding.etSignupEmail
            cancel = true
        }
        if (TextUtils.isEmpty(phone)) {
            binding.signupPhoneLayout.error = "Phone number is required"
            focusView = binding.etSignupPhone
            cancel = true
        }
        // Replace the password validation section with this:

        if (TextUtils.isEmpty(password)) {
            binding.signupPasswordLayout.error = "Password is required"
            focusView = binding.etSignupPassword
            cancel = true
        } else if (password.length < 8) {
            binding.signupPasswordLayout.error = "Password must be at least 8 characters"
            focusView = binding.etSignupPassword
            cancel = true
        } else if (!password.matches(Regex(".*[A-Z].*"))) {
            binding.signupPasswordLayout.error = "Password must contain at least one uppercase letter"
            focusView = binding.etSignupPassword
            cancel = true
        } else if (!password.matches(Regex(".*[a-z].*"))) {
            binding.signupPasswordLayout.error = "Password must contain at least one lowercase letter"
            focusView = binding.etSignupPassword
            cancel = true
        } else if (!password.matches(Regex(".*\\d.*"))) {
            binding.signupPasswordLayout.error = "Password must contain at least one number"
            focusView = binding.etSignupPassword
            cancel = true
        } else if (!password.matches(Regex(".*[@#\$%^&+=!].*"))) {
            binding.signupPasswordLayout.error = "Password must contain at least one special character (@#\$%^&+=!)"
            focusView = binding.etSignupPassword
            cancel = true
        }

        if (cancel) {
            focusView?.requestFocus()
        } else {
            performSignup(fullName, email, phone, password)
        }
    }

    private fun getAppLanguage(): String {
        val prefs = requireContext().getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
        return prefs.getString("app_language", "en") ?: "en"
    }

    private fun performSignup(fullName: String, email: String, phone: String, password: String) {
        binding.btnCreateAccount.isEnabled = false
        val app = requireActivity().application as GlucareApplication

        lifecycleScope.launch {
            try {
                val exists = withContext(Dispatchers.IO) { app.databaseHelper.checkUserExists(email) }
                if (exists) {
                    Toast.makeText(requireContext(), "Email already registered", Toast.LENGTH_SHORT).show()
                    binding.btnCreateAccount.isEnabled = true
                    return@launch
                }

                val user = User(fullName = fullName, email = email, phone = phone, password = password)

                // Register with Firebase + push profile
                val uid = app.syncManager.registerAndSync(email, password, user)

                // Insert into local SQLite
                val localUserId = withContext(Dispatchers.IO) {
                    app.databaseHelper.insertUser(user).toInt()
                }

                if (localUserId > 0) {
                    app.sessionManager.saveUser(localUserId, fullName, email)
                    app.sessionManager.saveCredentials(email, password)
                    if (uid != null) app.sessionManager.saveFirebaseUid(uid)

                    // Mark terms as accepted for this user
                    requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("terms_accepted_$email", true)
                        .apply()

                    Toast.makeText(requireContext(), "Account created!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), HealthInfoActivity::class.java))
                    requireActivity().finish()
                } else {
                    Toast.makeText(requireContext(), "Registration failed", Toast.LENGTH_SHORT).show()
                    binding.btnCreateAccount.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnCreateAccount.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}