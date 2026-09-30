// models/MealPlanItem.kt
package com.example.glucare.models

data class MealPlanItem(
    val id: Int = 0,
    val userId: Int,
    val recipeId: String,
    val date: String,  // YYYY-MM-DD
    val mealType: String,  // breakfast, lunch, dinner, snack
    val notes: String = ""
)