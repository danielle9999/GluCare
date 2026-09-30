package com.example.glucare.repositories

import com.example.glucare.database.DatabaseHelper

class FavoritesRepository(private val dbHelper: DatabaseHelper) {

    fun addFavorite(userId: Int, recipeId: String): Boolean {
        return dbHelper.addFavorite(userId, recipeId) > 0
    }

    fun removeFavorite(userId: Int, recipeId: String): Boolean {
        return dbHelper.removeFavorite(userId, recipeId) > 0
    }

    fun isFavorite(userId: Int, recipeId: String): Boolean {
        return dbHelper.isFavorite(userId, recipeId)
    }

    fun getFavoriteRecipeIds(userId: Int): List<String> {
        return dbHelper.getFavoriteRecipeIds(userId)
    }
}