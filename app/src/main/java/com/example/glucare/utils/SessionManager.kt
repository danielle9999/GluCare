package com.example.glucare.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("glucare_session", Context.MODE_PRIVATE)

    companion object {
        const val KEY_USER_ID              = "user_id"
        const val KEY_USER_NAME            = "user_name"
        const val KEY_USER_EMAIL           = "user_email"
        const val KEY_IS_LOGGED_IN         = "is_logged_in"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        const val KEY_HEALTH_INFO_COMPLETED= "health_info_completed"
        const val KEY_SAVED_EMAIL          = "saved_email"
        const val KEY_SAVED_PASSWORD       = "saved_password"
        // NEW: Firebase UID — survives reinstall via Auto Backup
        const val KEY_FIREBASE_UID         = "firebase_uid"
    }

    fun saveUser(userId: Int, name: String, email: String) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    // Firebase UID
    fun saveFirebaseUid(uid: String) {
        prefs.edit().putString(KEY_FIREBASE_UID, uid).apply()
    }

    fun getFirebaseUid(): String? = prefs.getString(KEY_FIREBASE_UID, null)

    // Credentials for auto-fill / restore
    fun saveCredentials(email: String, password: String) {
        prefs.edit().apply {
            putString(KEY_SAVED_EMAIL, email)
            putString(KEY_SAVED_PASSWORD, password)
            apply()
        }
    }

    fun getSavedCredentials(): Pair<String, String>? {
        val email    = prefs.getString(KEY_SAVED_EMAIL, null)
        val password = prefs.getString(KEY_SAVED_PASSWORD, null)
        return if (email != null && password != null) Pair(email, password) else null
    }

    fun clearSavedCredentials() {
        prefs.edit().remove(KEY_SAVED_EMAIL).remove(KEY_SAVED_PASSWORD).apply()
    }

    fun getUserId(): Int          = prefs.getInt(KEY_USER_ID, -1)
    fun getUserName(): String?    = prefs.getString(KEY_USER_NAME, null)
    fun getUserEmail(): String?   = prefs.getString(KEY_USER_EMAIL, null)
    fun isLoggedIn(): Boolean     = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }
    fun isOnboardingCompleted(): Boolean =
        prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    fun setHealthInfoCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_HEALTH_INFO_COMPLETED, completed).apply()
    }
    fun isHealthInfoCompleted(): Boolean =
        prefs.getBoolean(KEY_HEALTH_INFO_COMPLETED, false)

    fun logout() {
        // Keep saved credentials and Firebase UID so restore works on next login
        val email    = prefs.getString(KEY_SAVED_EMAIL, null)
        val password = prefs.getString(KEY_SAVED_PASSWORD, null)
        val uid      = prefs.getString(KEY_FIREBASE_UID, null)
        prefs.edit().clear().apply()
        // Re-save restore hints
        prefs.edit().apply {
            email?.let    { putString(KEY_SAVED_EMAIL,    it) }
            password?.let { putString(KEY_SAVED_PASSWORD, it) }
            uid?.let      { putString(KEY_FIREBASE_UID,   it) }
            apply()
        }
    }
}