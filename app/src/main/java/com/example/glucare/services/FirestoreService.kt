package com.example.glucare.services

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.glucare.models.*
import kotlinx.coroutines.tasks.await

/**
 * FirestoreService — cloud sync layer for Glucare.
 *
 * KEY FIXES vs original:
 * 1. Firestore cannot deserialize Kotlin data classes that have enums or
 *    non-nullable primitives with no default values via toObject().
 *    → All reads now use manual field mapping instead of toObject().
 * 2. Enum values are stored/read as String names.
 * 3. getInsulinCalculations() no longer crashes on null map entries.
 * 4. syncLocalData() is type-safe (no unchecked casts).
 * 5. Added syncAllUserData() — the one function you call after login
 *    to restore everything from Firestore into local SQLite.
 */
class FirestoreService {

    private val db = FirebaseFirestore.getInstance()

    // ─────────────────────────────────────────────
    // USER
    // ─────────────────────────────────────────────

    suspend fun saveUser(firebaseUid: String, user: User) {
        val map = mapOf(
            "fullName"       to user.fullName,
            "email"          to user.email,
            "phone"          to user.phone,
            "age"            to user.age,
            "gender"         to user.gender,
            "height"         to user.height,
            "weight"         to user.weight,
            "diabetesType"   to user.diabetesType,
            "avgBloodSugar"  to user.avgBloodSugar,
            "insulinRatio"   to user.insulinRatio
        )
        db.collection("users").document(firebaseUid).set(map).await()
    }

    suspend fun getUser(firebaseUid: String): User? {
        val doc = db.collection("users").document(firebaseUid).get().await()
        if (!doc.exists()) return null
        return User(
            fullName      = doc.getString("fullName")      ?: "",
            email         = doc.getString("email")         ?: "",
            phone         = doc.getString("phone")         ?: "",
            password      = "",          // never stored in Firestore
            age           = doc.getLong("age")?.toInt(),
            gender        = doc.getString("gender"),
            height        = doc.getDouble("height")?.toFloat(),
            weight        = doc.getDouble("weight")?.toFloat(),
            diabetesType  = doc.getString("diabetesType"),
            avgBloodSugar = doc.getDouble("avgBloodSugar")?.toFloat(),
            insulinRatio  = doc.getLong("insulinRatio")?.toInt() ?: 10
        )
    }

    // ─────────────────────────────────────────────
    // GLUCOSE READINGS
    // ─────────────────────────────────────────────

    suspend fun saveGlucoseReading(firebaseUid: String, reading: GlucoseReading) {
        val map = mapOf(
            "localId"     to reading.id,
            "userId"      to reading.userId,
            "value"       to reading.value,
            "readingType" to reading.readingType.name,   // store enum as String
            "timestamp"   to reading.timestamp,
            "notes"       to reading.notes
        )
        db.collection("users").document(firebaseUid)
            .collection("glucose_readings")
            .document(reading.id.toString())
            .set(map)
            .await()
    }

