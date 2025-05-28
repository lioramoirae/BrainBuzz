package com.example.brainbuzz.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Entity(tableName = "questions")
@Parcelize
data class Question(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: String,
    val questionText: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val option4: String,
    val correctAnswer: Int, // 0-3 for options 1-4
    val explanation: String = ""
) : Parcelable

@Entity(tableName = "quiz_results")
@Parcelize
data class QuizResult(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: String,
    val score: Int,
    val totalQuestions: Int,
    val timeSpent: Long, // in milliseconds
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable