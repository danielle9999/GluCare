package com.example.glucare.fragments

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.glucare.GlucareApplication
import com.example.glucare.activities.AuthActivity
import com.example.glucare.activities.ForgotPasswordActivity
import com.example.glucare.activities.HealthInfoActivity
import com.example.glucare.activities.HomeActivity
import com.example.glucare.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Auto-fill saved credentials
        val app = requireActivity().application as GlucareApplication
        val saved = app.sessionManager.getSavedCredentials()
        if (saved != null) {
            binding.etLoginEmail.setText(saved.first)
            binding.etLoginPassword.setText(saved.second)
        }
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(requireContext(), ForgotPasswordActivity::class.java))
        }
        binding.tvSignUpLink.setOnClickListener {
            (requireActivity() as? AuthActivity)?.switchToTab(1)
        }

    }

    private fun attemptLogin() {
        binding.loginEmailLayout.error = null
        binding.loginPasswordLayout.error = null

        val email = binding.etLoginEmail.text.toString().trim()
        val password = binding.etLoginPassword.text.toString().trim()

        var cancel = false
        var focusView: View? = null

        if (TextUtils.isEmpty(email)) {
            binding.loginEmailLayout.error = "Email is required"
            focusView = binding.etLoginEmail
            cancel = true
        }
        if (TextUtils.isEmpty(password)) {
            binding.loginPasswordLayout.error = "Password is required"
            focusView = binding.etLoginPassword
            cancel = true
        }
        if (cancel) focusView?.requestFocus() else performLogin(email, password)
    }

    private fun performLogin(email: String, password: String) {
        binding.btnLogin.isEnabled = false
        val app = requireActivity().application as GlucareApplication

        lifecycleScope.launch {
            try {
                val restoredUserId = app.syncManager.loginAndSync(email, password)
                if (restoredUserId > 0) {
                    app.sessionManager.saveCredentials(email, password)
                    navigateAfterLogin(restoredUserId)
                    return@launch
                }
                val user = app.databaseHelper.getUser(email, password)
                if (user != null) {
                    app.sessionManager.saveUser(user.id, user.fullName, user.email)
                    app.sessionManager.saveCredentials(email, password)
                    navigateAfterLogin(user.id)
                } else {
                    Toast.makeText(requireContext(), "Invalid email or password",
                        Toast.LENGTH_SHORT).show()
                    binding.btnLogin.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnLogin.isEnabled = true
            }
        }
    }

    private fun navigateAfterLogin(userId: Int) {
        val app = requireActivity().application as GlucareApplication
        val user = app.databaseHelper.getUserById(userId)
        val intent = if (user?.age == null || user.height == null) {
            Intent(requireContext(), HealthInfoActivity::class.java)
        } else {
            Intent(requireContext(), HomeActivity::class.java)
        }
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}