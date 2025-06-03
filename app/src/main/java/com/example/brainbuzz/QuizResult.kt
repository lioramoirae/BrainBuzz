package com.example.brainbuzz

// Quiz Result data class
data class QuizResult(
    val id: Long = 0L,
    val userId: Long,
    val category: String,
    val score: Int,
    val maxScore: Int,
    val timeTaken: Int,
    val dateTaken: Long = System.currentTimeMillis()
)