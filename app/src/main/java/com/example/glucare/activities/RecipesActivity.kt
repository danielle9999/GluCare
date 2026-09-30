package com.example.glucare.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.glucare.GlucareApplication
import com.example.glucare.R
import com.example.glucare.adapters.RecipeAdapter
import com.example.glucare.databinding.ActivityRecipesBinding
import com.example.glucare.models.Recipe
import com.example.glucare.repositories.FavoritesRepository
import com.example.glucare.repositories.RecipeRepository
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class RecipesActivity : BaseActivity() {

    private lateinit var binding: ActivityRecipesBinding
    private lateinit var adapter: RecipeAdapter
    private lateinit var repo: RecipeRepository
    private lateinit var favoritesRepo: FavoritesRepository
    private lateinit var app: GlucareApplication

    private var selectedCategory = "All"
    private var sortMode = SortMode.NONE
    private var favoriteIds = setOf<String>()
    private var currentUserId = -1

    private enum class SortMode { NONE, LOW_SUGAR, QUICK }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as GlucareApplication
        repo = RecipeRepository(this)
        favoritesRepo = FavoritesRepository(app.databaseHelper)
        currentUserId = app.sessionManager.getUserId()

        setupRecyclerView()
        setupCategoryChips()
        setupSearch()
        setupSortChips()
        loadFavorites()
    }

    private fun setupRecyclerView() {
        // FIXED: Pass repo to adapter
        adapter = RecipeAdapter(
            items = mutableListOf(),
            onClick = { recipe -> showRecipeDetail(recipe) },
            repo = repo,  // ADD THIS LINE
            onFavoriteClick = { recipe, isFavorite -> toggleFavorite(recipe, isFavorite) },
            favoriteIds = favoriteIds
        )
        binding.rvRecipes.layoutManager = LinearLayoutManager(this)
        binding.rvRecipes.adapter = adapter
    }

    private fun loadFavorites() {
        lifecycleScope.launch {
            favoriteIds = withContext(Dispatchers.IO) {
                favoritesRepo.getFavoriteRecipeIds(currentUserId).toSet()
            }
            renderRecipes()
        }
    }

    private fun toggleFavorite(recipe: Recipe, add: Boolean) {
        lifecycleScope.launch {
            val success = withContext(Dispatchers.IO) {
                if (add) {
                    favoritesRepo.addFavorite(currentUserId, recipe.id)
                } else {
                    favoritesRepo.removeFavorite(currentUserId, recipe.id)
                }
            }
            if (success) {
                runOnUiThread {
                    if (add) {
                        favoriteIds = favoriteIds + recipe.id
                        Toast.makeText(this@RecipesActivity, "Added to favorites", Toast.LENGTH_SHORT).show()
                    } else {
                        favoriteIds = favoriteIds - recipe.id
                        Toast.makeText(this@RecipesActivity, "Removed from favorites", Toast.LENGTH_SHORT).show()
                    }
                    renderRecipes()
                }
            }
        }
    }

    private fun setupCategoryChips() {
        val favoritesChip = Chip(this).apply {
            text = "❤️ Favorites"
            isCheckable = true
            isChecked = false
            setOnClickListener {
                selectedCategory = "Favorites"
                renderRecipes()
                for (i in 0 until binding.chipGroupCategories.childCount) {
                    val c = binding.chipGroupCategories.getChildAt(i) as Chip
                    c.isChecked = (c.text == "❤️ Favorites")
                }
            }
        }
        binding.chipGroupCategories.addView(favoritesChip)
        repo.getCategories().forEach { category ->
            val chip = Chip(this).apply {
                text = category
                isCheckable = true
                isChecked = (category == "All")
                setOnClickListener {
                    selectedCategory = category
                    renderRecipes()
                }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun setupSearch() {
        binding.etSearchRecipe.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = renderRecipes()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupSortChips() {
        binding.chipSortLowSugar.setOnClickListener {
            sortMode = if (sortMode == SortMode.LOW_SUGAR) SortMode.NONE else SortMode.LOW_SUGAR
            binding.chipSortLowSugar.isChecked = (sortMode == SortMode.LOW_SUGAR)
            binding.chipSortQuick.isChecked = false
            renderRecipes()
        }
        binding.chipSortQuick.setOnClickListener {
            sortMode = if (sortMode == SortMode.QUICK) SortMode.NONE else SortMode.QUICK
            binding.chipSortQuick.isChecked = (sortMode == SortMode.QUICK)
            binding.chipSortLowSugar.isChecked = false
            renderRecipes()
        }
    }

    private fun renderRecipes() {
        val query = binding.etSearchRecipe.text.toString().trim()

        var results = when {
            selectedCategory == "Favorites" -> {
                val allRecipes = repo.getAll()
                allRecipes.filter { favoriteIds.contains(it.id) }
            }
            query.isEmpty() -> repo.getAll(selectedCategory)
            else -> repo.search(query, selectedCategory)
        }

        results = when (sortMode) {
            SortMode.LOW_SUGAR -> repo.sortByLowSugar(results)
            SortMode.QUICK     -> repo.sortByQuick(results)
            SortMode.NONE      -> results
        }

        if (results.isEmpty()) {
            binding.layoutRecipesEmpty.visibility = View.VISIBLE
            binding.rvRecipes.visibility = View.GONE
            binding.tvRecipeCount.text = when {
                selectedCategory == "Favorites" -> "No favorite recipes yet.\nTap the ❤️ on any recipe to add it here"
                query.isNotEmpty() -> "No recipes found for \"$query\""
                else -> "No recipes in $selectedCategory"
            }
        } else {
            binding.layoutRecipesEmpty.visibility = View.GONE
            binding.rvRecipes.visibility = View.VISIBLE
            val countLabel = when {
                selectedCategory == "Favorites" -> "${results.size} favorite ${if (results.size != 1) "recipes" else "recipe"}"
                query.isNotEmpty() -> "${results.size} result${if (results.size != 1) "s" else ""} for \"$query\""
                selectedCategory != "All" -> "${results.size} in $selectedCategory"
                else -> "${results.size} recipes"
            }
            binding.tvRecipeCount.text = countLabel
            adapter.submitList(results, favoriteIds)
        }
    }

    private fun showRecipeDetail(recipe: Recipe) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_recipe_detail, null)

        // HERO IMAGE
        val ivHero = view.findViewById<ImageView>(R.id.ivDetailHero)
        if (recipe.imageResId != 0) {
            ivHero?.setImageResource(recipe.imageResId)
        } else {
            ivHero?.setImageResource(R.drawable.ic_recipe)
        }

        // FIXED: Use repo methods for translated text
        view.findViewById<TextView>(R.id.tvDetailName)?.text = repo.getRecipeName(recipe)
        view.findViewById<TextView>(R.id.tvDetailCategory)?.text = repo.getCategoryName(recipe)
        view.findViewById<TextView>(R.id.tvDetailPrepTime)?.text = "⏱ ${recipe.prepTime} min"
        view.findViewById<TextView>(R.id.tvDetailCalories)?.text = "🔥 ${recipe.calories} kcal"
        view.findViewById<TextView>(R.id.tvDetailSugar)?.text = "🍬 Sugar: ${recipe.sugar}g  ${recipe.sugarLevelLabel()}"
        view.findViewById<TextView>(R.id.tvDetailCarbs)?.text = "🍞 Carbs: ${recipe.carbs}g"
        view.findViewById<TextView>(R.id.tvDetailProtein)?.text = "🥩 Protein: ${recipe.protein}g"
        view.findViewById<TextView>(R.id.tvDetailFat)?.text = "🥑 Fat: ${recipe.fat}g"
        view.findViewById<TextView>(R.id.tvDetailFiber)?.text = "🌾 Fiber: ${recipe.fiber}g"

        // Ingredients - use repo method
        view.findViewById<TextView>(R.id.tvDetailIngredients)?.text =
            repo.getIngredients(recipe).joinToString("\n") { "• $it" }

        // Instructions - use repo method
        view.findViewById<TextView>(R.id.tvDetailInstructions)?.text =
            repo.getInstructions(recipe).mapIndexed { i, step -> "${i + 1}. $step" }.joinToString("\n")

        // Diabetic badge
        val badge = view.findViewById<TextView>(R.id.tvDetailDiabeticBadge)
        badge?.visibility = if (recipe.diabeticFriendly) View.VISIBLE else View.GONE

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showMealPlanDialog(recipe: Recipe) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_to_meal_plan, null)
        val etDate = dialogView.findViewById<TextInputEditText>(R.id.etMealDate)
        val rgMealType = dialogView.findViewById<RadioGroup>(R.id.rgMealType)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        etDate.setText(sdf.format(Date()))

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                etDate.setText(selectedDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Add to Meal Plan")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val date = etDate.text.toString()
                val mealTypeId = rgMealType.checkedRadioButtonId
                val mealType = when (mealTypeId) {
                    R.id.rbBreakfast -> "breakfast"
                    R.id.rbLunch     -> "lunch"
                    R.id.rbDinner    -> "dinner"
                    R.id.rbSnack     -> "snack"
                    else             -> "other"
                }

                lifecycleScope.launch {
                    val success = withContext(Dispatchers.IO) {
                        app.databaseHelper.addToMealPlan(currentUserId, recipe.id, date, mealType)
                    }
                    runOnUiThread {
                        if (success > 0) {
                            Toast.makeText(this@RecipesActivity, "Added to meal plan!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}