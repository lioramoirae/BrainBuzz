package com.example.brainbuzz

import android.content.Context

class QuizIntegration(private val context: Context) {
    private val userManager = LocalUserManager(context)

    fun saveQuizResult(category: String, score: Int, maxScore: Int, timeTaken: Int = 0): Boolean {
        // Save the quiz result using the LocalUserManager
        return userManager.saveQuizResult(category, score, maxScore, timeTaken)
    }

    fun getCurrentUser(): LocalUser? {
        return userManager.getCurrentUser()
    }

    fun getQuizHistory(limit: Int = 20): List<QuizResult> {
        return userManager.getQuizHistory(limit)
    }

    fun isUserLoggedIn(): Boolean {
        return userManager.isLoggedIn()
    }

    // Calculate user statistics from quiz history
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

    // Create a simple User object from LocalUser for backward compatibility
    fun getUserForDisplay(): User? {
        val localUser = getCurrentUser() ?: return null

        return User(
            id = localUser.id.toLong(),
            username = localUser.username,
            email = localUser.email,
            userType = localUser.userType,
            totalScore = localUser.totalScore,
            quizzesCompleted = localUser.quizzesCompleted,
            averageScore = localUser.averageScore,
            registrationDate = localUser.createdAt,
            streakDays = 0, // Default value since we don't track streaks yet
            isGuest = localUser.isGuest
        )
    }

    fun updateUserType(newUserType: UserType): Boolean {
        return userManager.updateUserType(newUserType)
    }

    fun logout() {
        userManager.logout()
    }
}