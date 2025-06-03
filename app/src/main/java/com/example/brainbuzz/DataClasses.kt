package com.example.brainbuzz

// UserType enum
enum class UserType {
    CASUAL,
    COMPETITIVE,
    STUDENT
}

// User data class for backward compatibility
data class User(
    val id: Long = 0L,
    val username: String,
    val email: String,
    val userType: UserType,
    val totalScore: Int = 0,
    val quizzesCompleted: Int = 0,
    val averageScore: Double = 0.0,
    val registrationDate: Long = System.currentTimeMillis(),
    val streakDays: Int = 0,
    val isGuest: Boolean = false
)

// Simple data class for user statistics
data class UserStats(
    val totalQuizzes: Int,
    val averageScore: Double,
    val bestScore: Int,
    val totalScore: Int,
    val favoriteCategory: String
)

// Legacy enums for backward compatibility
enum class RegisterResult {
    SUCCESS, USERNAME_TOO_SHORT, PASSWORD_TOO_SHORT,
    INVALID_EMAIL, USER_EXISTS, FAILED
}

enum class LoginResult {
    SUCCESS, INVALID_CREDENTIALS, FAILED
}