package com.example.glucare.models

data class Recipe(
    val id: String = "",
    // English fields
    val nameEn: String = "",
    val categoryEn: String = "",
    val ingredientsEn: List<String> = emptyList(),
    val instructionsEn: List<String> = emptyList(),
    val tagsEn: List<String> = emptyList(),
    // French fields
    val nameFr: String = "",
    val categoryFr: String = "",
    val ingredientsFr: List<String> = emptyList(),
    val instructionsFr: List<String> = emptyList(),
    val tagsFr: List<String> = emptyList(),
    // Arabic fields
    val nameAr: String = "",
    val categoryAr: String = "",
    val ingredientsAr: List<String> = emptyList(),
    val instructionsAr: List<String> = emptyList(),
    val tagsAr: List<String> = emptyList(),
    // Common fields (same across all languages)
    val calories: Int = 0,
    val carbs: Float = 0f,
    val protein: Float = 0f,
    val fat: Float = 0f,
    val sugar: Float = 0f,
    val fiber: Float = 0f,
    val prepTime: Int = 0,
    val imageResId: Int = 0
) {
    val diabeticFriendly: Boolean get() = sugar <= 10f && carbs <= 45f

    fun sugarLevel(): SugarLevel = when {
        sugar <= 5f -> SugarLevel.LOW
        sugar <= 12f -> SugarLevel.MEDIUM
        else -> SugarLevel.HIGH
    }

    enum class SugarLevel { LOW, MEDIUM, HIGH }

    fun sugarLevelLabel(): String = when (sugarLevel()) {
        SugarLevel.LOW -> "🟢 Low Sugar"
        SugarLevel.MEDIUM -> "🟡 Med Sugar"
        SugarLevel.HIGH -> "🔴 High Sugar"
    }
}