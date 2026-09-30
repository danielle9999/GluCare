package com.example.glucare.models

data class FoodProduct(
    val name: String,
    val barcode: String,
    val calories: Float,
    val totalCarbs: Float,
    val sugar: Float,
    val fiber: Float,
    val protein: Float,
    val fat: Float,
    val servingSize: String,
    val imageUrl: String? = null
)