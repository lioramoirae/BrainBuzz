package com.example.brainbuzz

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.security.MessageDigest
import java.util.*

class UserDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "brainbuzz_users.db"
        private const val DATABASE_VERSION = 2 // Increment version

        // Users table
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD_HASH = "password_hash"
        private const val COLUMN_USER_TYPE = "user_type"
        private const val COLUMN_CREATED_AT = "created_at"
        private const val COLUMN_TOTAL_SCORE = "total_score"
        private const val COLUMN_QUIZZES_COMPLETED = "quizzes_completed"
        private const val COLUMN_AVERAGE_SCORE = "average_score"
        private const val COLUMN_STREAK_DAYS = "streak_days"
        private const val COLUMN_LAST_LOGIN = "last_login_date"
        private const val COLUMN_ACHIEVEMENTS = "achievements"
        private const val COLUMN_IS_GUEST = "is_guest"

        // Quiz results table
        private const val TABLE_QUIZ_RESULTS = "quiz_results"
        private const val COLUMN_QUIZ_ID = "quiz_id"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_SCORE = "score"
        private const val COLUMN_MAX_SCORE = "max_score"
        private const val COLUMN_TIME_TAKEN = "time_taken"
        private const val COLUMN_DATE_TAKEN = "date_taken"
    }

    private val gson = Gson()

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EMAIL TEXT UNIQUE,
                $COLUMN_USERNAME TEXT,
                $COLUMN_PASSWORD_HASH TEXT,
                $COLUMN_USER_TYPE TEXT,
                $COLUMN_CREATED_AT INTEGER,
                $COLUMN_TOTAL_SCORE INTEGER DEFAULT 0,
                $COLUMN_QUIZZES_COMPLETED INTEGER DEFAULT 0,
                $COLUMN_AVERAGE_SCORE REAL DEFAULT 0.0,
                $COLUMN_STREAK_DAYS INTEGER DEFAULT 0,
                $COLUMN_LAST_LOGIN INTEGER DEFAULT 0,
                $COLUMN_ACHIEVEMENTS TEXT DEFAULT '',
                $COLUMN_IS_GUEST INTEGER DEFAULT 0
            )
        """.trimIndent()

        val createQuizResultsTable = """
            CREATE TABLE $TABLE_QUIZ_RESULTS (
                $COLUMN_QUIZ_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER,
                $COLUMN_CATEGORY TEXT,
                $COLUMN_SCORE INTEGER,
                $COLUMN_MAX_SCORE INTEGER,
                $COLUMN_TIME_TAKEN INTEGER,
                $COLUMN_DATE_TAKEN INTEGER,
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID)
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createQuizResultsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_QUIZ_RESULTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // Insert user and return the user ID
    fun insertUser(user: User): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_USERNAME, user.username)
            put(COLUMN_PASSWORD_HASH, user.passwordHash)
            put(COLUMN_USER_TYPE, user.userType.name)
            put(COLUMN_CREATED_AT, user.createdAt)
            put(COLUMN_TOTAL_SCORE, user.totalScore)
            put(COLUMN_QUIZZES_COMPLETED, user.quizzesCompleted)
            put(COLUMN_AVERAGE_SCORE, user.averageScore)
            put(COLUMN_STREAK_DAYS, user.streakDays)
            put(COLUMN_LAST_LOGIN, user.lastLoginDate)
            put(COLUMN_ACHIEVEMENTS, gson.toJson(user.achievements))
            put(COLUMN_IS_GUEST, if (user.isGuest) 1 else 0)
        }

        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result
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

        var user: User? = null
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor)
        }
        cursor.close()
        db.close()
        return user
    }

    fun getUserById(id: Long): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        var user: User? = null
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor)
        }
        cursor.close()
        db.close()
        return user
    }

    fun updateUser(user: User): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_USERNAME, user.username)
            put(COLUMN_PASSWORD_HASH, user.passwordHash)
            put(COLUMN_USER_TYPE, user.userType.name)
            put(COLUMN_TOTAL_SCORE, user.totalScore)
            put(COLUMN_QUIZZES_COMPLETED, user.quizzesCompleted)
            put(COLUMN_AVERAGE_SCORE, user.averageScore)
            put(COLUMN_STREAK_DAYS, user.streakDays)
            put(COLUMN_LAST_LOGIN, user.lastLoginDate)
            put(COLUMN_ACHIEVEMENTS, gson.toJson(user.achievements))
        }

        val result = db.update(TABLE_USERS, values, "$COLUMN_ID = ?", arrayOf(user.id.toString()))
        db.close()
        return result > 0
    }

    fun deleteUser(userId: Long): Boolean {
        val db = writableDatabase

        // First delete quiz results
        db.delete(TABLE_QUIZ_RESULTS, "$COLUMN_USER_ID = ?", arrayOf(userId.toString()))

        // Then delete user
        val result = db.delete(TABLE_USERS, "$COLUMN_ID = ?", arrayOf(userId.toString()))
        db.close()
        return result > 0
    }

    fun emailExists(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )

        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return hashPassword(password) == hashedPassword
    }

    // Quiz Results methods
    fun insertQuizResult(quizResult: QuizResult): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, quizResult.userId)
            put(COLUMN_CATEGORY, quizResult.category)
            put(COLUMN_SCORE, quizResult.score)
            put(COLUMN_MAX_SCORE, quizResult.maxScore)
            put(COLUMN_TIME_TAKEN, quizResult.timeTaken)
            put(COLUMN_DATE_TAKEN, quizResult.dateTaken)
        }

        val result = db.insert(TABLE_QUIZ_RESULTS, null, values)
        db.close()
        return result
    }

    fun getQuizHistory(userId: Long, limit: Int = 50): List<QuizResult> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_QUIZ_RESULTS,
            null,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null,
            "$COLUMN_DATE_TAKEN DESC",
            limit.toString()
        )

        val results = mutableListOf<QuizResult>()
        while (cursor.moveToNext()) {
            results.add(cursorToQuizResult(cursor))
        }
        cursor.close()
        db.close()
        return results
    }

    fun deleteQuizHistory(userId: Long): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_QUIZ_RESULTS, "$COLUMN_USER_ID = ?", arrayOf(userId.toString()))
        db.close()
        return result > 0
    }

    private fun cursorToUser(cursor: android.database.Cursor): User {
        val achievementsJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACHIEVEMENTS)) ?: ""
        val achievements: List<String> = if (achievementsJson.isNotEmpty()) {
            try {
                gson.fromJson(achievementsJson, object : TypeToken<List<String>>() {}.type)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        return User(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)) ?: "",
            username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)) ?: "",
            passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH)) ?: "",
            userType = try {
                UserType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_TYPE)))
            } catch (e: Exception) {
                UserType.CASUAL
            },
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
            registrationDate = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
            totalScore = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_SCORE)),
            quizzesCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUIZZES_COMPLETED)),
            averageScore = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_AVERAGE_SCORE)),
            streakDays = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STREAK_DAYS)),
            lastLoginDate = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LAST_LOGIN)),
            achievements = achievements,
            isGuest = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_GUEST)) == 1
        )
    }

    private fun cursorToQuizResult(cursor: android.database.Cursor): QuizResult {
        return QuizResult(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_QUIZ_ID)),
            userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
            category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
            score = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE)),
            maxScore = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MAX_SCORE)),
            timeTaken = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TIME_TAKEN)),
            dateTaken = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATE_TAKEN))
        )
    }
}