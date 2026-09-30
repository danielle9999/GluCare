// services/DiabetesAdvisor.kt
package com.example.glucare.services

import com.example.glucare.database.DatabaseHelper
import com.example.glucare.models.GlucoseReading
import com.example.glucare.models.InsulinDose
import com.example.glucare.models.CalendarEvent
import java.util.*

class DiabetesAdvisor(
    private val dbHelper: DatabaseHelper,
    private val userId: Int
) {

    data class Advice(
        val title: String,
        val message: String,
        val type: AdviceType,
        val icon: String,
        val action: AdviceAction? = null
    )

    enum class AdviceType {
        WARNING,    // Red - urgent
        TIP,        // Blue - informational
        SUCCESS,    // Green - positive
        INSIGHT     // Purple - pattern
    }

    enum class AdviceAction {
        LOG_GLUCOSE,
        VIEW_CALENDAR,
        OPEN_INSULIN_CALC,
        CHECK_KETONES
    }

    fun getDailyAdvice(): List<Advice> {
        val advice = mutableListOf<Advice>()
        val latestGlucose = dbHelper.getLatestGlucoseReading(userId)
        val recentReadings = dbHelper.getGlucoseReadings(userId).takeLast(30)
        val recentDoses = dbHelper.getInsulinHistory(userId).takeLast(20)
        val todayEvents = getTodayEvents()

        // 1. URGENT: Critical glucose levels
        if (latestGlucose != null) {
            when {
                latestGlucose.value < 54 -> advice.add(Advice(
                    title = "🚨 SEVERE HYPOGLYCEMIA",
                    message = "Blood glucose critically low! Take 15-20g fast-acting carbs immediately (juice, glucose tablets). Recheck in 15 minutes.",
                    type = AdviceType.WARNING,
                    icon = "⚠️",
                    action = AdviceAction.LOG_GLUCOSE
                ))
                latestGlucose.value < 70 -> advice.add(Advice(
                    title = "⚠️ Low Glucose",
                    message = "Your glucose is below target. Have 15g carbs and recheck in 15 minutes.",
                    type = AdviceType.WARNING,
                    icon = "🍬",
                    action = AdviceAction.LOG_GLUCOSE
                ))
                latestGlucose.value > 300 -> advice.add(Advice(
                    title = "🚨 VERY HIGH GLUCOSE",
                    message = "Blood glucose critically high! Check for ketones, drink water, and consider a correction dose.",
                    type = AdviceType.WARNING,
                    icon = "🔥",
                    action = AdviceAction.CHECK_KETONES
                ))
                latestGlucose.value > 250 -> advice.add(Advice(
                    title = "⚠️ High Glucose",
                    message = "Your glucose is above target. Check ketones and consider a correction dose.",
                    type = AdviceType.WARNING,
                    icon = "📈",
                    action = AdviceAction.OPEN_INSULIN_CALC
                ))
            }
        }

        // 2. Pattern: Dawn Phenomenon (high morning readings)
        val morningReadings = recentReadings.filter { reading ->
            val hour = Calendar.getInstance().apply {
                timeInMillis = reading.timestamp * 1000
            }.get(Calendar.HOUR_OF_DAY)
            hour in 5..9
        }

        if (morningReadings.size >= 3) {
            val avgMorning = morningReadings.map { it.value }.average()
            if (avgMorning > 150) {
                advice.add(Advice(
                    title = "🌅 Dawn Phenomenon Detected",
                    message = "Your morning readings average ${avgMorning.toInt()} mg/dL. Consider discussing basal insulin adjustment with your doctor.",
                    type = AdviceType.INSIGHT,
                    icon = "📊"
                ))
            }
        }

        // 3. Pattern: Post-meal spikes
        val afterMealReadings = recentReadings.filter { it.readingType == GlucoseReading.ReadingType.AFTER_MEAL }
        if (afterMealReadings.size >= 3) {
            val avgAfterMeal = afterMealReadings.map { it.value }.average()
            if (avgAfterMeal > 180) {
                advice.add(Advice(
                    title = "🍽️ Post-Meal Spikes",
                    message = "Your after-meal readings are high (avg ${avgAfterMeal.toInt()} mg/dL). Try reducing carbs or adjusting mealtime insulin.",
                    type = AdviceType.INSIGHT,
                    icon = "📈"
                ))
            }
        }

        // 4. Success: Good control
        val inRange = recentReadings.count { it.value in 70..180 }
        if (recentReadings.size >= 10 && inRange > recentReadings.size * 0.7) {
            advice.add(Advice(
                title = "🎉 Great Control!",
                message = "${(inRange * 100 / recentReadings.size)}% of your readings are in range. Keep up the good work!",
                type = AdviceType.SUCCESS,
                icon = "✅"
            ))
        }

        // 5. Missed doses
        val today = Calendar.getInstance().timeInMillis / 1000
        val yesterday = today - 24*60*60
        val dosesToday = recentDoses.count { it.timestamp > yesterday }

        if (dosesToday == 0 && todayEvents.isNotEmpty()) {
            advice.add(Advice(
                title = "💊 Missed Medication?",
                message = "You have events today but no insulin doses logged. Don't forget your medication!",
                type = AdviceType.TIP,
                icon = "⏰",
                action = AdviceAction.VIEW_CALENDAR
            ))
        }

        // 6. Hydration tip
        if (latestGlucose != null && latestGlucose.value > 200) {
            advice.add(Advice(
                title = "💧 Stay Hydrated",
                message = "High glucose can cause dehydration. Drink a glass of water now.",
                type = AdviceType.TIP,
                icon = "🚰"
            ))
        }

        // 7. Weekly summary
        if (recentReadings.size >= 7) {
            val weeklyAvg = recentReadings.take(7).map { it.value }.average().toInt()
            val trend = getTrend(recentReadings.take(7))
            advice.add(Advice(
                title = "📊 Weekly Summary",
                message = "7-day average: $weeklyAvg mg/dL • Trend: $trend",
                type = AdviceType.INSIGHT,
                icon = "📉"
            ))
        }

        return advice.sortedByDescending { it.type == AdviceType.WARNING }
    }

    private fun getTodayEvents(): List<CalendarEvent> {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayEnd = todayStart + 24*60*60*1000 - 1

        return dbHelper.getEventsForDay(userId, todayStart)
    }

    private fun getTrend(readings: List<GlucoseReading>): String {
        if (readings.size < 3) return "Insufficient data"

        val first = readings.first().value
        val last = readings.last().value

        return when {
            last < first - 20 -> "Improving 📉"
            last > first + 20 -> "Rising 📈"
            else -> "Stable 📊"
        }
    }

    fun getSpecificAdvice(glucose: Int, context: String): String {
        return when (context) {
            "before_meal" -> {
                if (glucose < 100) "Good before meal! Consider reducing insulin slightly to prevent hypo."
                else if (glucose > 180) "High before meal. You may need more insulin or a pre-meal walk."
                else "Good starting point. Calculate your dose normally."
            }
            "after_meal" -> {
                if (glucose < 140) "Excellent! Your dose worked perfectly."
                else if (glucose < 180) "Good control. Slightly high but acceptable."
                else if (glucose < 250) "Post-meal spike. Next time try reducing carbs or increasing insulin."
                else "Significant spike. Consider reviewing your meal composition."
            }
            "bedtime" -> {
                if (glucose < 100) "Low before bed. Have a small snack to prevent nighttime hypo."
                else if (glucose > 200) "High before bed. Consider a correction dose or evening walk."
                else "Good night! Your glucose is well controlled."
            }
            else -> "Keep monitoring your glucose regularly."
        }
    }
}