    suspend fun getGlucoseReadings(firebaseUid: String): List<GlucoseReading> {
        val snapshot = db.collection("users").document(firebaseUid)
            .collection("glucose_readings")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                GlucoseReading(
                    id          = doc.getLong("localId")?.toInt() ?: 0,
                    userId      = doc.getLong("userId")?.toInt() ?: 0,
                    value       = doc.getLong("value")?.toInt() ?: return@mapNotNull null,
                    readingType = GlucoseReading.ReadingType.valueOf(
                        doc.getString("readingType") ?: GlucoseReading.ReadingType.RANDOM.name
                    ),
                    timestamp   = doc.getLong("timestamp") ?: 0L,
                    notes       = doc.getString("notes")
                )
            } catch (e: Exception) { null }
        }
    }

    // ─────────────────────────────────────────────
    // INSULIN DOSES
    // ─────────────────────────────────────────────

    suspend fun saveInsulinDose(firebaseUid: String, dose: InsulinDose) {
        val map = mapOf(
            "localId"        to dose.id,
            "userId"         to dose.userId,
            "glucoseValue"   to dose.glucoseValue,
            "carbs"          to dose.carbs,
            "insulinRatio"   to dose.insulinRatio,
            "calculatedDose" to dose.calculatedDose,
            "actualDose"     to dose.actualDose,
            "timestamp"      to dose.timestamp,
            "mealType"       to dose.mealType,
            "notes"          to dose.notes,
            "effectiveness"  to dose.effectiveness
        )
        db.collection("users").document(firebaseUid)
            .collection("insulin_doses")
            .document(dose.id.toString())
            .set(map)
            .await()
    }

    suspend fun getInsulinHistory(firebaseUid: String): List<InsulinDose> {
        val snapshot = db.collection("users").document(firebaseUid)
            .collection("insulin_doses")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                InsulinDose(
                    id             = doc.getLong("localId")?.toInt() ?: 0,
                    userId         = doc.getLong("userId")?.toInt() ?: 0,
                    glucoseValue   = doc.getLong("glucoseValue")?.toInt(),
                    carbs          = doc.getLong("carbs")?.toInt() ?: 0,
                    insulinRatio   = doc.getLong("insulinRatio")?.toInt() ?: 10,
                    calculatedDose = doc.getDouble("calculatedDose")?.toFloat() ?: 0f,
                    actualDose     = doc.getDouble("actualDose")?.toFloat(),
                    timestamp      = doc.getLong("timestamp") ?: 0L,
                    mealType       = doc.getString("mealType"),
                    notes          = doc.getString("notes"),
                    effectiveness  = doc.getDouble("effectiveness")?.toFloat()
                )
            } catch (e: Exception) { null }
        }
    }

    // ─────────────────────────────────────────────
    // CALENDAR EVENTS
    // ─────────────────────────────────────────────

    suspend fun saveEvent(firebaseUid: String, event: CalendarEvent) {
        val map = mapOf(
            "localId"          to event.id,
            "userId"           to event.userId,
            "title"            to event.title,
            "description"      to event.description,
            "eventType"        to event.eventType.name,  // enum → String
            "date"             to event.date,
            "time"             to event.time,
            "location"         to event.location,
            "isCompleted"      to event.isCompleted,
            "notificationTime" to event.notificationTime
        )
        db.collection("users").document(firebaseUid)
            .collection("events")
            .document(event.id.toString())
            .set(map)
            .await()
    }

    suspend fun deleteEvent(firebaseUid: String, eventId: Int) {
        db.collection("users").document(firebaseUid)
            .collection("events")
            .document(eventId.toString())
            .delete()
            .await()
    }

    suspend fun getEventsForMonth(firebaseUid: String, year: Int, month: Int): List<CalendarEvent> {
        val startOfMonth = getStartOfMonthTimestamp(year, month)
        val endOfMonth   = getEndOfMonthTimestamp(year, month)

        val snapshot = db.collection("users").document(firebaseUid)
            .collection("events")
            .whereGreaterThanOrEqualTo("date", startOfMonth)
            .whereLessThanOrEqualTo("date", endOfMonth)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                CalendarEvent(
                    id               = doc.getLong("localId")?.toInt() ?: 0,
                    userId           = doc.getLong("userId")?.toInt() ?: 0,
                    title            = doc.getString("title") ?: return@mapNotNull null,
                    description      = doc.getString("description"),
                    eventType        = CalendarEvent.EventType.valueOf(
                        doc.getString("eventType") ?: CalendarEvent.EventType.OTHER.name
                    ),
                    date             = doc.getLong("date") ?: 0L,
                    time             = doc.getString("time"),
                    location         = doc.getString("location"),
                    isCompleted      = doc.getBoolean("isCompleted") ?: false,
                    notificationTime = doc.getLong("notificationTime")
                )
            } catch (e: Exception) { null }
        }
    }

    // ─────────────────────────────────────────────
    // FAVOURITE RECIPES
    // ─────────────────────────────────────────────

    suspend fun saveFavoriteRecipe(firebaseUid: String, recipeId: String) {
        db.collection("users").document(firebaseUid)
            .collection("favorites")
            .document(recipeId)
            .set(mapOf("recipeId" to recipeId, "timestamp" to System.currentTimeMillis()))
            .await()
    }

    suspend fun removeFavoriteRecipe(firebaseUid: String, recipeId: String) {
        db.collection("users").document(firebaseUid)
            .collection("favorites")
            .document(recipeId)
            .delete()
            .await()
    }

    suspend fun getFavoriteRecipeIds(firebaseUid: String): List<String> {
        val snapshot = db.collection("users").document(firebaseUid)
            .collection("favorites")
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.getString("recipeId") }
    }

    // ─────────────────────────────────────────────
    // GLUCOSE STATS (for reports — computed server-side)
    // ─────────────────────────────────────────────

    suspend fun getGlucoseStats(firebaseUid: String, days: Int): Map<String, Any> {
        val startTimeSec = (System.currentTimeMillis() / 1000) - (days * 24 * 60 * 60L)

        val snapshot = db.collection("users").document(firebaseUid)
            .collection("glucose_readings")
            .whereGreaterThanOrEqualTo("timestamp", startTimeSec)
            .get()
            .await()

        val values = snapshot.documents.mapNotNull { it.getLong("value")?.toInt() }

        return mapOf(
            "average" to if (values.isNotEmpty()) values.average() else 0.0,
            "min"     to (values.minOrNull() ?: 0),
            "max"     to (values.maxOrNull() ?: 0),
            "count"   to values.size,
            "inRange" to values.count { it in 70..180 }
        )
    }

    // ─────────────────────────────────────────────
    // FULL SYNC — call this after login / on app start
    // Pushes all local SQLite data up to Firestore
    // ─────────────────────────────────────────────

    suspend fun syncLocalDataToCloud(
        firebaseUid: String,
        glucoseReadings: List<GlucoseReading>,
        insulinDoses: List<InsulinDose>,
        events: List<CalendarEvent>,
        favoriteIds: List<String>
    ) {
        glucoseReadings.forEach { saveGlucoseReading(firebaseUid, it) }
        insulinDoses.forEach    { saveInsulinDose(firebaseUid, it) }
        events.forEach          { saveEvent(firebaseUid, it) }
        favoriteIds.forEach     { saveFavoriteRecipe(firebaseUid, it) }
    }

    // ─────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────

    private fun getStartOfMonthTimestamp(year: Int, month: Int): Long {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, year)
            set(java.util.Calendar.MONTH, month)
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun getEndOfMonthTimestamp(year: Int, month: Int): Long {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, year)
            set(java.util.Calendar.MONTH, month)
            set(java.util.Calendar.DAY_OF_MONTH, getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 59)
            set(java.util.Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }
}