package com.example.glucare.repositories

import com.example.glucare.database.DatabaseHelper
import com.example.glucare.models.FoodProduct
import com.example.glucare.models.ScannedFoodHistory

class FoodRepository(private val dbHelper: DatabaseHelper) {

    fun saveScannedFood(scan: ScannedFoodHistory): Long {
        return dbHelper.insertScannedFood(scan)
    }

    fun getScanHistory(userId: Int): List<ScannedFoodHistory> {
        return dbHelper.getScannedHistory(userId)
    }

    // This would connect to a real API in production
    fun searchFoodByName(query: String): List<FoodProduct> {
        // Mock data for demonstration
        return listOf(
            FoodProduct(
                name = "Apple",
                barcode = "123456789",
                calories = 95f,
                totalCarbs = 25f,
                sugar = 19f,
                fiber = 4f,
                protein = 0.5f,
                fat = 0.3f,
                servingSize = "1 medium (182g)"
            ),
            FoodProduct(
                name = "Banana",
                barcode = "987654321",
                calories = 105f,
                totalCarbs = 27f,
                sugar = 14f,
                fiber = 3f,
                protein = 1.3f,
                fat = 0.4f,
                servingSize = "1 medium (118g)"
            )
        )
    }

    fun getProductByBarcode(barcode: String): FoodProduct? {
        // Mock lookup
        return when (barcode) {
            "123456789" -> FoodProduct(
                name = "Apple",
                barcode = barcode,
                calories = 95f,
                totalCarbs = 25f,
                sugar = 19f,
                fiber = 4f,
                protein = 0.5f,
                fat = 0.3f,
                servingSize = "1 medium (182g)"
            )
            "987654321" -> FoodProduct(
                name = "Banana",
                barcode = barcode,
                calories = 105f,
                totalCarbs = 27f,
                sugar = 14f,
                fiber = 3f,
                protein = 1.3f,
                fat = 0.4f,
                servingSize = "1 medium (118g)"
            )
            else -> null
        }
    }
}