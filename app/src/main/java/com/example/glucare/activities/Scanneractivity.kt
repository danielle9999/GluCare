package com.example.glucare.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.glucare.R
import com.example.glucare.database.DatabaseHelper
import com.example.glucare.models.ScannedFoodHistory
import com.example.glucare.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class ScannerActivity : BaseActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager

    private lateinit var btnCameraMode: Button
    private lateinit var btnBarcodeMode: Button
    private lateinit var cameraPreview: MaterialCardView
    private lateinit var barcodeInput: LinearLayout
    private lateinit var etBarcode: TextInputEditText
    private lateinit var btnLookup: Button
    private lateinit var etSearchFood: TextInputEditText
    private lateinit var resultCard: MaterialCardView

    // Result views
    private lateinit var tvFoodName: TextView
    private lateinit var tvFoodSize: TextView
    private lateinit var tvSugarFocusAmount: TextView
    private lateinit var tvSugarLevelStatus: TextView
    private lateinit var progressSugar: ProgressBar
    private lateinit var tvCaloriesValue: TextView
    private lateinit var tvCarbsValue: TextView
    private lateinit var tvFiberValue: TextView
    private lateinit var tvProteinValue: TextView
    private lateinit var tvFatValue: TextView
    private lateinit var tvNutritionSugarValue: TextView
    private lateinit var tvSugarTeaspoons: TextView
    private lateinit var btnSaveFood: Button
    private lateinit var btnScanAgain: Button

    private var currentFood: ScannedFoodHistory? = null
    private var isCameraMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        db = DatabaseHelper(this)
        session = SessionManager(this)

        initViews()
        setupToggle()
        setupSearch()
        setupResultActions()

        resultCard.visibility = View.GONE
    }

    private fun initViews() {
        btnCameraMode = findViewById(R.id.btnCameraMode)
        btnBarcodeMode = findViewById(R.id.btnBarcodeMode)
        cameraPreview = findViewById(R.id.cameraPreview)
        barcodeInput = findViewById(R.id.barcodeInput)
        etBarcode = findViewById(R.id.etBarcode)
        btnLookup = findViewById(R.id.btnLookup)
        etSearchFood = findViewById(R.id.etSearchFood)
        resultCard = findViewById(R.id.resultCard)

        tvFoodName = findViewById(R.id.tvFoodName)
        tvFoodSize = findViewById(R.id.tvFoodSize)
        tvSugarFocusAmount = findViewById(R.id.tvSugarFocusAmount)
        tvSugarLevelStatus = findViewById(R.id.tvSugarLevelStatus)
        progressSugar = findViewById(R.id.progressSugarLevel)
        tvCaloriesValue = findViewById(R.id.tvCaloriesValue)
        tvCarbsValue = findViewById(R.id.tvCarbsValue)
        tvFiberValue = findViewById(R.id.tvFiberValue)
        tvProteinValue = findViewById(R.id.tvProteinValue)
        tvFatValue = findViewById(R.id.tvFatValue)
        tvNutritionSugarValue = findViewById(R.id.tvNutritionSugarValue)
        tvSugarTeaspoons = findViewById(R.id.tvSugarTeaspoons)
        btnSaveFood = findViewById(R.id.btnSaveFood)
        btnScanAgain = findViewById(R.id.btnScanAgain)
    }

    private fun setupToggle() {
        btnCameraMode.setOnClickListener {
            isCameraMode = true
            cameraPreview.visibility = View.VISIBLE
            barcodeInput.visibility = View.GONE
            btnCameraMode.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFE53935.toInt())
            btnCameraMode.setTextColor(0xFFFFFFFF.toInt())
            btnBarcodeMode.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFFFFFFF.toInt())
            btnBarcodeMode.setTextColor(0xFF424242.toInt())
            requestCameraPermission()
        }

        btnBarcodeMode.setOnClickListener {
            isCameraMode = false
            cameraPreview.visibility = View.GONE
            barcodeInput.visibility = View.VISIBLE
            btnBarcodeMode.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFE53935.toInt())
            btnBarcodeMode.setTextColor(0xFFFFFFFF.toInt())
            btnCameraMode.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFFFFFFF.toInt())
            btnCameraMode.setTextColor(0xFF424242.toInt())
        }

        btnLookup.setOnClickListener {
            val barcode = etBarcode.text.toString().trim()
            if (barcode.isEmpty()) {
                etBarcode.error = "Enter barcode"
                return@setOnClickListener
            }
            lookupBarcode(barcode)
        }
    }

    private fun setupSearch() {
        etSearchFood.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = etSearchFood.text.toString().trim()
                if (query.isNotEmpty()) searchFood(query)
                true
            } else false
        }
    }

    private fun searchFood(name: String) {
        val localFoods = mapOf(
            "apple" to ScannedFoodHistory(foodName = "Apple", calories = 95f, carbs = 25f, sugar = 19f, protein = 0.5f, fat = 0.3f, fiber = 4.4f),
            "banana" to ScannedFoodHistory(foodName = "Banana", calories = 89f, carbs = 23f, sugar = 12f, protein = 1.1f, fat = 0.3f, fiber = 2.6f),
            "orange" to ScannedFoodHistory(foodName = "Orange", calories = 62f, carbs = 15f, sugar = 12f, protein = 1.2f, fat = 0.2f, fiber = 3.1f),
            "bread" to ScannedFoodHistory(foodName = "White Bread (1 slice)", calories = 79f, carbs = 15f, sugar = 1.4f, protein = 2.7f, fat = 1.0f, fiber = 0.6f),
            "coca cola" to ScannedFoodHistory(foodName = "Coca-Cola (330ml)", calories = 139f, carbs = 35f, sugar = 35f, protein = 0f, fat = 0f, fiber = 0f)
        )

        val food = localFoods.entries.firstOrNull { name.lowercase().contains(it.key) }?.value

        if (food != null) {
            showResult(food)
        } else {
            Toast.makeText(this, "Food not found in demo database", Toast.LENGTH_SHORT).show()
        }
    }

    private fun lookupBarcode(barcode: String) {
        val demoFood = ScannedFoodHistory(
            foodName = "Product $barcode",
            barcode = barcode,
            calories = 150f,
            carbs = 20f,
            sugar = 8f,
            protein = 5f,
            fat = 6f,
            fiber = 2f,
            scanMethod = ScannedFoodHistory.ScanMethod.BARCODE
        )
        showResult(demoFood)
    }

    private fun showResult(food: ScannedFoodHistory) {
        currentFood = food
        resultCard.visibility = View.VISIBLE

        tvFoodName.text = food.foodName
        tvFoodSize.text = "Per 100g serving"
        tvSugarFocusAmount.text = food.sugar.toInt().toString()
        tvNutritionSugarValue.text = food.sugar.toInt().toString()
        tvCaloriesValue.text = food.calories.toInt().toString()
        tvCarbsValue.text = food.carbs.toInt().toString()
        tvFiberValue.text = food.fiber.toInt().toString()
        tvProteinValue.text = food.protein.toInt().toString()
        tvFatValue.text = food.fat.toInt().toString()
        tvSugarTeaspoons.text = String.format("%.1f", food.getSugarTeaspoons())

        val sugarLevel = food.getSugarLevel()
        val (statusText, statusColor, progress) = when (sugarLevel) {
            ScannedFoodHistory.SugarLevel.LOW -> Triple("Low", 0xFF43A047.toInt(), 25)
            ScannedFoodHistory.SugarLevel.MEDIUM -> Triple("Medium", 0xFFF57C00.toInt(), 55)
            ScannedFoodHistory.SugarLevel.HIGH -> Triple("High", 0xFFE53935.toInt(), 85)
        }
        tvSugarLevelStatus.text = statusText
        tvSugarLevelStatus.setTextColor(statusColor)
        progressSugar.progress = progress
        progressSugar.progressTintList = android.content.res.ColorStateList.valueOf(statusColor)
    }

    private fun setupResultActions() {
        btnSaveFood.setOnClickListener {
            val food = currentFood ?: return@setOnClickListener
            val toSave = food.copy(
                userId = session.getUserId(),
                timestamp = System.currentTimeMillis() / 1000
            )
            db.insertScannedFood(toSave)
            Toast.makeText(this, "${food.foodName} saved to history", Toast.LENGTH_SHORT).show()
        }

        btnScanAgain.setOnClickListener {
            resultCard.visibility = View.GONE
            currentFood = null
            etSearchFood.text?.clear()
            etBarcode.text?.clear()
        }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }
    }
}
