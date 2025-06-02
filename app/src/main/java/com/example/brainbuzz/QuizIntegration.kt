package com.example.brainbuzz

import android.content.Context
import android.widget.Toast

object QuizIntegration {

    fun saveQuizResult(
        context: Context,
        score: Int,
        maxScore: Int,
        timeTaken: Int = 0,
        category: String = "General"
    ) {
        val userManager = UserManager.getInstance(context)

        if (userManager.isLoggedIn()) {
            val success = userManager.updateUserStats(score, maxScore, timeTaken, category)

            if (success) {
                val user = userManager.getCurrentUser()
                user?.let {
                    val message = if (it.isGuest) {
                        "Quiz completed! Score: $score/$maxScore"
                    } else {
                        "Quiz saved! Total score: ${it.totalScore}"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun getCurrentUserInfo(context: Context): String? {
        val userManager = UserManager.getInstance(context)
        val user = userManager.getCurrentUser()

        return user?.let {
            if (it.isGuest) {
                "Welcome Guest | Score: ${it.totalScore} | Quizzes: ${it.quizzesCompleted}"
            } else {
                "Welcome ${it.username} | Score: ${it.totalScore} | Quizzes: ${it.quizzesCompleted}"
            }
        }
    }

    fun getUserStats(context: Context): UserStats? {
        val userManager = UserManager.getInstance(context)
        val user = userManager.getCurrentUser()

        return user?.let {
            UserStats(
                username = it.username,
                userType = it.userType,
                totalScore = it.totalScore,
                quizzesCompleted = it.quizzesCompleted,
                averageScore = it.averageScore.toDouble(),
                streakDays = it.streakDays,
                isGuest = it.isGuest
            )
        }
    }

    fun getEncouragementMessage(context: Context, score: Int, maxScore: Int): String {
        val userManager = UserManager.getInstance(context)
        val user = userManager.getCurrentUser()
        val percentage = if (maxScore > 0) (score.toDouble() / maxScore) * 100 else 0.0

        return when (user?.userType) {
            UserType.STUDENT -> {
                when {
                    percentage >= 90 -> "Excellent! You're mastering this subject! 📚"
                    percentage >= 70 -> "Good job! Keep studying to improve further! 📖"
                    percentage >= 50 -> "Not bad! Review the topics you missed. 📝"
                    else -> "Keep practicing! Every attempt makes you better! 💪"
                }
            }
            UserType.COMPETITIVE -> {
                when {
                    percentage >= 90 -> "Outstanding! You're climbing the leaderboard! 🏆"
                    percentage >= 70 -> "Great score! Challenge your friends! 🎯"
                    percentage >= 50 -> "Good effort! Practice more to dominate! ⚡"
                    else -> "Don't give up! Champions are made through practice! 🥊"
                }
            }
            else -> {
                when {
                    percentage >= 90 -> "Amazing! You're a quiz master! 🌟"
                    percentage >= 70 -> "Well done! That was fun! 😊"
                    percentage >= 50 -> "Nice try! Quiz time is always learning time! 🧠"
                    else -> "Good attempt! Every quiz makes you smarter! 🚀"
                }
            }
        }
    }

    fun isLoggedIn(context: Context): Boolean {
        val userManager = UserManager.getInstance(context)
        return userManager.isLoggedIn()
    }

    fun logout(context: Context) {
        val userManager = UserManager.getInstance(context)
        userManager.logout()
    }
}

data class UserStats(
    val username: String,
    val userType: UserType,
    val totalScore: Int,
    val quizzesCompleted: Int,
    val averageScore: Double,
    val streakDays: Int,
    val isGuest: Boolean
)