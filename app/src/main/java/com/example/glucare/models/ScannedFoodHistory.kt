package com.example.glucare.models

data class ScannedFoodHistory(
    val id: Int = 0,
    val userId: Int = 0,
    val foodName: String = "",
    val barcode: String? = null,
    val calories: Float = 0f,
    val sugar: Float = 0f,
    val carbs: Float = 0f,
    val protein: Float = 0f,
    val fat: Float = 0f,
    val fiber: Float = 0f,
    val scanMethod: ScanMethod = ScanMethod.SEARCH,
    val timestamp: Long = System.currentTimeMillis() / 1000
) {
    enum class ScanMethod {
        BARCODE, CAMERA, SEARCH
    }

    fun getSugarLevel(): SugarLevel = when {
        sugar < 5f  -> SugarLevel.LOW
        sugar < 15f -> SugarLevel.MEDIUM
        else        -> SugarLevel.HIGH
    }

    enum class SugarLevel { LOW, MEDIUM, HIGH }

    fun getSugarTeaspoons(): Float = sugar / 4.2f

    companion object {
        fun fromFoodProduct(product: FoodProduct, userId: Int, method: ScanMethod): ScannedFoodHistory {
            return ScannedFoodHistory(
                userId     = userId,
                foodName   = product.name,
                barcode    = product.barcode,
                calories   = product.calories,
                sugar      = product.sugar,
                carbs      = product.totalCarbs,
                protein    = product.protein,
                fat        = product.fat,
                fiber      = product.fiber,
                scanMethod = method
            )
        }
    }
}