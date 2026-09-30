package com.example.glucare.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.glucare.models.CalendarEvent
import com.example.glucare.models.GlucoseReading
import com.example.glucare.models.InsulinDose
import com.example.glucare.models.ScannedFoodHistory
import com.example.glucare.models.User
import java.util.*
import com.example.glucare.models.MealPlanItem

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Glucare.db"
        private const val DATABASE_VERSION = 1

        // User table
        private const val TABLE_USERS = "users"
        private const val COLUMN_NOTIFICATION_TIME = "notification_time"

        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_FULL_NAME = "full_name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_AGE = "age"
        private const val COLUMN_GENDER = "gender"
        private const val COLUMN_HEIGHT = "height"
        private const val COLUMN_WEIGHT = "weight"
        private const val COLUMN_DIABETES_TYPE = "diabetes_type"
        private const val COLUMN_AVG_BLOOD_SUGAR = "avg_blood_sugar"
        private const val COLUMN_INSULIN_RATIO = "insulin_ratio"
        private const val COLUMN_CREATED_AT = "created_at"

        // Glucose readings table
        private const val TABLE_GLUCOSE = "glucose_readings"
        private const val COLUMN_GLUCOSE_ID = "id"
        private const val COLUMN_USER_ID_FK = "user_id"
        private const val COLUMN_GLUCOSE_VALUE = "value"
        private const val COLUMN_READING_TYPE = "reading_type"
        private const val COLUMN_READING_TIMESTAMP = "timestamp"
        private const val COLUMN_NOTES = "notes"

        // Insulin doses table
        private const val TABLE_INSULIN = "insulin_doses"
        private const val COLUMN_INSULIN_ID = "id"
        private const val COLUMN_INSULIN_USER_ID = "user_id"
        private const val COLUMN_GLUCOSE_VALUE_IN = "glucose_value"
        private const val COLUMN_CARBS = "carbs"
        private const val COLUMN_RATIO = "ratio"
        private const val COLUMN_CALCULATED_DOSE = "calculated_dose"
        private const val COLUMN_ACTUAL_DOSE = "actual_dose"
        private const val COLUMN_INSULIN_TIMESTAMP = "timestamp"

        // Calendar events table
        private const val TABLE_EVENTS = "calendar_events"
        private const val COLUMN_EVENT_ID = "id"
        private const val COLUMN_EVENT_USER_ID = "user_id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_EVENT_TYPE = "event_type"
        private const val COLUMN_EVENT_DATE = "event_date"
        private const val COLUMN_EVENT_TIME = "event_time"
        private const val COLUMN_LOCATION = "location"
        private const val COLUMN_IS_COMPLETED = "is_completed"

        // Scanned food table
        private const val TABLE_SCANNED_FOOD = "scanned_food"
        private const val COLUMN_SCAN_ID = "id"
        private const val COLUMN_SCAN_USER_ID = "user_id"
        private const val COLUMN_FOOD_NAME = "food_name"
        private const val COLUMN_BARCODE = "barcode"
        private const val COLUMN_CALORIES = "calories"
        private const val COLUMN_SUGAR = "sugar"
        private const val COLUMN_CARBS_SCAN = "carbs"
        private const val COLUMN_SCAN_METHOD = "scan_method"
        private const val COLUMN_SCAN_TIMESTAMP = "timestamp"
        private const val TABLE_FAVORITES = "favorite_recipes"
        private const val COLUMN_FAVORITE_ID = "id"
        private const val COLUMN_FAVORITE_USER_ID = "user_id"
        private const val COLUMN_FAVORITE_RECIPE_ID = "recipe_id"
        private const val COLUMN_FAVORITE_DATE_ADDED = "date_added"
        private const val COLUMN_FAVORITE_NOTES = "notes"

        // Meal plan table
        private const val TABLE_MEAL_PLAN = "meal_plan"
        private const val COLUMN_MEAL_PLAN_ID = "id"
        private const val COLUMN_MEAL_PLAN_USER_ID = "user_id"
        private const val COLUMN_MEAL_PLAN_RECIPE_ID = "recipe_id"
        private const val COLUMN_MEAL_PLAN_DATE = "plan_date"  // YYYY-MM-DD
        private const val COLUMN_MEAL_PLAN_MEAL_TYPE = "meal_type"  // breakfast, lunch, dinner, snack
        private const val COLUMN_MEAL_PLAN_NOTES = "notes"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUserTable = """
        CREATE TABLE $TABLE_USERS (
            $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_FULL_NAME TEXT NOT NULL,
            $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
            $COLUMN_PHONE TEXT,
            $COLUMN_PASSWORD TEXT NOT NULL,
            $COLUMN_AGE INTEGER,
            $COLUMN_GENDER TEXT,
            $COLUMN_HEIGHT REAL,
            $COLUMN_WEIGHT REAL,
            $COLUMN_DIABETES_TYPE TEXT,
            $COLUMN_AVG_BLOOD_SUGAR REAL,
            $COLUMN_INSULIN_RATIO INTEGER DEFAULT 10,
            $COLUMN_CREATED_AT INTEGER DEFAULT (strftime('%s','now'))
        )
    """.trimIndent()
        db.execSQL(createUserTable)

        // 2. Create glucose readings table
        val createGlucoseTable = """
        CREATE TABLE $TABLE_GLUCOSE (
            $COLUMN_GLUCOSE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_USER_ID_FK INTEGER NOT NULL,
            $COLUMN_GLUCOSE_VALUE INTEGER NOT NULL,
            $COLUMN_READING_TYPE TEXT,
            $COLUMN_READING_TIMESTAMP INTEGER DEFAULT (strftime('%s','now')),
            $COLUMN_NOTES TEXT,
            FOREIGN KEY($COLUMN_USER_ID_FK) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
        )
    """.trimIndent()
        db.execSQL(createGlucoseTable)

        // 3. Create insulin doses table
        val createInsulinTable = """
        CREATE TABLE $TABLE_INSULIN (
            $COLUMN_INSULIN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_INSULIN_USER_ID INTEGER NOT NULL,
            $COLUMN_GLUCOSE_VALUE_IN INTEGER,
            $COLUMN_CARBS INTEGER NOT NULL,
            $COLUMN_RATIO INTEGER NOT NULL,
            $COLUMN_CALCULATED_DOSE REAL NOT NULL,
            $COLUMN_ACTUAL_DOSE REAL,
            $COLUMN_INSULIN_TIMESTAMP INTEGER DEFAULT (strftime('%s','now')),
            FOREIGN KEY($COLUMN_INSULIN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
        )
    """.trimIndent()
        db.execSQL(createInsulinTable)

        // 4. Create calendar events table
        val createEventsTable = """
        CREATE TABLE $TABLE_EVENTS (
            $COLUMN_EVENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_EVENT_USER_ID INTEGER NOT NULL,
            $COLUMN_TITLE TEXT NOT NULL,
            $COLUMN_DESCRIPTION TEXT,
            $COLUMN_EVENT_TYPE TEXT NOT NULL,
            $COLUMN_EVENT_DATE INTEGER NOT NULL,
            $COLUMN_EVENT_TIME TEXT,
            $COLUMN_LOCATION TEXT,
            $COLUMN_IS_COMPLETED INTEGER DEFAULT 0,
            $COLUMN_NOTIFICATION_TIME INTEGER, 
            FOREIGN KEY($COLUMN_EVENT_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
        )
    """.trimIndent()
        db.execSQL(createEventsTable)

        // 5. Create scanned food table
        val createScannedFoodTable = """
        CREATE TABLE $TABLE_SCANNED_FOOD (
            $COLUMN_SCAN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_SCAN_USER_ID INTEGER NOT NULL,
            $COLUMN_FOOD_NAME TEXT NOT NULL,
            $COLUMN_BARCODE TEXT,
            $COLUMN_CALORIES REAL,
            $COLUMN_SUGAR REAL,
            $COLUMN_CARBS_SCAN REAL,
            $COLUMN_SCAN_METHOD TEXT,
            $COLUMN_SCAN_TIMESTAMP INTEGER DEFAULT (strftime('%s','now')),
            FOREIGN KEY($COLUMN_SCAN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
        )
    """.trimIndent()
        db.execSQL(createScannedFoodTable)

        // 6. Create favorites table
        val createFavoritesTable = """
        CREATE TABLE $TABLE_FAVORITES (
            $COLUMN_FAVORITE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_FAVORITE_USER_ID INTEGER NOT NULL,
            $COLUMN_FAVORITE_RECIPE_ID TEXT NOT NULL,
            $COLUMN_FAVORITE_DATE_ADDED INTEGER DEFAULT (strftime('%s','now')),
            $COLUMN_FAVORITE_NOTES TEXT,
            FOREIGN KEY($COLUMN_FAVORITE_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE,
            UNIQUE($COLUMN_FAVORITE_USER_ID, $COLUMN_FAVORITE_RECIPE_ID)
        )
    """.trimIndent()
        db.execSQL(createFavoritesTable)

        // 7. Create meal plan table
        val createMealPlanTable = """
        CREATE TABLE $TABLE_MEAL_PLAN (
            $COLUMN_MEAL_PLAN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_MEAL_PLAN_USER_ID INTEGER NOT NULL,
            $COLUMN_MEAL_PLAN_RECIPE_ID TEXT NOT NULL,
            $COLUMN_MEAL_PLAN_DATE TEXT NOT NULL,
            $COLUMN_MEAL_PLAN_MEAL_TYPE TEXT NOT NULL,
            $COLUMN_MEAL_PLAN_NOTES TEXT,
            FOREIGN KEY($COLUMN_MEAL_PLAN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
        )
    """.trimIndent()
        db.execSQL(createMealPlanTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SCANNED_FOOD")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EVENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INSULIN")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GLUCOSE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // User methods
    fun insertUser(user: User): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FULL_NAME, user.fullName)
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_PHONE, user.phone)
            put(COLUMN_PASSWORD, user.password)
            put(COLUMN_INSULIN_RATIO, 10) // Default ratio
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun addFavorite(userId: Int, recipeId: String, notes: String = ""): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FAVORITE_USER_ID, userId)
            put(COLUMN_FAVORITE_RECIPE_ID, recipeId)
            put(COLUMN_FAVORITE_NOTES, notes)
        }
        return db.insert(TABLE_FAVORITES, null, values)
    }

    fun removeFavorite(userId: Int, recipeId: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_FAVORITES,
            "$COLUMN_FAVORITE_USER_ID = ? AND $COLUMN_FAVORITE_RECIPE_ID = ?",
            arrayOf(userId.toString(), recipeId))
    }

    fun isFavorite(userId: Int, recipeId: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_FAVORITES,
            arrayOf(COLUMN_FAVORITE_ID),
            "$COLUMN_FAVORITE_USER_ID = ? AND $COLUMN_FAVORITE_RECIPE_ID = ?",
            arrayOf(userId.toString(), recipeId),
            null, null, null
        )
        return cursor.use { it.count > 0 }
    }

    fun getFavoriteRecipeIds(userId: Int): List<String> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_FAVORITES,
            arrayOf(COLUMN_FAVORITE_RECIPE_ID),
            "$COLUMN_FAVORITE_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, "$COLUMN_FAVORITE_DATE_ADDED DESC"
        )

        val favorites = mutableListOf<String>()
        cursor.use {
            while (it.moveToNext()) {
                favorites.add(it.getString(it.getColumnIndexOrThrow(COLUMN_FAVORITE_RECIPE_ID)))
            }
        }
        return favorites
    }
    fun addToMealPlan(userId: Int, recipeId: String, date: String, mealType: String, notes: String = ""): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MEAL_PLAN_USER_ID, userId)
            put(COLUMN_MEAL_PLAN_RECIPE_ID, recipeId)
            put(COLUMN_MEAL_PLAN_DATE, date)
            put(COLUMN_MEAL_PLAN_MEAL_TYPE, mealType)
            put(COLUMN_MEAL_PLAN_NOTES, notes)
        }
        return db.insert(TABLE_MEAL_PLAN, null, values)
    }

    fun getMealPlanForDate(userId: Int, date: String): List<MealPlanItem> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_MEAL_PLAN,
            null,
            "$COLUMN_MEAL_PLAN_USER_ID = ? AND $COLUMN_MEAL_PLAN_DATE = ?",
            arrayOf(userId.toString(), date),
            null, null, "$COLUMN_MEAL_PLAN_MEAL_TYPE ASC"
        )

        val items = mutableListOf<MealPlanItem>()
        cursor.use {
            while (it.moveToNext()) {
                items.add(MealPlanItem(
                    id = it.getInt(it.getColumnIndexOrThrow(COLUMN_MEAL_PLAN_ID)),
                    userId = it.getInt(it.getColumnIndexOrThrow(COLUMN_MEAL_PLAN_USER_ID)),
                    recipeId = it.getString(it.getColumnIndexOrThrow(COLUMN_MEAL_PLAN_RECIPE_ID)),
                    date = it.getString(it.getColumnIndexOrThrow(COLUMN_MEAL_PLAN_DATE)),
                    mealType = it.getString(it.getColumnIndexOrThrow(COLUMN_MEAL_PLAN_MEAL_TYPE)),
                    notes = it.getString(it.getColumnIndexOrThrow(COLUMN_MEAL_PLAN_NOTES))
                ))
            }
        }
        return items
    }
    fun getUser(email: String, password: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(email, password),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                extractUserFromCursor(it)
            } else null
        }
    }

    fun getUserById(userId: Int): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                extractUserFromCursor(it)
            } else null
        }
    }

    fun checkUserExists(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID),
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )
        return cursor.use { it.count > 0 }
    }

    fun updateUserHealthInfo(
        userId: Int,
        age: Int?,
        gender: String?,
        height: Float?,
        weight: Float?,
        diabetesType: String?,
        avgBloodSugar: Float?
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            age?.let { put(COLUMN_AGE, it) }
            gender?.let { put(COLUMN_GENDER, it) }
            height?.let { put(COLUMN_HEIGHT, it) }
            weight?.let { put(COLUMN_WEIGHT, it) }
            diabetesType?.let { put(COLUMN_DIABETES_TYPE, it) }
            avgBloodSugar?.let { put(COLUMN_AVG_BLOOD_SUGAR, it) }
        }

        val rows = db.update(TABLE_USERS, values, "$COLUMN_USER_ID = ?", arrayOf(userId.toString()))
        return rows > 0
    }

    private fun extractUserFromCursor(cursor: Cursor): User {
        return User(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
            fullName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)),
            email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
            phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
            password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
            age = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AGE)),
            gender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER)),
            height = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_HEIGHT)),
            weight = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT)),
            diabetesType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIABETES_TYPE)),
            avgBloodSugar = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_AVG_BLOOD_SUGAR)),
            insulinRatio = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_INSULIN_RATIO))
        )
    }

    // Glucose methods
    fun insertGlucoseReading(reading: GlucoseReading): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID_FK, reading.userId)
            put(COLUMN_GLUCOSE_VALUE, reading.value)
            put(COLUMN_READING_TYPE, reading.readingType.name)
            put(COLUMN_READING_TIMESTAMP, reading.timestamp) // FIX: persist timestamp
            put(COLUMN_NOTES, reading.notes)
        }
        return db.insert(TABLE_GLUCOSE, null, values)
    }

    fun getLatestGlucoseReading(userId: Int): GlucoseReading? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_GLUCOSE,
            null,
            "$COLUMN_USER_ID_FK = ?",
            arrayOf(userId.toString()),
            null, null,
            "$COLUMN_READING_TIMESTAMP DESC",
            "1"
        )

        return cursor.use {
            if (it.moveToFirst()) {
                GlucoseReading(
                    id = it.getInt(it.getColumnIndexOrThrow(COLUMN_GLUCOSE_ID)),
                    userId = it.getInt(it.getColumnIndexOrThrow(COLUMN_USER_ID_FK)),
                    value = it.getInt(it.getColumnIndexOrThrow(COLUMN_GLUCOSE_VALUE)),
                    readingType = GlucoseReading.ReadingType.valueOf(
                        it.getString(
                            it.getColumnIndexOrThrow(
                                COLUMN_READING_TYPE
                            )
                        )
                    ),
                    timestamp = it.getLong(it.getColumnIndexOrThrow(COLUMN_READING_TIMESTAMP)),
                    notes = it.getString(it.getColumnIndexOrThrow(COLUMN_NOTES))
                )
            } else null
        }
    }

    fun getGlucoseReadings(userId: Int): List<GlucoseReading> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_GLUCOSE,
            null,
            "$COLUMN_USER_ID_FK = ?",
            arrayOf(userId.toString()),
            null, null,
            "$COLUMN_READING_TIMESTAMP DESC"
        )
        val readings = mutableListOf<GlucoseReading>()
        cursor.use {
            while (it.moveToNext()) {
                readings.add(
                    GlucoseReading(
                        id = it.getInt(it.getColumnIndexOrThrow(COLUMN_GLUCOSE_ID)),
                        userId = it.getInt(it.getColumnIndexOrThrow(COLUMN_USER_ID_FK)),
                        value = it.getInt(it.getColumnIndexOrThrow(COLUMN_GLUCOSE_VALUE)),
                        readingType = GlucoseReading.ReadingType.valueOf(
                            it.getString(
                                it.getColumnIndexOrThrow(
                                    COLUMN_READING_TYPE
                                )
                            )
                        ),
                        timestamp = it.getLong(it.getColumnIndexOrThrow(COLUMN_READING_TIMESTAMP)),
                        notes = it.getString(it.getColumnIndexOrThrow(COLUMN_NOTES))
                    )
                )
            }
        }
        return readings
    }

    fun getAverageGlucose(userId: Int): Float {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT AVG($COLUMN_GLUCOSE_VALUE) FROM $TABLE_GLUCOSE WHERE $COLUMN_USER_ID_FK = ?",
            arrayOf(userId.toString())
        )
        return cursor.use {
            if (it.moveToFirst()) it.getFloat(0) else 0f
        }
    }

    // Insulin methods
    fun insertInsulinDose(dose: InsulinDose): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_INSULIN_USER_ID, dose.userId)
            put(COLUMN_GLUCOSE_VALUE_IN, dose.glucoseValue)
            put(COLUMN_CARBS, dose.carbs)
            put(COLUMN_RATIO, dose.insulinRatio)
            put(COLUMN_CALCULATED_DOSE, dose.calculatedDose)
            put(COLUMN_ACTUAL_DOSE, dose.actualDose)
            put(COLUMN_INSULIN_TIMESTAMP, dose.timestamp) // FIX: persist timestamp
        }
        return db.insert(TABLE_INSULIN, null, values)
    }

    fun getInsulinHistory(userId: Int): List<InsulinDose> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_INSULIN,
            null,
            "$COLUMN_INSULIN_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null,
            "$COLUMN_INSULIN_TIMESTAMP DESC"
        )
        val history = mutableListOf<InsulinDose>()
        cursor.use {
            while (it.moveToNext()) {
                history.add(
                    InsulinDose(
                        id = it.getInt(it.getColumnIndexOrThrow(COLUMN_INSULIN_ID)),
                        userId = it.getInt(it.getColumnIndexOrThrow(COLUMN_INSULIN_USER_ID)),
                        // FIX: glucoseValue is nullable — use null if the column is NULL
                        glucoseValue = it.getColumnIndexOrThrow(COLUMN_GLUCOSE_VALUE_IN)
                            .takeIf { col -> !it.isNull(col) }
                            ?.let { col -> it.getInt(col) },
                        carbs = it.getInt(it.getColumnIndexOrThrow(COLUMN_CARBS)),
                        insulinRatio = it.getInt(it.getColumnIndexOrThrow(COLUMN_RATIO)),
                        calculatedDose = it.getFloat(it.getColumnIndexOrThrow(COLUMN_CALCULATED_DOSE)),
                        // FIX: actualDose is nullable too
                        actualDose = it.getColumnIndexOrThrow(COLUMN_ACTUAL_DOSE)
                            .takeIf { col -> !it.isNull(col) }
                            ?.let { col -> it.getFloat(col) },
                        timestamp = it.getLong(it.getColumnIndexOrThrow(COLUMN_INSULIN_TIMESTAMP))
                    )
                )
            }
        }
        return history
    }

    // Calendar methods
    fun insertEvent(event: CalendarEvent): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EVENT_USER_ID, event.userId)
            put(COLUMN_TITLE, event.title)
            put(COLUMN_DESCRIPTION, event.description)
            put(COLUMN_EVENT_TYPE, event.eventType.name)
            put(COLUMN_EVENT_DATE, event.date)
            put(COLUMN_EVENT_TIME, event.time)
            put(COLUMN_LOCATION, event.location)
            put(COLUMN_IS_COMPLETED, if (event.isCompleted) 1 else 0)
            event.notificationTime?.let { put(COLUMN_NOTIFICATION_TIME, it) }
        }
        return db.insert(TABLE_EVENTS, null, values)
    }

    fun deleteEvent(eventId: Int): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_EVENTS, "$COLUMN_EVENT_ID = ?", arrayOf(eventId.toString())) > 0
    }

    fun getEventsForDay(userId: Int, date: Long): List<CalendarEvent> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_EVENTS,
            null,
            "$COLUMN_EVENT_USER_ID = ? AND $COLUMN_EVENT_DATE = ?",
            arrayOf(userId.toString(), date.toString()),
            null, null,
            "$COLUMN_EVENT_TIME ASC"
        )

        val events = mutableListOf<CalendarEvent>()
        cursor.use {
            while (it.moveToNext()) {
                events.add(
                    CalendarEvent(
                        id = it.getInt(it.getColumnIndexOrThrow(COLUMN_EVENT_ID)),
                        userId = it.getInt(it.getColumnIndexOrThrow(COLUMN_EVENT_USER_ID)),
                        title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE)),
                        description = it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        eventType = CalendarEvent.EventType.valueOf(
                            it.getString(it.getColumnIndexOrThrow(COLUMN_EVENT_TYPE))
                        ),
                        date = it.getLong(it.getColumnIndexOrThrow(COLUMN_EVENT_DATE)),
                        time = it.getString(it.getColumnIndexOrThrow(COLUMN_EVENT_TIME)),
                        location = it.getString(it.getColumnIndexOrThrow(COLUMN_LOCATION)),
                        isCompleted = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)) == 1,
                        notificationTime = if (it.getColumnIndex(COLUMN_NOTIFICATION_TIME) >= 0)
                            it.getLong(it.getColumnIndexOrThrow(COLUMN_NOTIFICATION_TIME)) else null
                    )
                )
            }
        }
        return events
    }
    fun getEventDates(userId: Int): List<Long> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_EVENTS,
            arrayOf(COLUMN_EVENT_DATE),
            "$COLUMN_EVENT_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )
        val dates = mutableListOf<Long>()
        cursor.use {
            while (it.moveToNext()) {
                dates.add(it.getLong(it.getColumnIndexOrThrow(COLUMN_EVENT_DATE)))
            }
        }
        return dates
    }

    // Scanned food methods
    fun insertScannedFood(scan: ScannedFoodHistory): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SCAN_USER_ID, scan.userId)
            put(COLUMN_FOOD_NAME, scan.foodName)
            put(COLUMN_BARCODE, scan.barcode)
            put(COLUMN_CALORIES, scan.calories)
            put(COLUMN_SUGAR, scan.sugar)
            put(COLUMN_CARBS_SCAN, scan.carbs)
            put(COLUMN_SCAN_METHOD, scan.scanMethod.name)
        }
        return db.insert(TABLE_SCANNED_FOOD, null, values)
    }
    // Add to DatabaseHelper.kt

    fun getEventDatesForMonth(userId: Int, year: Int, month: Int): List<Long> {
        val db = readableDatabase

        // Get start and end of month timestamps
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfMonth = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endOfMonth = cal.timeInMillis

        val cursor = db.query(
            TABLE_EVENTS,
            arrayOf(COLUMN_EVENT_DATE),
            "$COLUMN_EVENT_USER_ID = ? AND $COLUMN_EVENT_DATE BETWEEN ? AND ?",
            arrayOf(userId.toString(), startOfMonth.toString(), endOfMonth.toString()),
            null, null, null
        )

        val dates = mutableListOf<Long>()
        cursor.use {
            while (it.moveToNext()) {
                dates.add(it.getLong(it.getColumnIndexOrThrow(COLUMN_EVENT_DATE)))
            }
        }
        return dates
    }

    fun getGlucoseDatesForMonth(userId: Int, year: Int, month: Int): List<Long> {
        val db = readableDatabase

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfMonth = cal.timeInMillis / 1000

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endOfMonth = cal.timeInMillis / 1000

        val cursor = db.query(
            TABLE_GLUCOSE,
            arrayOf(COLUMN_READING_TIMESTAMP),
            "$COLUMN_USER_ID_FK = ? AND $COLUMN_READING_TIMESTAMP BETWEEN ? AND ?",
            arrayOf(userId.toString(), startOfMonth.toString(), endOfMonth.toString()),
            null, null, null
        )

        val dates = mutableListOf<Long>()
        cursor.use {
            while (it.moveToNext()) {
                val timestampSec = it.getLong(it.getColumnIndexOrThrow(COLUMN_READING_TIMESTAMP))
                dates.add(timestampSec * 1000)
            }
        }
        return dates
    }

    fun updateEvent(event: CalendarEvent): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, event.title)
            put(COLUMN_DESCRIPTION, event.description)
            put(COLUMN_EVENT_TYPE, event.eventType.name)
            put(COLUMN_EVENT_TIME, event.time)
            put(COLUMN_LOCATION, event.location)
            put(COLUMN_IS_COMPLETED, if (event.isCompleted) 1 else 0)
            event.notificationTime?.let { put(COLUMN_NOTIFICATION_TIME, it) }
        }
        return db.update(TABLE_EVENTS, values, "$COLUMN_EVENT_ID = ?", arrayOf(event.id.toString()))
    }


    fun getScannedHistory(userId: Int): List<ScannedFoodHistory> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SCANNED_FOOD,
            null,
            "$COLUMN_SCAN_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null,
            "$COLUMN_SCAN_TIMESTAMP DESC"
        )

        val history = mutableListOf<ScannedFoodHistory>()
        cursor.use {
            while (it.moveToNext()) {
                history.add(
                    ScannedFoodHistory(
                        id = it.getInt(it.getColumnIndexOrThrow(COLUMN_SCAN_ID)),
                        userId = it.getInt(it.getColumnIndexOrThrow(COLUMN_SCAN_USER_ID)),
                        foodName = it.getString(it.getColumnIndexOrThrow(COLUMN_FOOD_NAME)),
                        barcode = it.getString(it.getColumnIndexOrThrow(COLUMN_BARCODE)),
                        calories = it.getFloat(it.getColumnIndexOrThrow(COLUMN_CALORIES)),
                        sugar = it.getFloat(it.getColumnIndexOrThrow(COLUMN_SUGAR)),
                        carbs = it.getFloat(it.getColumnIndexOrThrow(COLUMN_CARBS_SCAN)),
                        scanMethod = ScannedFoodHistory.ScanMethod.valueOf(
                            it.getString(
                                it.getColumnIndexOrThrow(
                                    COLUMN_SCAN_METHOD
                                )
                            )
                        ),
                        timestamp = it.getLong(it.getColumnIndexOrThrow(COLUMN_SCAN_TIMESTAMP))
                    )
                )
            }
        }
        return history
    }
    fun verifyUsernameAndEmail(username: String, email: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID),
            "$COLUMN_FULL_NAME = ? AND $COLUMN_EMAIL = ?",
            arrayOf(username, email),
            null, null, null
        )
        return cursor.use { it.count > 0 }
    }

    fun resetPassword(username: String, email: String, newPassword: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PASSWORD, newPassword)
        }
        val rows = db.update(
            TABLE_USERS,
            values,
            "$COLUMN_FULL_NAME = ? AND $COLUMN_EMAIL = ?",
            arrayOf(username, email)
        )
        return rows > 0
    }
    fun getUserByEmail(email: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                extractUserFromCursor(it)
            } else null
        }
    }

}