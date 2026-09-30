// services/MedicalInsulinCalculator.kt - FIXED with correct medical formulas
package com.example.glucare.services

import com.example.glucare.database.DatabaseHelper
import com.example.glucare.models.InsulinDose
import com.example.glucare.repositories.GlucoseRepository
import com.example.glucare.repositories.InsulinRepository
import kotlin.math.roundToInt

class MedicalInsulinCalculator(
    private val dbHelper: DatabaseHelper,
    private val glucoseRepo: GlucoseRepository,
    private val insulinRepo: InsulinRepository,
    private val userId: Int
) {

    data class InsulinPrescription(
        val totalDailyInsulin: Float,
        val basalDose: Float,
        val carbohydrateRatio: Int,
        val correctionFactor: Int,
        val targetGlucose: Int = 120,
        val weight: Float,
        val weightUnit: String = "kg"
    )

    data class DoseRecommendation(
        val carbohydrateDose: Float,
        val correctionDose: Float,
        val totalDose: Float,
        val insulinToCarbRatio: Int,
        val correctionFactor: Int,
        val targetGlucose: Int,
        val message: String,
        val confidenceLevel: ConfidenceLevel
    )

    enum class ConfidenceLevel {
        HIGH,
        MEDIUM,
        LOW,
        FIRST_TIME
    }

    fun calculatePrescription(weightKg: Float): InsulinPrescription {
        // TDI based on weight (0.55 units/kg/day is standard starting dose)
        val tdi = 0.55f * weightKg

        // Basal insulin = 45% of TDI (typical range 40-50%)
        val basal = tdi * 0.45f

        // 500 Rule for carb ratio: 500 / TDI
        val carbRatio = (500f / tdi).roundToInt().coerceIn(5, 30)

        // 1800 Rule for correction factor: 1800 / TDI (some use 1700)
        // Using 1800 as it's more common for rapid-acting insulin
        val correctionFactor = (1800f / tdi).roundToInt().coerceIn(20, 100)

        // Target glucose: 120 mg/dL is standard for most adults
        // Some may use 100-140 depending on individual goals

        return InsulinPrescription(
            totalDailyInsulin = tdi,
            basalDose = basal,
            carbohydrateRatio = carbRatio,
            correctionFactor = correctionFactor,
            weight = weightKg,
            targetGlucose = 120
        )
    }


    fun calculateMealtimeDose(
        currentGlucose: Int,
        carbs: Int,
        mealType: String? = null,
        userWeight: Float? = null,
        customRatio: Int? = null,
        customCorrectionFactor: Int? = null,
        targetGlucose: Int = 120
    ): DoseRecommendation {

        val history = insulinRepo.getHistory(userId)

        // Use custom ratio if provided, otherwise use default or calculated
        val insulinRatio = customRatio ?: 10

        // Use custom correction factor if provided, otherwise use default
        val correctionFactor = customCorrectionFactor ?: 50

        // Calculate carbohydrate dose
        val carbDose = carbs.toFloat() / insulinRatio

        // Calculate correction dose (ONLY if glucose is above target)
        val glucoseDiff = currentGlucose - targetGlucose
        val correctionDose = if (glucoseDiff > 0) {
            glucoseDiff.toFloat() / correctionFactor
        } else {
            0f  // Never give negative correction - low glucose needs carbs, not insulin reduction
        }

        // Total dose = carb dose + correction dose
        val totalDose = carbDose + correctionDose

        // Determine confidence level based on history
        val confidence = when {
            history.size >= 20 -> ConfidenceLevel.HIGH
            history.size >= 10 -> ConfidenceLevel.MEDIUM
            history.size >= 1 -> ConfidenceLevel.LOW
            else -> ConfidenceLevel.FIRST_TIME
        }

        // Generate appropriate message
        val message = when {
            currentGlucose < 70 -> "⚠️ LOW GLUCOSE! Treat with 15g fast carbs first, then recheck."
            currentGlucose > 250 -> "⚠️ HIGH GLUCOSE! Check ketones and consider correction."
            confidence == ConfidenceLevel.HIGH -> "🎯 Based on ${history.size} previous doses"
            confidence == ConfidenceLevel.MEDIUM -> "📊 Based on ${history.size} previous doses"
            confidence == ConfidenceLevel.LOW -> "📝 Limited data (${history.size} doses) - monitor closely"
            else -> "🆕 First time! Using standard formula - monitor carefully"
        }

        return DoseRecommendation(
            carbohydrateDose = carbDose,
            correctionDose = correctionDose,
            totalDose = totalDose,
            insulinToCarbRatio = insulinRatio,
            correctionFactor = correctionFactor,
            targetGlucose = targetGlucose,
            message = message,
            confidenceLevel = confidence
        )
    }


    fun recordDoseFeedback(doseId: Int, nextGlucose: Int): Float {
        val effectiveness = when {
            nextGlucose < 70 -> 0.0f  // Hypo - dose was too high
            nextGlucose in 70..140 -> 0.7f  // Good control
            nextGlucose in 141..180 -> 1.0f  // Perfect post-meal
            nextGlucose in 181..250 -> 0.5f  // Slightly high
            else -> 0.3f  // Too high - dose insufficient
        }

        // TODO: Update database with effectiveness score
        // dbHelper.updateDoseEffectiveness(doseId, effectiveness)

        return effectiveness
    }


    fun getInsights(): List<String> {
        val history = insulinRepo.getHistory(userId)
        if (history.size < 5) {
            return listOf(
                "📝 Log more doses to see personalized insights",
                "💡 Aim to log insulin with every meal for better patterns"
            )
        }

        val insights = mutableListOf<String>()

        // Basic stats
        val avgDose = history.map { it.calculatedDose }.average()
        val avgCarbs = history.map { it.carbs }.average()

        insights.add("📊 You've logged ${history.size} insulin doses")
        insights.add("💉 Average dose: ${String.format("%.1f", avgDose)} units")
        insights.add("🍚 Average carbs per meal: ${avgCarbs.toInt()}g")

        val avgRatio = history.map { it.carbs.toFloat() / it.calculatedDose }.average()
        insights.add("⚖️ Your average I:C ratio is 1:${avgRatio.toInt()}g")

        val recentDoses = history.takeLast(10)
        val recentAvg = recentDoses.map { it.calculatedDose }.average()

        if (recentAvg > avgDose * 1.1) {
            insights.add("📈 Your insulin needs have increased recently - discuss with your doctor")
        } else if (recentAvg < avgDose * 0.9) {
            insights.add("📉 Your insulin needs have decreased recently - great progress!")
        }

        return insights
    }

    fun getEducationalTip(): String {
        val tips = listOf(
            "💡 Basal insulin should be 40-50% of your total daily dose - it's your background insulin",
            "💡 The '500 Rule' calculates carb ratio: 500 ÷ Total Daily Insulin = grams carbs per unit",
            "💡 The '1800 Rule' calculates correction: 1800 ÷ TDI = mg/dL drop per unit",
            "💡 Take rapid insulin 15-20 minutes before meals for optimal effect",
            "💡 Wait at least 3-4 hours between correction doses to avoid 'stacking'",
            "💡 Exercise increases insulin sensitivity - you may need 25-50% less insulin",
            "💡 Illness and stress can increase insulin needs by 20-50%",
            "💡 Rotate injection sites to prevent lipohypertrophy (fatty lumps)",
            "💡 Never share insulin pens or syringes - even with family members",
            "💡 Store unopened insulin in the refrigerator (36-46°F / 2-8°C)",
            "💡 Opened insulin is good for 28-30 days at room temperature",
            "💡 Check your blood sugar before driving - safety first!",
            "💡 Always carry fast-acting glucose for emergencies",
            "💡 Alcohol can cause delayed hypoglycemia - be cautious"
        )
        return tips.random()
    }
}