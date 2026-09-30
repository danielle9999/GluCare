package com.example.glucare.repositories

import com.example.glucare.database.DatabaseHelper
import com.example.glucare.models.InsulinDose

class InsulinRepository(private val dbHelper: DatabaseHelper) {

    fun saveDose(dose: InsulinDose): Long {
        return dbHelper.insertInsulinDose(dose)
    }

    fun getHistory(userId: Int): List<InsulinDose> {
        return dbHelper.getInsulinHistory(userId)
    }

    fun calculateDose(glucose: Int, carbs: Int, ratio: Int): Float {
        // Insulin for carbs
        val carbsDose = carbs.toFloat() / ratio

        // Correction factor if glucose is high
        val correction = if (glucose > 150) {
            (glucose - 150) / 50f
        } else {
            0f
        }

        return (carbsDose + correction).coerceAtLeast(0f)
    }
}