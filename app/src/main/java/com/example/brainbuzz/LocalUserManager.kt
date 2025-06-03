package com.example.brainbuzz

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest

// Simple User Data Class
data class LocalUser(
    val id: Int = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val userType: UserType,
    val totalScore: Int = 0,
    val quizzesCompleted: Int = 0,
    val averageScore: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val isGuest: Boolean = false
)

// Local Database Helper
class LocalUserDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "brainbuzz_local.db"
        private const val DATABASE_VERSION = 1

        // Users table
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD_HASH = "password_hash"
        private const val COLUMN_USER_TYPE = "user_type"
        private const val COLUMN_TOTAL_SCORE = "total_score"
        private const val COLUMN_QUIZZES_COMPLETED = "quizzes_completed"
        private const val COLUMN_AVERAGE_SCORE = "average_score"
        private const val COLUMN_CREATED_AT = "created_at"
        private const val COLUMN_IS_GUEST = "is_guest"

        // Quiz History table
        private const val TABLE_QUIZ_HISTORY = "quiz_history"
        private const val COLUMN_HISTORY_ID = "id"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_SCORE = "score"
        private const val COLUMN_MAX_SCORE = "max_score"
        private const val COLUMN_DATE_TAKEN = "date_taken"
        private const val COLUMN_PERCENTAGE = "percentage"
        private const val COLUMN_TIME_TAKEN = "time_taken"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create users table
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD_HASH TEXT NOT NULL,
                $COLUMN_USER_TYPE TEXT NOT NULL DEFAULT 'CASUAL',
                $COLUMN_TOTAL_SCORE INTEGER DEFAULT 0,
                $COLUMN_QUIZZES_COMPLETED INTEGER DEFAULT 0,
                $COLUMN_AVERAGE_SCORE REAL DEFAULT 0.0,
                $COLUMN_CREATED_AT INTEGER DEFAULT 0,
                $COLUMN_IS_GUEST INTEGER DEFAULT 0
            )
        """.trimIndent()

        // Create quiz history table
        val createHistoryTable = """
            CREATE TABLE $TABLE_QUIZ_HISTORY (
                $COLUMN_HISTORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_SCORE INTEGER NOT NULL,
                $COLUMN_MAX_SCORE INTEGER NOT NULL,
                $COLUMN_PERCENTAGE REAL NOT NULL,
                $COLUMN_TIME_TAKEN INTEGER DEFAULT 0,
                $COLUMN_DATE_TAKEN INTEGER NOT NULL,
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createHistoryTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_QUIZ_HISTORY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }
}

// Local User Manager - Simplified
class LocalUserManager(private val context: Context) {
    private val database = LocalUserDatabase(context)
    private val sharedPrefs = context.getSharedPreferences("local_user_session", Context.MODE_PRIVATE)

