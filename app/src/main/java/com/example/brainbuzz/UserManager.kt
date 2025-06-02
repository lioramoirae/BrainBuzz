// Fixed UserManager.kt - Updated to work with your existing database structure
package com.example.brainbuzz

import android.content.Context
import android.content.SharedPreferences

class UserManager private constructor(private val context: Context) {

    companion object {
        private const val PREF_NAME = "brainbuzz_user_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_IS_GUEST = "is_guest"

        @Volatile
        private var INSTANCE: UserManager? = null

        fun getInstance(context: Context): UserManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val dbHelper: UserDatabaseHelper = UserDatabaseHelper(context)
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private var currentUser: User? = null

    enum class RegisterResult {
        SUCCESS,
        EMAIL_EXISTS,
        USERNAME_TOO_SHORT,
        PASSWORD_TOO_SHORT,
        FAILED
    }

    enum class LoginResult {
        SUCCESS,
        INVALID_CREDENTIALS,
        USER_NOT_FOUND
    }

    init {
        // Load current user if logged in
        if (isLoggedIn()) {
            loadCurrentUser()
        }
    }

    // Authentication methods
    fun registerUser(email: String, username: String, password: String, userType: UserType): RegisterResult {
        // Validation
        if (username.length < 3) return RegisterResult.USERNAME_TOO_SHORT
        if (password.length < 6) return RegisterResult.PASSWORD_TOO_SHORT

        // Check if email already exists
        if (dbHelper.emailExists(email)) return RegisterResult.EMAIL_EXISTS

        try {
            val passwordHash = dbHelper.hashPassword(password)
            val user = User(
                id = 0L, // Will be auto-generated
                email = email,
                username = username,
                passwordHash = passwordHash,
                userType = userType,
                createdAt = System.currentTimeMillis(),
                registrationDate = System.currentTimeMillis(),
                lastLoginDate = System.currentTimeMillis()
            )

            val userId = dbHelper.insertUser(user)
            if (userId > 0) {
                // Auto-login after registration
                val newUser = user.copy(id = userId)
                loginUser(newUser)
                return RegisterResult.SUCCESS
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return RegisterResult.FAILED
    }

    fun loginUser(email: String, password: String): LoginResult {
        try {
            val user = dbHelper.getUserByEmail(email) ?: return LoginResult.USER_NOT_FOUND

            if (dbHelper.verifyPassword(password, user.passwordHash)) {
                // Update last login
                val updatedUser = user.copy(lastLoginDate = System.currentTimeMillis())
                dbHelper.updateUser(updatedUser)

                loginUser(updatedUser)
                return LoginResult.SUCCESS
            } else {
                return LoginResult.INVALID_CREDENTIALS
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return LoginResult.USER_NOT_FOUND
    }

    private fun loginUser(user: User) {
        currentUser = user
        sharedPrefs.edit().apply {
            putLong(KEY_USER_ID, user.id)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putBoolean(KEY_IS_GUEST, user.isGuest)
            apply()
        }
    }

    fun continueAsGuest(): User {
        val guestUser = User(
            id = -1, // Special ID for guest
            email = "guest@brainbuzz.com",
            username = "Guest",
            userType = UserType.CASUAL,
            isGuest = true,
            createdAt = System.currentTimeMillis(),
            registrationDate = System.currentTimeMillis(),
            lastLoginDate = System.currentTimeMillis()
        )

        currentUser = guestUser
        sharedPrefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putBoolean(KEY_IS_GUEST, true)
            apply()
        }

        return guestUser
    }

    fun logout() {
        currentUser = null
        sharedPrefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPrefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun isGuest(): Boolean {
        return sharedPrefs.getBoolean(KEY_IS_GUEST, false)
    }

    // User data methods
    fun getCurrentUser(): User? {
        if (currentUser == null && isLoggedIn()) {
            loadCurrentUser()
        }
        return currentUser
    }

    private fun loadCurrentUser() {
        if (isGuest()) {
            // Load guest user
            currentUser = User(
                id = -1,
                email = "guest@brainbuzz.com",
                username = "Guest",
                userType = UserType.CASUAL,
                isGuest = true,
                createdAt = System.currentTimeMillis(),
                registrationDate = System.currentTimeMillis(),
                lastLoginDate = System.currentTimeMillis()
            )
        } else {
            // Load from database
            val userId = sharedPrefs.getLong(KEY_USER_ID, -1)
            if (userId > 0) {
                currentUser = dbHelper.getUserById(userId)
            }
        }
    }

    fun updateUserStats(score: Int, maxScore: Int, timeTaken: Int, category: String = "General"): Boolean {
        val user = currentUser ?: return false

        try {
            // For guests, just update in memory
            if (user.isGuest) {
                val newQuizzesCompleted = user.quizzesCompleted + 1
                val newTotalScore = user.totalScore + score
                val newAverageScore = if (newQuizzesCompleted > 0) {
                    newTotalScore.toFloat() / newQuizzesCompleted
                } else {
                    0f
                }

                currentUser = user.copy(
                    totalScore = newTotalScore,
                    quizzesCompleted = newQuizzesCompleted,
                    averageScore = newAverageScore,
                    lastLoginDate = System.currentTimeMillis()
                )
                return true
            }

            // For registered users, save to database
            val quizResult = QuizResult(
                userId = user.id,
                category = category,
                score = score,
                maxScore = maxScore,
                timeTaken = timeTaken
            )

            // Insert quiz result
            val quizId = dbHelper.insertQuizResult(quizResult)

            if (quizId > 0) {
                // Calculate new stats
                val newQuizzesCompleted = user.quizzesCompleted + 1
                val newTotalScore = user.totalScore + score
                val newAverageScore = if (newQuizzesCompleted > 0) {
                    newTotalScore.toFloat() / newQuizzesCompleted
                } else {
                    user.averageScore
                }

                // Update user stats
                val updatedUser = user.copy(
                    totalScore = newTotalScore,
                    quizzesCompleted = newQuizzesCompleted,
                    averageScore = newAverageScore,
                    lastLoginDate = System.currentTimeMillis()
                )

                if (dbHelper.updateUser(updatedUser)) {
                    currentUser = updatedUser
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    fun updateUserType(newUserType: UserType): Boolean {
        val user = currentUser ?: return false

        if (user.isGuest) {
            // For guests, just update in memory
            currentUser = user.copy(userType = newUserType)
            return true
        }

        try {
            val updatedUser = user.copy(userType = newUserType)
            if (dbHelper.updateUser(updatedUser)) {
                currentUser = updatedUser
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    fun updateUserProfile(email: String? = null, username: String? = null): Boolean {
        val user = currentUser ?: return false

        if (user.isGuest) {
            // For guests, just update username in memory
            username?.let {
                currentUser = user.copy(username = it)
            }
            return true
        }

        try {
            val updatedUser = user.copy(
                email = email ?: user.email,
                username = username ?: user.username
            )

            if (dbHelper.updateUser(updatedUser)) {
                currentUser = updatedUser
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    fun resetUserStatistics(): Boolean {
        val user = currentUser ?: return false

        if (user.isGuest) {
            // For guests, just reset in memory
            currentUser = user.copy(
                totalScore = 0,
                quizzesCompleted = 0,
                averageScore = 0f,
                streakDays = 0
            )
            return true
        }

        try {
            // Delete quiz history
            dbHelper.deleteQuizHistory(user.id)

            // Reset user stats
            val resetUser = user.copy(
                totalScore = 0,
                quizzesCompleted = 0,
                averageScore = 0f,
                streakDays = 0
            )

            if (dbHelper.updateUser(resetUser)) {
                currentUser = resetUser
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    fun deleteAccount(): Boolean {
        val user = currentUser ?: return false

        if (user.isGuest) {
            logout()
            return true
        }

        try {
            if (dbHelper.deleteUser(user.id)) {
                logout()
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    fun getQuizHistory(limit: Int = 50): List<QuizResult> {
        val user = currentUser ?: return emptyList()

        if (user.isGuest) {
            return emptyList() // Guests don't have persistent history
        }

        return try {
            dbHelper.getQuizHistory(user.id, limit)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun changePassword(currentPassword: String, newPassword: String): Boolean {
        val user = currentUser ?: return false

        if (user.isGuest) return false
        if (newPassword.length < 6) return false

        try {
            // Verify current password
            if (!dbHelper.verifyPassword(currentPassword, user.passwordHash)) {
                return false
            }

            // Update password
            val newPasswordHash = dbHelper.hashPassword(newPassword)
            val updatedUser = user.copy(passwordHash = newPasswordHash)

            if (dbHelper.updateUser(updatedUser)) {
                currentUser = updatedUser
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    // Helper method to set current user (for testing or migration)
    fun setCurrentUser(user: User) {
        currentUser = user
    }
}