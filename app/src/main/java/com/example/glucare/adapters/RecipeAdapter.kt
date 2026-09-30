package com.example.glucare.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.glucare.repositories.RecipeRepository
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.glucare.R
import com.example.glucare.models.Recipe

class RecipeAdapter(
    private val items: MutableList<Recipe> = mutableListOf(),
    private val onClick: (Recipe) -> Unit,
    private val repo: RecipeRepository,
    private val onFavoriteClick: (Recipe, Boolean) -> Unit,
    private var favoriteIds: Set<String> = emptySet()
) : RecyclerView.Adapter<RecipeAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivRecipeThumbnail: ImageView = itemView.findViewById(R.id.ivRecipeThumbnail)
        val tvName: TextView = itemView.findViewById(R.id.tvRecipeName)
        val tvCategory: TextView = itemView.findViewById(R.id.tvRecipeCategory)
        val tvPrepTime: TextView = itemView.findViewById(R.id.tvRecipePrepTime)
        val tvCalories: TextView = itemView.findViewById(R.id.tvRecipeCalories)
        val tvSugar: TextView = itemView.findViewById(R.id.tvRecipeSugar)
        val tvCarbs: TextView = itemView.findViewById(R.id.tvRecipeCarbs)
        val tvProtein: TextView = itemView.findViewById(R.id.tvRecipeProtein)
        val tvDiabBadge: TextView = itemView.findViewById(R.id.tvDiabeticBadge)
        val ivFavorite: ImageView = itemView.findViewById(R.id.ivFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = items[position]
        val context = holder.itemView.context

        // Set recipe thumbnail image
        if (recipe.imageResId != 0) {
            holder.ivRecipeThumbnail.setImageResource(recipe.imageResId)
        } else {
            holder.ivRecipeThumbnail.setImageResource(R.drawable.ic_recipe)
        }

        // FIXED: Use repo methods to get translated text
        holder.tvName.text = repo.getRecipeName(recipe)
        holder.tvCategory.text = repo.getCategoryName(recipe)
        holder.tvPrepTime.text = "${recipe.prepTime} min"
        holder.tvCalories.text = "${recipe.calories} kcal"
        holder.tvSugar.text = "${recipe.sugar.toInt()}g"
        holder.tvCarbs.text = "${recipe.carbs.toInt()}g"
        holder.tvProtein.text = "${recipe.protein.toInt()}g"

        // Favorite icon
        val isFav = favoriteIds.contains(recipe.id)
        holder.ivFavorite.setImageResource(
            if (isFav) R.drawable.fav else R.drawable.empty_fav
        )
        holder.ivFavorite.setOnClickListener {
            onFavoriteClick(recipe, !isFav)
        }

        // Diabetic-friendly badge
        if (recipe.diabeticFriendly) {
            holder.tvDiabBadge.visibility = View.VISIBLE
            holder.tvDiabBadge.text = "✓ DB Friendly"
        } else {
            holder.tvDiabBadge.visibility = View.GONE
        }

        // Sugar level color
        val sugarColor = when (recipe.sugarLevel()) {
            Recipe.SugarLevel.LOW -> ContextCompat.getColor(context, R.color.primary_green)
            Recipe.SugarLevel.MEDIUM -> ContextCompat.getColor(context, R.color.warning_orange)
            Recipe.SugarLevel.HIGH -> ContextCompat.getColor(context, R.color.error_red)
        }
        holder.tvSugar.setTextColor(sugarColor)

        holder.itemView.setOnClickListener { onClick(recipe) }
    }

    fun submitList(newItems: List<Recipe>, newFavoriteIds: Set<String> = emptySet()) {
        items.clear()
        items.addAll(newItems)
        favoriteIds = newFavoriteIds
        notifyDataSetChanged()
    }

    fun getCurrentList(): List<Recipe> = items.toList()
}