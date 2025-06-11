package com.example.brainbuzz

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "brainbuzz.db"
        private const val DATABASE_VERSION = 1

        // Users table
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_USER_TYPE = "user_type"
        const val COLUMN_TOTAL_SCORE = "total_score"
        const val COLUMN_QUIZZES_COMPLETED = "quizzes_completed"
        const val COLUMN_AVERAGE_SCORE = "average_score"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_IS_GUEST = "is_guest"

        // Quiz results table
        const val TABLE_QUIZ_RESULTS = "quiz_results"
        const val COLUMN_RESULT_ID = "id"
        const val COLUMN_RESULT_USER_ID = "user_id"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_SCORE = "score"
        const val COLUMN_MAX_SCORE = "max_score"
        const val COLUMN_TIME_TAKEN = "time_taken"
        const val COLUMN_DATE_TAKEN = "date_taken"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create users table
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_USER_TYPE TEXT NOT NULL,
                $COLUMN_TOTAL_SCORE INTEGER DEFAULT 0,
                $COLUMN_QUIZZES_COMPLETED INTEGER DEFAULT 0,
                $COLUMN_AVERAGE_SCORE REAL DEFAULT 0.0,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_IS_GUEST INTEGER DEFAULT 0
            )
        """.trimIndent()

        // Create quiz results table
        val createQuizResultsTable = """
            CREATE TABLE $TABLE_QUIZ_RESULTS (
                $COLUMN_RESULT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_RESULT_USER_ID INTEGER NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_SCORE INTEGER NOT NULL,
                $COLUMN_MAX_SCORE INTEGER NOT NULL,
                $COLUMN_TIME_TAKEN INTEGER DEFAULT 0,
                $COLUMN_DATE_TAKEN INTEGER NOT NULL,
                FOREIGN KEY ($COLUMN_RESULT_USER_ID) REFERENCES $TABLE_USERS ($COLUMN_USER_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        // Create indexes for better performance
        val createUserEmailIndex = """
            CREATE INDEX idx_user_email ON $TABLE_USERS ($COLUMN_EMAIL)
        """.trimIndent()

        val createUserUsernameIndex = """
            CREATE INDEX idx_user_username ON $TABLE_USERS ($COLUMN_USERNAME)
        """.trimIndent()

        val createQuizResultsUserIndex = """
            CREATE INDEX idx_quiz_results_user ON $TABLE_QUIZ_RESULTS ($COLUMN_RESULT_USER_ID)
        """.trimIndent()

        val createQuizResultsDateIndex = """
            CREATE INDEX idx_quiz_results_date ON $TABLE_QUIZ_RESULTS ($COLUMN_DATE_TAKEN)
        """.trimIndent()

        // Execute table creation
        db.execSQL(createUsersTable)
        db.execSQL(createQuizResultsTable)

        // Create indexes
        db.execSQL(createUserEmailIndex)
        db.execSQL(createUserUsernameIndex)
        db.execSQL(createQuizResultsUserIndex)
        db.execSQL(createQuizResultsDateIndex)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades
        when (oldVersion) {
            1 -> {
                // Future upgrades will go here
                // For now, just recreate tables
                db.execSQL("DROP TABLE IF EXISTS $TABLE_QUIZ_RESULTS")
                db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
                onCreate(db)
            }
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // Enable foreign key constraints
        db.setForeignKeyConstraintsEnabled(true)
    }

    // Helper method to reset database (for testing)
    fun resetDatabase() {
        val db = writableDatabase
        db.execSQL("DROP TABLE IF EXISTS $TABLE_QUIZ_RESULTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // Helper method to get database info
    fun getDatabaseInfo(): String {
        val db = readableDatabase
        val userCount = db.rawQuery("SELECT COUNT(*) FROM $TABLE_USERS", null)
        val quizCount = db.rawQuery("SELECT COUNT(*) FROM $TABLE_QUIZ_RESULTS", null)

        userCount.moveToFirst()
        quizCount.moveToFirst()

        val info = """
            Database: $DATABASE_NAME (Version $DATABASE_VERSION)
            Users: ${userCount.getInt(0)}
            Quiz Results: ${quizCount.getInt(0)}
        """.trimIndent()

        userCount.close()
        quizCount.close()

        return info
    }
}