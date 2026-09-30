package com.example.glucare.models

data class Doctor(
    val id: Int = 0,           // DB primary key
    val name: String = "",
    val specialty: String = "",
    val address: String = "",
    val phone: String = "",
    val wilaya: String = "",   // used for filtering
    val wilayaCode: Int = 0,   // 1–58 Algeria official codes
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    /** Two-letter initials for avatar circle */
    fun initials(): String =
        name.removePrefix("Dr. ").split(" ")
            .take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
}