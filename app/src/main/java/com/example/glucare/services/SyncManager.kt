package com.example.glucare.services

import android.content.Context
import android.util.Log
import com.example.glucare.database.DatabaseHelper
import com.example.glucare.models.CalendarEvent
import com.example.glucare.models.GlucoseReading
import com.example.glucare.models.InsulinDose
import com.example.glucare.models.User
import com.example.glucare.utils.SessionManager
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * SyncManager — the glue between local SQLite and Firestore.
 *
 * USAGE:
 *
 * 1. On Signup  → call registerAndSync(email, password, user)
 * 2. On Login   → call loginAndSync(email, password)
 * 3. On Logout  → call logout()
 * 4. Any time you save local data (glucose, insulin, event) →
 *      call the matching push*() function so Firestore stays up-to-date.
 * 5. On fresh install / reinstall →
 *      loginAndSync() automatically pulls everything back from Firestore
 *      and re-inserts it into the local SQLite database.
 */
class SyncManager(
    private val context: Context,
    private val db: DatabaseHelper,
    private val session: SessionManager,
    private val firestoreService: FirestoreService = FirestoreService()
) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val TAG = "SyncManager"

    // ─────────────────────────────────────────────
    // AUTH
    // ─────────────────────────────────────────────

    /**
     * Creates a Firebase Auth account, saves the user locally, then pushes
     * the empty profile to Firestore so the document exists immediately.
     * Returns the Firebase UID or null on failure.
     */
    suspend fun registerAndSync(
        email: String,
        password: String,
        user: User
    ): String? = withContext(Dispatchers.IO) {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await2()
            val uid = result.user?.uid ?: return@withContext null

            // Save Firebase UID to session
            session.saveFirebaseUid(uid)

            // Push the new user profile to Firestore
            firestoreService.saveUser(uid, user)

            Log.d(TAG, "registerAndSync success, uid=$uid")
            uid
        } catch (e: Exception) {
            Log.e(TAG, "registerAndSync failed: ${e.message}")
            null
        }
    }

    /**
     * Signs in with Firebase Auth, then pulls ALL data from Firestore
     * back into local SQLite.  This is the key restore-after-reinstall path.
     * Returns the local SQLite userId (Int) or -1 on failure.
     */
    suspend fun loginAndSync(email: String, password: String): Int = withContext(Dispatchers.IO) {
        try {
            // 1. Sign in to Firebase
            val result = auth.signInWithEmailAndPassword(email, password).await2()
            val uid = result.user?.uid ?: return@withContext -1
            session.saveFirebaseUid(uid)

            // 2. Check if user already exists locally (not a reinstall case)
            val existingUser = db.getUserByEmail(email)
            if (existingUser != null) {
                Log.d(TAG, "User already in local DB, no restore needed")
                return@withContext existingUser.id
            }

            // 3. Restore from Firestore → SQLite
            Log.d(TAG, "Restoring data from Firestore for uid=$uid")
            restoreFromFirestore(uid, email, password)
        } catch (e: Exception) {
            Log.e(TAG, "loginAndSync failed: ${e.message}")
            -1
        }
    }

    fun logout() {
        auth.signOut()
        session.logout()
    }

    fun currentFirebaseUser(): FirebaseUser? = auth.currentUser

    // ─────────────────────────────────────────────
    // PUSH (local → cloud) — call after every local write
    // ─────────────────────────────────────────────

    suspend fun pushGlucoseReading(reading: GlucoseReading) {
        val uid = session.getFirebaseUid() ?: return
        try { firestoreService.saveGlucoseReading(uid, reading) }
        catch (e: Exception) { Log.e(TAG, "pushGlucoseReading: ${e.message}") }
    }

    suspend fun pushInsulinDose(dose: InsulinDose) {
        val uid = session.getFirebaseUid() ?: return
        try { firestoreService.saveInsulinDose(uid, dose) }
        catch (e: Exception) { Log.e(TAG, "pushInsulinDose: ${e.message}") }
    }

    suspend fun pushEvent(event: CalendarEvent) {
        val uid = session.getFirebaseUid() ?: return
        try { firestoreService.saveEvent(uid, event) }
        catch (e: Exception) { Log.e(TAG, "pushEvent: ${e.message}") }
    }

    suspend fun pushDeleteEvent(eventId: Int) {
        val uid = session.getFirebaseUid() ?: return
        try { firestoreService.deleteEvent(uid, eventId) }
        catch (e: Exception) { Log.e(TAG, "pushDeleteEvent: ${e.message}") }
    }

    suspend fun pushFavorite(recipeId: String, add: Boolean) {
        val uid = session.getFirebaseUid() ?: return
        try {
            if (add) firestoreService.saveFavoriteRecipe(uid, recipeId)
            else firestoreService.removeFavoriteRecipe(uid, recipeId)
        } catch (e: Exception) { Log.e(TAG, "pushFavorite: ${e.message}") }
    }

    suspend fun pushUserProfile(user: User) {
        val uid = session.getFirebaseUid() ?: return
        try { firestoreService.saveUser(uid, user) }
        catch (e: Exception) { Log.e(TAG, "pushUserProfile: ${e.message}") }
    }

    // ─────────────────────────────────────────────
    // FULL SYNC UP — call once to upload everything
    // (e.g. first time Firebase is connected to an existing account)
    // ─────────────────────────────────────────────

    suspend fun fullSyncUp(localUserId: Int) {
        val uid = session.getFirebaseUid() ?: return
        try {
            val glucose   = db.getGlucoseReadings(localUserId)
            val insulin   = db.getInsulinHistory(localUserId)
            val events    = db.getEventDates(localUserId)
                .flatMap { date -> db.getEventsForDay(localUserId, date) }
                .distinctBy { it.id }
            val favorites = db.getFavoriteRecipeIds(localUserId)

            firestoreService.syncLocalDataToCloud(uid, glucose, insulin, events, favorites)
            Log.d(TAG, "fullSyncUp complete")
        } catch (e: Exception) {
            Log.e(TAG, "fullSyncUp failed: ${e.message}")
        }
    }

    // ─────────────────────────────────────────────
    // RESTORE (cloud → local SQLite)
    // ─────────────────────────────────────────────

    private suspend fun restoreFromFirestore(uid: String, email: String, password: String): Int {
        // 1. Restore user profile
        val cloudUser = firestoreService.getUser(uid)
            ?: return -1

        val userWithPassword = cloudUser.copy(
            email    = email,
            password = password   // local DB needs the password for login checks
        )

        // Check again (race condition guard)
        if (db.checkUserExists(email)) {
            return db.getUserByEmail(email)?.id ?: -1
        }

        val localUserId = db.insertUser(userWithPassword).toInt()
        if (localUserId <= 0) return -1

        // Save to session immediately
        session.saveUser(localUserId, userWithPassword.fullName, email)
        session.saveFirebaseUid(uid)

        // Restore health info if available
        if (cloudUser.age != null) {
            db.updateUserHealthInfo(
                userId       = localUserId,
                age          = cloudUser.age,
                gender       = cloudUser.gender,
                height       = cloudUser.height,
                weight       = cloudUser.weight,
                diabetesType = cloudUser.diabetesType,
                avgBloodSugar = cloudUser.avgBloodSugar
            )
            session.setHealthInfoCompleted(true)
        }

        // 2. Restore glucose readings
        val glucoseReadings = firestoreService.getGlucoseReadings(uid)
        glucoseReadings.forEach { reading ->
            db.insertGlucoseReading(reading.copy(userId = localUserId))
        }
        Log.d(TAG, "Restored ${glucoseReadings.size} glucose readings")

        // 3. Restore insulin doses
        val insulinDoses = firestoreService.getInsulinHistory(uid)
        insulinDoses.forEach { dose ->
            db.insertInsulinDose(dose.copy(userId = localUserId))
        }
        Log.d(TAG, "Restored ${insulinDoses.size} insulin doses")

        // 4. Restore events (pull last 3 months)
        val cal = Calendar.getInstance()
        val currentYear  = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH)
        for (offset in 0..2) {
            val month = (currentMonth - offset + 12) % 12
            val year  = if (currentMonth - offset < 0) currentYear - 1 else currentYear
            val events = firestoreService.getEventsForMonth(uid, year, month)
            events.forEach { event ->
                db.insertEvent(event.copy(userId = localUserId))
            }
            Log.d(TAG, "Restored ${events.size} events for $year/$month")
        }

        // 5. Restore favorites
        val favIds = firestoreService.getFavoriteRecipeIds(uid)
        favIds.forEach { recipeId ->
            db.addFavorite(localUserId, recipeId)
        }
        Log.d(TAG, "Restored ${favIds.size} favorites")

        Log.d(TAG, "Full restore complete for localUserId=$localUserId")
        return localUserId
    }

    // ─────────────────────────────────────────────
    // UTILITY — bridge Firebase Task<T> to suspend
    // ─────────────────────────────────────────────

    // Firebase Tasks already have an await() extension from
    // kotlinx-coroutines-play-services, but naming it await2() here
    // avoids import ambiguity in some IDE setups.
    private suspend fun <T> Task<T>.await2(): T =
       this.await()
}