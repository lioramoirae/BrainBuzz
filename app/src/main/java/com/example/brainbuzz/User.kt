package com.example.brainbuzz

data class User(
    val id: Long = 0L,
    val email: String = "",
    val username: String = "",
    val passwordHash: String = "",
    val userType: UserType = UserType.CASUAL,
    val createdAt: Long = System.currentTimeMillis(),
    val registrationDate: Long = System.currentTimeMillis(),
    val totalScore: Int = 0,
    val quizzesCompleted: Int = 0,
    val averageScore: Float = 0.0f,
    val streakDays: Int = 0,
    val lastLoginDate: Long = 0,
    val achievements: List<String> = emptyList(),
    val isGuest: Boolean = false
)
