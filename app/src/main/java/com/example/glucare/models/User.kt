package com.example.glucare.models

data class User(
    val id: Int = 0,
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String,
    val age: Int? = null,
    val gender: String? = null,
    val height: Float? = null,
    val weight: Float? = null,
    val diabetesType: String? = null,
    val avgBloodSugar: Float? = null,
    val insulinRatio: Int = 10,
    val appwriteId: String? = null  // Add this

)