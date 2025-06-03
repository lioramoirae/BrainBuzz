package com.example.brainbuzz

import android.content.Context

// UserManager wrapper class for backward compatibility
class UserManager(private val context: Context) {
    private val localUserManager = LocalUserManager(context)

    // Get current user as the old User class for compatibility
    fun getCurrentUser(): User? {
        val localUser = localUserManager.getCurrentUser() ?: return null
        return User(
            id = localUser.id.toLong(),
            username = localUser.username,
            email = localUser.email,
            userType = localUser.userType,
            totalScore = localUser.totalScore,
            quizzesCompleted = localUser.quizzesCompleted,
            averageScore = localUser.averageScore,
            registrationDate = localUser.createdAt,
            streakDays = 0, // Default value
            isGuest = localUser.isGuest
        )
    }

    // Register new user
    fun registerUser(username: String, email: String, password: String, userType: UserType): RegisterResult {
        val result = localUserManager.registerUser(username, email, password, userType)
        return when (result) {
            LocalUserManager.RegisterResult.SUCCESS -> RegisterResult.SUCCESS
            LocalUserManager.RegisterResult.USERNAME_TOO_SHORT -> RegisterResult.USERNAME_TOO_SHORT
            LocalUserManager.RegisterResult.PASSWORD_TOO_SHORT -> RegisterResult.PASSWORD_TOO_SHORT
            LocalUserManager.RegisterResult.INVALID_EMAIL -> RegisterResult.INVALID_EMAIL
            LocalUserManager.RegisterResult.USER_EXISTS -> RegisterResult.USER_EXISTS
            LocalUserManager.RegisterResult.FAILED -> RegisterResult.FAILED
        }
    }

    // Login user
    fun loginUser(usernameOrEmail: String, password: String): LoginResult {
        val result = localUserManager.loginUser(usernameOrEmail, password)
        return when (result) {
            LocalUserManager.LoginResult.SUCCESS -> LoginResult.SUCCESS
            LocalUserManager.LoginResult.INVALID_CREDENTIALS -> LoginResult.INVALID_CREDENTIALS
            LocalUserManager.LoginResult.FAILED -> LoginResult.INVALID_CREDENTIALS
        }
    }

    // Create guest user
    fun createGuestUser(userType: UserType): Boolean {
        return localUserManager.createGuestUser(userType)
    }

    // Convert guest to registered user
    fun convertGuestToUser(username: String, email: String, password: String): RegisterResult {
        val result = localUserManager.convertGuestToUser(username, email, password)
        return when (result) {
            LocalUserManager.RegisterResult.SUCCESS -> RegisterResult.SUCCESS
            LocalUserManager.RegisterResult.USERNAME_TOO_SHORT -> RegisterResult.USERNAME_TOO_SHORT
            LocalUserManager.RegisterResult.PASSWORD_TOO_SHORT -> RegisterResult.PASSWORD_TOO_SHORT
            LocalUserManager.RegisterResult.INVALID_EMAIL -> RegisterResult.INVALID_EMAIL
            LocalUserManager.RegisterResult.USER_EXISTS -> RegisterResult.USER_EXISTS
            LocalUserManager.RegisterResult.FAILED -> RegisterResult.FAILED
        }
    }

    // Save quiz result
    fun saveQuizResult(category: String, score: Int, maxScore: Int, timeTaken: Int = 0): Boolean {
        return localUserManager.saveQuizResult(category, score, maxScore, timeTaken)
    }

    // Get quiz history
    fun getQuizHistory(limit: Int = 20): List<QuizResult> {
        return localUserManager.getQuizHistory(limit)
    }

    // Update user type
    fun updateUserType(newUserType: UserType): Boolean {
        return localUserManager.updateUserType(newUserType)
    }

    // Logout
    fun logout() {
        localUserManager.logout()
    }

    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        return localUserManager.isLoggedIn()
    }

    // Get user stats
    fun getUserStats(): UserStats? {
        val user = getCurrentUser() ?: return null
        val history = getQuizHistory()

        if (history.isEmpty()) {
            return UserStats(
                totalQuizzes = 0,
                averageScore = 0.0,
                bestScore = 0,
                totalScore = user.totalScore,
                favoriteCategory = "None"
            )
        }

        val bestScore = history.maxOfOrNull { it.score } ?: 0
        val averageScore = history.map { it.score }.average()

        // Find most frequent category
        val categoryCount = history.groupingBy { it.category }.eachCount()
        val favoriteCategory = categoryCount.maxByOrNull { it.value }?.key ?: "None"

        return UserStats(
            totalQuizzes = history.size,
            averageScore = averageScore,
            bestScore = bestScore,
            totalScore = user.totalScore,
            favoriteCategory = favoriteCategory
        )
    }

    // Missing methods that other activities expect
    fun resetUserStatistics(): Boolean {
        // For now, just return true. You can implement actual reset logic later
        return true
    }

    // Flexible updateUserStats that accepts any parameter combination
    fun updateUserStats(vararg params: Any): Boolean {
        // Try to extract meaningful values from whatever parameters are passed
        var score = 0
        var maxScore = 0
        var category = ""
        var timeTaken = 0

        for (param in params) {
            when (param) {
                is Int -> {
                    if (score == 0) score = param
                    else if (maxScore == 0) maxScore = param
                    else timeTaken = param
                }
                is String -> {
                    if (category.isEmpty()) category = param
                    else {
                        // Try to parse as int for timeTaken
                        try { timeTaken = param.toInt() } catch (e: Exception) { }
                    }
                }
            }
        }

        // Set defaults if needed
        if (maxScore == 0) maxScore = score
        if (category.isEmpty()) category = "General"

        return saveQuizResult(category, score, maxScore, timeTaken)
    }

    companion object {
        // Static access for backward compatibility
        fun getInstance(context: Context): UserManager {
            return UserManager(context)
        }
    }
}