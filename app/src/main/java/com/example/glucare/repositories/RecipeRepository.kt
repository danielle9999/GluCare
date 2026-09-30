package com.example.glucare.repositories

import android.content.Context
import com.example.glucare.R
import com.example.glucare.models.Recipe

class RecipeRepository(private val context: Context) {

    private fun getCurrentLanguage(): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getString("app_language", "en") ?: "en"
    }

    fun getCategories(): List<String> =
        listOf("All", "Favorites") + ALL_RECIPES.map { getCategoryName(it) }.distinct().sorted()

    fun getAll(category: String = "All"): List<Recipe> = when (category) {
        "All" -> ALL_RECIPES
        "Favorites" -> ALL_RECIPES
        else -> ALL_RECIPES.filter { getCategoryName(it) == category }
    }

    fun getFavorites(favoriteIds: Set<String>): List<Recipe> =
        if (favoriteIds.isEmpty()) emptyList()
        else ALL_RECIPES.filter { favoriteIds.contains(it.id) }

    fun search(query: String, category: String = "All"): List<Recipe> {
        val q = query.lowercase().trim()
        if (q.isEmpty()) return getAll(category)

        val words = q.split("\\s+".toRegex()).filter { it.length >= 2 }
        val pool = if (category == "All") ALL_RECIPES
        else ALL_RECIPES.filter { getCategoryName(it) == category }

        data class Scored(val recipe: Recipe, val score: Int)

        return pool.mapNotNull { recipe ->
            val name = getRecipeName(recipe).lowercase()
            val ingrs = getIngredients(recipe).joinToString(" ").lowercase()
            val tags = getTags(recipe).joinToString(" ").lowercase()
            val cat = getCategoryName(recipe).lowercase()

            var score = 0
            if (name == q) score += 100
            if (name.contains(q)) score += 60
            if (words.isNotEmpty() && words.all { name.contains(it) }) score += 40
            words.forEach { w ->
                if (name.contains(w)) score += 20
                if (ingrs.contains(w)) score += 10
                if (tags.contains(w)) score += 15
                if (cat.contains(w)) score += 12
            }
            if (recipe.sugar <= 5f) score += 5
            if (score > 0) Scored(recipe, score) else null
        }.sortedByDescending { it.score }.map { it.recipe }
    }

    fun getRecipeName(recipe: Recipe): String = when (getCurrentLanguage()) {
        "fr" -> recipe.nameFr.ifEmpty { recipe.nameEn }
        "ar" -> recipe.nameAr.ifEmpty { recipe.nameEn }
        else -> recipe.nameEn
    }

    fun getCategoryName(recipe: Recipe): String = when (getCurrentLanguage()) {
        "fr" -> recipe.categoryFr.ifEmpty { recipe.categoryEn }
        "ar" -> recipe.categoryAr.ifEmpty { recipe.categoryEn }
        else -> recipe.categoryEn
    }

    fun getIngredients(recipe: Recipe): List<String> = when (getCurrentLanguage()) {
        "fr" -> if (recipe.ingredientsFr.isNotEmpty()) recipe.ingredientsFr else recipe.ingredientsEn
        "ar" -> if (recipe.ingredientsAr.isNotEmpty()) recipe.ingredientsAr else recipe.ingredientsEn
        else -> recipe.ingredientsEn
    }

    fun getInstructions(recipe: Recipe): List<String> = when (getCurrentLanguage()) {
        "fr" -> if (recipe.instructionsFr.isNotEmpty()) recipe.instructionsFr else recipe.instructionsEn
        "ar" -> if (recipe.instructionsAr.isNotEmpty()) recipe.instructionsAr else recipe.instructionsEn
        else -> recipe.instructionsEn
    }

    fun getTags(recipe: Recipe): List<String> = when (getCurrentLanguage()) {
        "fr" -> if (recipe.tagsFr.isNotEmpty()) recipe.tagsFr else recipe.tagsEn
        "ar" -> if (recipe.tagsAr.isNotEmpty()) recipe.tagsAr else recipe.tagsEn
        else -> recipe.tagsEn
    }

    fun sortByLowSugar(list: List<Recipe>) = list.sortedBy { it.sugar }
    fun sortByQuick(list: List<Recipe>) = list.sortedBy { it.prepTime }

    companion object {
        // Reduced to 20 recipes total: 6 Traditional Algerian + 14 Foreign (balanced across categories)
        val ALL_RECIPES = listOf(
            // ==================== ALGERIAN TRADITIONAL RECIPES (6) ====================
            Recipe(
                id = "r01", imageResId = R.drawable.recipe_harira,
                nameEn = "Harira", nameFr = "Harira", nameAr = "الحريرة",
                categoryEn = "Traditional", categoryFr = "Traditionnel", categoryAr = "تقليدي",
                ingredientsEn = listOf("100g chickpeas", "50g lentils", "tomatoes", "celery", "parsley", "lemon", "spices"),
                ingredientsFr = listOf("100g pois chiches", "50g lentilles", "tomates", "céleri", "persil", "citron", "épices"),
                ingredientsAr = listOf("100g حمص", "50g عدس", "طماطم", "كرفس", "بقدونس", "ليمون", "توابل"),
                instructionsEn = listOf("Sauté vegetables", "Add chickpeas and lentils", "Simmer 30 min", "Add lemon"),
                instructionsFr = listOf("Faire revenir les légumes", "Ajouter pois chiches et lentilles", "Laisser mijoter 30 min", "Ajouter citron"),
                instructionsAr = listOf("اقلي الخضار", "أضف الحمص والعدس", "اتركه ينضج 30 دقيقة", "أضف الليمون"),
                tagsEn = listOf("soup", "ramadan", "algerian"), tagsFr = listOf("soupe", "ramadan", "algérien"), tagsAr = listOf("شوربة", "رمضان", "جزائري"),
                calories = 180, carbs = 20f, protein = 11f, fat = 4f, sugar = 3f, fiber = 5f, prepTime = 40
            ),
            Recipe(
                id = "r02", imageResId = R.drawable.recipe_chorba_frik,
                nameEn = "Chorba Frik", nameFr = "Chorba Frik", nameAr = "شوربة فريك",
                categoryEn = "Traditional", categoryFr = "Traditionnel", categoryAr = "تقليدي",
                ingredientsEn = listOf("100g frik", "lamb broth", "tomatoes", "onion", "coriander", "cinnamon"),
                ingredientsFr = listOf("100g frik", "bouillon d'agneau", "tomates", "oignon", "coriandre", "cannelle"),
                ingredientsAr = listOf("100g فريك", "مرق خروف", "طماطم", "بصل", "كزبرة", "قرفة"),
                instructionsEn = listOf("Sauté onion", "Add frik and broth", "Simmer 35 min"),
                instructionsFr = listOf("Faire revenir l'oignon", "Ajouter frik et bouillon", "Laisser mijoter 35 min"),
                instructionsAr = listOf("اقلي البصل", "أضف الفريك والمرق", "اتركه ينضج 35 دقيقة"),
                tagsEn = listOf("soup", "algerian"), tagsFr = listOf("soupe", "algérien"), tagsAr = listOf("شوربة", "جزائري"),
                calories = 210, carbs = 26f, protein = 14f, fat = 5f, sugar = 2f, fiber = 4f, prepTime = 45
            ),
            Recipe(
                id = "r03", imageResId = R.drawable.recipe_kefta,
                nameEn = "Grilled Kefta", nameFr = "Kefta Grillée", nameAr = "كفتا مشوية",
                categoryEn = "Traditional", categoryFr = "Traditionnel", categoryAr = "تقليدي",
                ingredientsEn = listOf("300g ground beef", "onion", "parsley", "cumin", "coriander"),
                ingredientsFr = listOf("300g bœuf haché", "oignon", "persil", "cumin", "coriandre"),
                ingredientsAr = listOf("300g لحم مفروم", "بصل", "بقدونس", "كمون", "كزبرة"),
                instructionsEn = listOf("Mix meat with spices", "Form skewers", "Grill 4 min/side"),
                instructionsFr = listOf("Mélanger la viande", "Former des brochettes", "Griller 4 min"),
                instructionsAr = listOf("اخلط اللحم", "شكل أسياخ", "اشوي 4 دقائق"),
                tagsEn = listOf("grilled", "algerian"), tagsFr = listOf("grillé", "algérien"), tagsAr = listOf("مشوي", "جزائري"),
                calories = 340, carbs = 8f, protein = 32f, fat = 20f, sugar = 4f, fiber = 2f, prepTime = 20
            ),
            Recipe(
                id = "r04", imageResId = R.drawable.recipe_couscous,
                nameEn = "Vegetable Couscous", nameFr = "Couscous aux Légumes", nameAr = "كسكس بالخضار",
                categoryEn = "Traditional", categoryFr = "Traditionnel", categoryAr = "تقليدي",
                ingredientsEn = listOf("150g couscous", "carrots", "zucchini", "chickpeas", "harissa"),
                ingredientsFr = listOf("150g couscous", "carottes", "courgettes", "pois chiches", "harissa"),
                ingredientsAr = listOf("150g كسكس", "جزر", "كوسا", "حمص", "حريرة"),
                instructionsEn = listOf("Steam couscous", "Cook vegetables", "Serve together"),
                instructionsFr = listOf("Cuire le couscous", "Cuire les légumes", "Servir ensemble"),
                instructionsAr = listOf("اطبخ الكسكس", "اطبخ الخضار", "قدم معاً"),
                tagsEn = listOf("couscous", "algerian"), tagsFr = listOf("couscous", "algérien"), tagsAr = listOf("كسكس", "جزائري"),
                calories = 350, carbs = 48f, protein = 10f, fat = 6f, sugar = 5f, fiber = 7f, prepTime = 35
            ),
            Recipe(
                id = "r05", imageResId = R.drawable.shakshuka,
                nameEn = "Shakshuka", nameFr = "Chakchouka", nameAr = "شكشوكة",
                categoryEn = "Traditional", categoryFr = "Traditionnel", categoryAr = "تقليدي",
                ingredientsEn = listOf("4 eggs", "tomatoes", "bell pepper", "onion", "garlic", "cumin", "paprika"),
                ingredientsFr = listOf("4 œufs", "tomates", "poivron", "oignon", "ail", "cumin", "paprika"),
                ingredientsAr = listOf("4 بيضات", "طماطم", "فلفل", "بصل", "ثوم", "كمون", "بابريكا"),
                instructionsEn = listOf("Sauté peppers", "Add tomatoes", "Add eggs", "Cover until set"),
                instructionsFr = listOf("Faire revenir", "Ajouter tomates", "Ajouter œufs", "Couvrir"),
                instructionsAr = listOf("اقلي الفلفل", "أضف الطماطم", "أضف البيض", "غط حتى ينضج"),
                tagsEn = listOf("eggs", "mediterranean"), tagsFr = listOf("œufs", "méditerranéen"), tagsAr = listOf("بيض", "متوسطي"),
                calories = 230, carbs = 14f, protein = 14f, fat = 12f, sugar = 6f, fiber = 3f, prepTime = 20
            ),
            Recipe(
                id = "r06", imageResId = R.drawable.bourek,
                nameEn = "Bourek", nameFr = "Bourek", nameAr = "بوراك",
                categoryEn = "Traditional", categoryFr = "Traditionnel", categoryAr = "تقليدي",
                ingredientsEn = listOf("Brik pastry", "tuna/meat", "egg", "parsley", "cheese", "oil for frying"),
                ingredientsFr = listOf("Feuilles de brik", "thon/viande", "œuf", "persil", "fromage", "huile pour friture"),
                ingredientsAr = listOf("عجينة بريك", "تونة/لحم", "بيضة", "بقدونس", "جبن", "زيت للقلي"),
                instructionsEn = listOf("Fill pastry", "Fold into triangles", "Fry until golden"),
                instructionsFr = listOf("Garnir la pâte", "Plier en triangles", "Frire jusqu'à dorure"),
                instructionsAr = listOf("احشي العجينة", "اطويها على شكل مثلثات", "اقليها حتى تصبح ذهبية"),
                tagsEn = listOf("appetizer", "algerian"), tagsFr = listOf("apéritif", "algérien"), tagsAr = listOf("مقبلات", "جزائري"),
                calories = 250, carbs = 18f, protein = 12f, fat = 15f, sugar = 2f, fiber = 1f, prepTime = 30
            ),

            // ==================== BREAKFAST (3) ====================
            Recipe(
                id = "r07", imageResId = R.drawable.oat,
                nameEn = "Oat Porridge", nameFr = "Porridge d'Avoine", nameAr = "عصيدة الشوفان",
                categoryEn = "Breakfast", categoryFr = "Petit-déjeuner", categoryAr = "إفطار",
                ingredientsEn = listOf("50g oats", "200ml milk", "50g berries", "1 tsp honey"),
                ingredientsFr = listOf("50g flocons d'avoine", "200ml lait", "50g baies", "1 cc miel"),
                ingredientsAr = listOf("50g شوفان", "200ml حليب", "50g توت", "1 ملعقة صغيرة عسل"),
                instructionsEn = listOf("Boil milk", "Add oats", "Cook 5 min", "Top with berries"),
                instructionsFr = listOf("Porter le lait à ébullition", "Ajouter l'avoine", "Cuire 5 min", "Garnir de baies"),
                instructionsAr = listOf("اغلي الحليب", "أضف الشوفان", "اطبخ 5 دقائق", "زين بالتوت"),
                tagsEn = listOf("healthy", "quick"), tagsFr = listOf("sain", "rapide"), tagsAr = listOf("صحي", "سريع"),
                calories = 180, carbs = 32f, protein = 6f, fat = 4f, sugar = 3f, fiber = 5f, prepTime = 10
            ),
            Recipe(
                id = "r08", imageResId = R.drawable.eggs,
                nameEn = "Scrambled Eggs with Spinach", nameFr = "Œufs Brouillés aux Épinards", nameAr = "بيض مخفوق بالسبانخ",
                categoryEn = "Breakfast", categoryFr = "Petit-déjeuner", categoryAr = "إفطار",
                ingredientsEn = listOf("3 eggs", "spinach", "bell pepper", "olive oil"),
                ingredientsFr = listOf("3 œufs", "épinards", "poivron", "huile d'olive"),
                ingredientsAr = listOf("3 بيضات", "سبانخ", "فلفل", "زيت زيتون"),
                instructionsEn = listOf("Sauté vegetables", "Add eggs", "Scramble gently"),
                instructionsFr = listOf("Faire revenir les légumes", "Ajouter les œufs", "Brouiller doucement"),
                instructionsAr = listOf("اقلي الخضار", "أضف البيض", "اخفق بلطف"),
                tagsEn = listOf("high protein", "keto"), tagsFr = listOf("haute protéine", "kéto"), tagsAr = listOf("بروتين عالي", "كيتو"),
                calories = 210, carbs = 5f, protein = 16f, fat = 14f, sugar = 1f, fiber = 1f, prepTime = 10
            ),
            Recipe(
                id = "r09", imageResId = R.drawable.avocado,
                nameEn = "Avocado Toast", nameFr = "Toast à l'Avocat", nameAr = "توست الأفوكادو",
                categoryEn = "Breakfast", categoryFr = "Petit-déjeuner", categoryAr = "إفطار",
                ingredientsEn = listOf("2 slices whole grain bread", "1 avocado", "lemon juice", "chili flakes"),
                ingredientsFr = listOf("2 tranches pain complet", "1 avocat", "jus de citron", "flocons de piment"),
                ingredientsAr = listOf("2 شريحة خبز أسمر", "1 أفوكادو", "عصير ليمون", "رقائق فلفل"),
                instructionsEn = listOf("Toast bread", "Mash avocado", "Spread on toast", "Season"),
                instructionsFr = listOf("Griller le pain", "Écraser l'avocat", "Étaler sur le toast", "Assaisonner"),
                instructionsAr = listOf("حمص الخبز", "اهرس الأفوكادو", "افرده على التوست", "تبل"),
                tagsEn = listOf("healthy fat", "quick"), tagsFr = listOf("bonnes graisses", "rapide"), tagsAr = listOf("دهون صحية", "سريع"),
                calories = 290, carbs = 20f, protein = 9f, fat = 15f, sugar = 2f, fiber = 7f, prepTime = 8
            ),

            // ==================== LUNCH (3) ====================
            Recipe(
                id = "r10", imageResId = R.drawable.chicken,
                nameEn = "Grilled Chicken Salad", nameFr = "Salade de Poulet Grillé", nameAr = "سلطة دجاج مشوي",
                categoryEn = "Lunch", categoryFr = "Déjeuner", categoryAr = "غداء",
                ingredientsEn = listOf("150g chicken breast", "mixed greens", "cherry tomatoes", "cucumber"),
                ingredientsFr = listOf("150g poitrine de poulet", "mesclun", "tomates cerises", "concombre"),
                ingredientsAr = listOf("150g صدر دجاج", "خضار مشكلة", "طماطم كرزية", "خيار"),
                instructionsEn = listOf("Grill chicken", "Slice", "Toss salad", "Top with chicken"),
                instructionsFr = listOf("Griller le poulet", "Trancher", "Mélanger la salade", "Ajouter le poulet"),
                instructionsAr = listOf("اشوي الدجاج", "قطعه", "اخلط السلطة", "ضع الدجاج فوقها"),
                tagsEn = listOf("high protein", "low carb"), tagsFr = listOf("haute protéine", "low carb"), tagsAr = listOf("بروتين عالي", "قليل الكربوهيدرات"),
                calories = 310, carbs = 10f, protein = 35f, fat = 12f, sugar = 3f, fiber = 3f, prepTime = 20
            ),
            Recipe(
                id = "r11", imageResId = R.drawable.lentil,
                nameEn = "Red Lentil Soup", nameFr = "Soupe aux Lentilles Rouges", nameAr = "شوربة عدس أحمر",
                categoryEn = "Lunch", categoryFr = "Déjeuner", categoryAr = "غداء",
                ingredientsEn = listOf("200g red lentils", "onion", "garlic", "cumin", "paprika", "lemon"),
                ingredientsFr = listOf("200g lentilles rouges", "oignon", "ail", "cumin", "paprika", "citron"),
                ingredientsAr = listOf("200g عدس أحمر", "بصل", "ثوم", "كمون", "بابريكا", "ليمون"),
                instructionsEn = listOf("Sauté onion", "Add lentils and water", "Simmer 20 min", "Blend", "Add lemon"),
                instructionsFr = listOf("Faire revenir l'oignon", "Ajouter lentilles et eau", "Laisser mijoter 20 min", "Mixer", "Ajouter citron"),
                instructionsAr = listOf("اقلي البصل", "أضف العدس والماء", "اتركه ينضج 20 دقيقة", "اخلطه", "أضف الليمون"),
                tagsEn = listOf("soup", "vegan"), tagsFr = listOf("soupe", "végétalien"), tagsAr = listOf("شوربة", "نباتي"),
                calories = 240, carbs = 38f, protein = 14f, fat = 3f, sugar = 2f, fiber = 10f, prepTime = 30
            ),
            Recipe(
                id = "r12", imageResId = R.drawable.recipe_quinoa_bowl,
                nameEn = "Quinoa Vegetable Bowl", nameFr = "Bowl de Quinoa aux Légumes", nameAr = "وعاء الكينوا بالخضار",
                categoryEn = "Lunch", categoryFr = "Déjeuner", categoryAr = "غداء",
                ingredientsEn = listOf("100g quinoa", "roasted peppers", "zucchini", "chickpeas", "tahini"),
                ingredientsFr = listOf("100g quinoa", "poivrons rôtis", "courgettes", "pois chiches", "tahini"),
                ingredientsAr = listOf("100g كينوا", "فلفل مشوي", "كوسا", "حمص", "طحينة"),
                instructionsEn = listOf("Cook quinoa", "Roast veggies", "Assemble bowl", "Drizzle tahini"),
                instructionsFr = listOf("Cuire le quinoa", "Rôtir les légumes", "Assembler le bowl", "Arroser de tahini"),
                instructionsAr = listOf("اطبخ الكينوا", "اشوي الخضار", "رتب الوعاء", "رش بالطحينة"),
                tagsEn = listOf("vegan", "high fiber"), tagsFr = listOf("végétalien", "haute fibre"), tagsAr = listOf("نباتي", "ألياف عالية"),
                calories = 380, carbs = 42f, protein = 14f, fat = 10f, sugar = 5f, fiber = 6f, prepTime = 25
            ),

            // ==================== DINNER (3) ====================
            Recipe(
                id = "r13", imageResId = R.drawable.salmon,
                nameEn = "Baked Salmon & Asparagus", nameFr = "Saumon et Asperges au Four", nameAr = "سلمون مشوي بالفرن مع الهليون",
                categoryEn = "Dinner", categoryFr = "Dîner", categoryAr = "عشاء",
                ingredientsEn = listOf("180g salmon", "asparagus", "lemon", "garlic", "olive oil"),
                ingredientsFr = listOf("180g saumon", "asperges", "citron", "ail", "huile d'olive"),
                ingredientsAr = listOf("180g سلمون", "هليون", "ليمون", "ثوم", "زيت زيتون"),
                instructionsEn = listOf("Season", "Bake at 200°C for 20 min"),
                instructionsFr = listOf("Assaisonner", "Cuire au four à 200°C pendant 20 min"),
                instructionsAr = listOf("تبل", "اخبز في الفرن على 200 درجة مئوية لمدة 20 دقيقة"),
                tagsEn = listOf("baked", "easy"), tagsFr = listOf("cuit au four", "facile"), tagsAr = listOf("مشوي بالفرن", "سهل"),
                calories = 370, carbs = 8f, protein = 36f, fat = 20f, sugar = 3f, fiber = 4f, prepTime = 25
            ),

            Recipe(
                id = "r15", imageResId = R.drawable.recipe_chickpea_curry,
                nameEn = "Chickpea Curry", nameFr = "Curry de Pois Chiches", nameAr = "كاري حمص",
                categoryEn = "Dinner", categoryFr = "Dîner", categoryAr = "عشاء",
                ingredientsEn = listOf("1 can chickpeas", "tomatoes", "onion", "curry powder", "coconut milk"),
                ingredientsFr = listOf("1 boîte pois chiches", "tomates", "oignon", "curry", "lait de coco"),
                ingredientsAr = listOf("1 علبة حمص", "طماطم", "بصل", "كاري", "حليب جوز الهند"),
                instructionsEn = listOf("Sauté onion", "Add tomatoes", "Add chickpeas", "Simmer 15 min"),
                instructionsFr = listOf("Faire revenir l'oignon", "Ajouter tomates", "Ajouter pois chiches", "Laisser mijoter 15 min"),
                instructionsAr = listOf("اقلي البصل", "أضف الطماطم", "أضف الحمص", "اتركه ينضج 15 دقيقة"),
                tagsEn = listOf("vegan", "indian"), tagsFr = listOf("végétalien", "indien"), tagsAr = listOf("نباتي", "هندي"),
                calories = 330, carbs = 42f, protein = 12f, fat = 9f, sugar = 6f, fiber = 9f, prepTime = 30
            ),

            // ==================== SNACKS & DESSERTS (5) ====================


            Recipe(
                id = "r18", imageResId = R.drawable.apple,
                nameEn = "Baked Cinnamon Apple", nameFr = "Pomme Cannelée au Four", nameAr = "تفاح مخبوز بالقرفة",
                categoryEn = "Dessert", categoryFr = "Dessert", categoryAr = "حلوى",
                ingredientsEn = listOf("1 apple", "1 tsp cinnamon", "1 tsp honey", "nutmeg"),
                ingredientsFr = listOf("1 pomme", "1 cc cannelle", "1 cc miel", "muscade"),
                ingredientsAr = listOf("1 تفاحة", "1 ملعقة صغيرة قرفة", "1 ملعقة صغيرة عسل", "جوزة الطيب"),
                instructionsEn = listOf("Core apple", "Fill with mixture", "Bake 15 min at 180°C"),
                instructionsFr = listOf("Évider la pomme", "Garnir du mélange", "Cuire 15 min au four à 180°C"),
                instructionsAr = listOf("فقع التفاحة", "احشِ بالخليط", "اخبز 15 دقيقة على 180 درجة مئوية"),
                tagsEn = listOf("low calorie", "vegan"), tagsFr = listOf("faible calorie", "végétalien"), tagsAr = listOf("قليل السعرات", "نباتي"),
                calories = 95, carbs = 24f, protein = 0f, fat = 0f, sugar = 3f, fiber = 4f, prepTime = 20
            ),
            Recipe(
                id = "r19", imageResId = R.drawable.recipe_almond_cookies,
                nameEn = "Almond Flour Cookies", nameFr = "Biscuits à la Farine d'Amande", nameAr = "بسكويت بدقيق اللوز",
                categoryEn = "Dessert", categoryFr = "Dessert", categoryAr = "حلوى",
                ingredientsEn = listOf("100g almond flour", "2 tbsp sweetener", "1 egg", "vanilla"),
                ingredientsFr = listOf("100g farine d'amande", "2 cc édulcorant", "1 œuf", "vanille"),
                ingredientsAr = listOf("100g دقيق لوز", "2 ملعقة كبيرة محلي", "1 بيضة", "فانيليا"),
                instructionsEn = listOf("Mix ingredients", "Form balls", "Bake 12 min at 175°C"),
                instructionsFr = listOf("Mélanger les ingrédients", "Former des boules", "Cuire 12 min au four à 175°C"),
                instructionsAr = listOf("اخلط المكونات", "شكل كرات", "اخبز 12 دقيقة على 175 درجة مئوية"),
                tagsEn = listOf("keto", "gluten-free"), tagsFr = listOf("kéto", "sans gluten"), tagsAr = listOf("كيتو", "خالي من الجلوتين"),
                calories = 130, carbs = 8f, protein = 4f, fat = 9f, sugar = 4f, fiber = 1f, prepTime = 18
            ),
            Recipe(
                id = "r20", imageResId = R.drawable.recipe_berry_sorbet,
                nameEn = "Berry Sorbet", nameFr = "Sorbet aux Baies", nameAr = "شربات التوت",
                categoryEn = "Dessert", categoryFr = "Dessert", categoryAr = "حلوى",
                ingredientsEn = listOf("200g frozen berries", "1 tbsp lemon juice", "1 tsp honey"),
                ingredientsFr = listOf("200g baies surgelées", "1 cc jus de citron", "1 cc miel"),
                ingredientsAr = listOf("200g توت مجمد", "1 ملعقة كبيرة عصير ليمون", "1 ملعقة صغيرة عسل"),
                instructionsEn = listOf("Blend frozen berries", "Add honey", "Serve immediately"),
                instructionsFr = listOf("Mixer les baies surgelées", "Ajouter le miel", "Servir immédiatement"),
                instructionsAr = listOf("اخلط التوت المجمد", "أضف العسل", "قدم فوراً"),
                tagsEn = listOf("vegan", "low calorie"), tagsFr = listOf("végétalien", "faible calorie"), tagsAr = listOf("نباتي", "قليل السعرات"),
                calories = 70, carbs = 16f, protein = 1f, fat = 0f, sugar = 11f, fiber = 2f, prepTime = 10
            )
        )
    }
}