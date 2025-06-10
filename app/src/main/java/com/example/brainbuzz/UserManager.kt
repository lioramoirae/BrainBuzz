package com.example.brainbuzz

import android.content.Context

class UserManager(private val context: Context) {
    private val localUserManager = LocalUserManager(context)

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
            streakDays = 0,
            isGuest = localUser.isGuest
        )
    }

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

    fun loginUser(usernameOrEmail: String, password: String): LoginResult {
        val result = localUserManager.loginUser(usernameOrEmail, password)
        return when (result) {
            LocalUserManager.LoginResult.SUCCESS -> LoginResult.SUCCESS
            else -> LoginResult.INVALID_CREDENTIALS
        }
    }

    fun createGuestUser(userType: UserType): Boolean {
        return localUserManager.createGuestUser(userType)
    }

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

    fun saveQuizResult(category: String, score: Int, maxScore: Int, timeTaken: Int = 0): Boolean {
        return localUserManager.saveQuizResult(category, score, maxScore, timeTaken)
    }

    fun getQuizHistory(limit: Int = 20): List<QuizResult> {
        return localUserManager.getQuizHistory(limit)
    }

    fun updateUserType(newUserType: UserType): Boolean {
        return localUserManager.updateUserType(newUserType)
    }

    fun logout() {
        localUserManager.logout()
    }

    fun isLoggedIn(): Boolean {
        return localUserManager.isLoggedIn()
    }

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
        val favoriteCategory = history.groupingBy { it.category }.eachCount()
            .maxByOrNull { it.value }?.key ?: "None"

        return UserStats(
            totalQuizzes = history.size,
            averageScore = averageScore,
            bestScore = bestScore,
            totalScore = user.totalScore,
            favoriteCategory = favoriteCategory
        )
    }

    fun resetUserStatistics(): Boolean {
        // Stub method
        return true
    }

    fun updateUserStats(vararg params: Any): Boolean {
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
                }
            }
        }

        if (maxScore == 0) maxScore = score
        if (category.isEmpty()) category = "General"

        return saveQuizResult(category, score, maxScore, timeTaken)
    }

    companion object {
        fun getInstance(context: Context): UserManager {
            return UserManager(context)
        }
    }
}