    // Hash password for security
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    // Register new user
    fun registerUser(username: String, email: String, password: String, userType: UserType): RegisterResult {
        if (username.length < 3) return RegisterResult.USERNAME_TOO_SHORT
        if (password.length < 6) return RegisterResult.PASSWORD_TOO_SHORT
        if (!isValidEmail(email)) return RegisterResult.INVALID_EMAIL

        val db = database.writableDatabase

        try {
            // Check if user already exists
            val cursor = db.query(
                "users",
                arrayOf("id"),
                "username = ? OR email = ?",
                arrayOf(username, email),
                null, null, null
            )

            if (cursor.moveToFirst()) {
                cursor.close()
                return RegisterResult.USER_EXISTS
            }
            cursor.close()

            // Create new user
            val values = ContentValues().apply {
                put("username", username)
                put("email", email)
                put("password_hash", hashPassword(password))
                put("user_type", userType.name)
                put("total_score", 0)
                put("quizzes_completed", 0)
                put("average_score", 0.0)
                put("created_at", System.currentTimeMillis())
                put("is_guest", 0)
            }

            val userId = db.insert("users", null, values)
            if (userId != -1L) {
                // Auto-login after registration
                saveCurrentUser(userId.toInt())
                return RegisterResult.SUCCESS
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return RegisterResult.FAILED
    }

    // Login user
    fun loginUser(usernameOrEmail: String, password: String): LoginResult {
        val db = database.readableDatabase
        val passwordHash = hashPassword(password)

        try {
            val cursor = db.query(
                "users",
                arrayOf("id", "username", "email", "user_type", "total_score", "quizzes_completed", "average_score"),
                "(username = ? OR email = ?) AND password_hash = ? AND is_guest = 0",
                arrayOf(usernameOrEmail, usernameOrEmail, passwordHash),
                null, null, null
            )

            if (cursor.moveToFirst()) {
                val userId = cursor.getInt(0)
                cursor.close()
                saveCurrentUser(userId)
                return LoginResult.SUCCESS
            }
            cursor.close()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return LoginResult.INVALID_CREDENTIALS
    }

    // Create guest user
    fun createGuestUser(userType: UserType): Boolean {
        val db = database.writableDatabase

        try {
            val values = ContentValues().apply {
                put("username", "Guest_${System.currentTimeMillis()}")
                put("email", "guest_${System.currentTimeMillis()}@local.app")
                put("password_hash", hashPassword("guest123"))
                put("user_type", userType.name)
                put("total_score", 0)
                put("quizzes_completed", 0)
                put("average_score", 0.0)
                put("created_at", System.currentTimeMillis())
                put("is_guest", 1)
            }

            val userId = db.insert("users", null, values)
            if (userId != -1L) {
                saveCurrentUser(userId.toInt())
                return true
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return false
    }

    // Convert guest to registered user
    fun convertGuestToUser(username: String, email: String, password: String): RegisterResult {
        val currentUser = getCurrentUser() ?: return RegisterResult.FAILED
        if (!currentUser.isGuest) return RegisterResult.FAILED

        if (username.length < 3) return RegisterResult.USERNAME_TOO_SHORT
        if (password.length < 6) return RegisterResult.PASSWORD_TOO_SHORT
        if (!isValidEmail(email)) return RegisterResult.INVALID_EMAIL

        val db = database.writableDatabase

        try {
            // Check if username/email already exists (excluding current guest)
            val cursor = db.query(
                "users",
                arrayOf("id"),
                "(username = ? OR email = ?) AND id != ? AND is_guest = 0",
                arrayOf(username, email, currentUser.id.toString()),
                null, null, null
            )

            if (cursor.moveToFirst()) {
                cursor.close()
                return RegisterResult.USER_EXISTS
            }
            cursor.close()

            // Update guest to registered user
            val values = ContentValues().apply {
                put("username", username)
                put("email", email)
                put("password_hash", hashPassword(password))
                put("is_guest", 0)
            }

            val rowsUpdated = db.update(
                "users",
                values,
                "id = ?",
                arrayOf(currentUser.id.toString())
            )

            if (rowsUpdated > 0) {
                return RegisterResult.SUCCESS
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return RegisterResult.FAILED
    }

    // Get current logged-in user
    fun getCurrentUser(): LocalUser? {
        val userId = sharedPrefs.getInt("current_user_id", -1)
        if (userId == -1) return null

        val db = database.readableDatabase

        try {
            val cursor = db.query(
                "users",
                null,
                "id = ?",
                arrayOf(userId.toString()),
                null, null, null
            )

            if (cursor.moveToFirst()) {
                val user = LocalUser(
                    id = cursor.getInt(0),
                    username = cursor.getString(1),
                    email = cursor.getString(2),
                    passwordHash = cursor.getString(3),
                    userType = UserType.valueOf(cursor.getString(4)),
                    totalScore = cursor.getInt(5),
                    quizzesCompleted = cursor.getInt(6),
                    averageScore = cursor.getDouble(7),
                    createdAt = cursor.getLong(8),
                    isGuest = cursor.getInt(9) == 1
                )
                cursor.close()
                return user
            }
            cursor.close()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return null
    }

    // Save quiz result - Updated to include timeTaken parameter
    fun saveQuizResult(category: String, score: Int, maxScore: Int, timeTaken: Int = 0): Boolean {
        val currentUser = getCurrentUser() ?: return false
        val percentage = if (maxScore > 0) (score.toDouble() / maxScore) * 100 else 0.0

        val db = database.writableDatabase

        try {
            // Save quiz history
            val historyValues = ContentValues().apply {
                put("user_id", currentUser.id)
                put("category", category)
                put("score", score)
                put("max_score", maxScore)
                put("percentage", percentage)
                put("time_taken", timeTaken)
                put("date_taken", System.currentTimeMillis())
            }

            val historyId = db.insert("quiz_history", null, historyValues)

            if (historyId != -1L) {
                // Update user stats
                val newQuizzesCompleted = currentUser.quizzesCompleted + 1
                val newTotalScore = currentUser.totalScore + score
                val newAverageScore = if (newQuizzesCompleted > 0) {
                    newTotalScore.toDouble() / newQuizzesCompleted
                } else 0.0

                val userValues = ContentValues().apply {
                    put("total_score", newTotalScore)
                    put("quizzes_completed", newQuizzesCompleted)
                    put("average_score", newAverageScore)
                }

                db.update(
                    "users",
                    userValues,
                    "id = ?",
                    arrayOf(currentUser.id.toString())
                )

                return true
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return false
    }

    // Get quiz history - Fixed to match QuizResult constructor
    fun getQuizHistory(limit: Int = 20): List<QuizResult> {
        val currentUser = getCurrentUser() ?: return emptyList()
        val results = mutableListOf<QuizResult>()

        val db = database.readableDatabase

        try {
            val cursor = db.query(
                "quiz_history",
                null,
                "user_id = ?",
                arrayOf(currentUser.id.toString()),
                null, null,
                "date_taken DESC",
                limit.toString()
            )

            while (cursor.moveToNext()) {
                val result = QuizResult(
                    id = cursor.getLong(0),
                    userId = cursor.getLong(1),
                    category = cursor.getString(2),
                    score = cursor.getInt(3),
                    maxScore = cursor.getInt(4),
                    timeTaken = cursor.getInt(6),
                    dateTaken = cursor.getLong(7)
                )
                results.add(result)
            }
            cursor.close()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return results
    }

    // Update user type
    fun updateUserType(newUserType: UserType): Boolean {
        val currentUser = getCurrentUser() ?: return false

        val db = database.writableDatabase

        try {
            val values = ContentValues().apply {
                put("user_type", newUserType.name)
            }

            val rowsUpdated = db.update(
                "users",
                values,
                "id = ?",
                arrayOf(currentUser.id.toString())
            )

            return rowsUpdated > 0

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return false
    }

    // Logout
    fun logout() {
        sharedPrefs.edit().remove("current_user_id").apply()
    }

    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        return sharedPrefs.getInt("current_user_id", -1) != -1
    }

    // Helper functions
    private fun saveCurrentUser(userId: Int) {
        sharedPrefs.edit().putInt("current_user_id", userId).apply()
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Result enums
    enum class RegisterResult {
        SUCCESS, USERNAME_TOO_SHORT, PASSWORD_TOO_SHORT,
        INVALID_EMAIL, USER_EXISTS, FAILED
    }

    enum class LoginResult {
        SUCCESS, INVALID_CREDENTIALS, FAILED
    }
}