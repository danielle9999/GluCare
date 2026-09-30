package com.example.glucare.repositories

import com.example.glucare.database.DatabaseHelper
import com.example.glucare.models.User

class UserRepository(private val dbHelper: DatabaseHelper) {

    fun register(user: User): Long {
        // Check if user exists
        if (dbHelper.checkUserExists(user.email)) {
            throw Exception("User with this email already exists")
        }
        return dbHelper.insertUser(user)
    }

    fun login(email: String, password: String): User? {
        return dbHelper.getUser(email, password)
    }

    fun getUserById(userId: Int): User? {
        return dbHelper.getUserById(userId)
    }
    fun verifyIdentity(username: String, email: String): Boolean {
        return dbHelper.verifyUsernameAndEmail(username, email)
    }

    fun resetPassword(username: String, email: String, newPassword: String): Boolean {
        return dbHelper.resetPassword(username, email, newPassword)
    }

    fun updateHealthInfo(
        userId: Int,
        age: Int?,
        gender: String?,
        height: Float?,
        weight: Float?,
        diabetesType: String?,
        avgBloodSugar: Float?
    ): Boolean {
        return dbHelper.updateUserHealthInfo(userId, age, gender, height, weight, diabetesType, avgBloodSugar)
    }
}