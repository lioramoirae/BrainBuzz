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
        val config = getQuizConfig()
        val adjustedScore = (score * config.pointsMultiplier).toInt()
        val adjustedMaxScore = (maxScore * config.pointsMultiplier).toInt()
        return localUserManager.saveQuizResult(category, adjustedScore, adjustedMaxScore, timeTaken)
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
        val user = getCurrentUser() ?: return false
        if (user.isGuest) return false

        localUserManager.deleteQuizHistoryByUserId(user.id)
        val currentType = user.userType
        return localUserManager.updateUserType(currentType)
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

    fun loadCurrentUser() {
        getCurrentUser()
    }

    fun getQuizConfig(): QuizConfig {
        return when (getCurrentUser()?.userType) {
            UserType.STUDENT -> QuizConfig(
                questionsPerQuiz = 15,
                timePerQuestion = 45,
                showCorrectAnswers = true,
                allowRetakes = true,
                difficultyLevel = "Mixed",
                pointsMultiplier = 1.0,
                hintsAllowed = 2,
                showExplanations = true
            )
            UserType.COMPETITIVE -> QuizConfig(
                questionsPerQuiz = 20,
                timePerQuestion = 20,
                showCorrectAnswers = false,
                allowRetakes = false,
                difficultyLevel = "Hard",
                pointsMultiplier = 1.5,
                hintsAllowed = 0,
                showExplanations = false
            )
            UserType.CASUAL -> QuizConfig(
                questionsPerQuiz = 10,
                timePerQuestion = 60,
                showCorrectAnswers = true,
                allowRetakes = true,
                difficultyLevel = "Easy",
                pointsMultiplier = 1.0,
                hintsAllowed = 3,
                showExplanations = true
            )
            else -> QuizConfig(
                questionsPerQuiz = 5,
                timePerQuestion = 30,
                showCorrectAnswers = true,
                allowRetakes = true,
                difficultyLevel = "Easy",
                pointsMultiplier = 0.5,
                hintsAllowed = 1,
                showExplanations = false
            )
        }
    }

    fun getMotivationalMessage(score: Int, maxScore: Int): String {
        val percentage = if (maxScore > 0) (score * 100) / maxScore else 0

        return when (getCurrentUser()?.userType) {
            UserType.STUDENT -> when {
                percentage >= 90 -> "Excellent work! 🎓 You're mastering this subject!"
                percentage >= 80 -> "Great job! 📚 Keep studying and you'll excel!"
                percentage >= 70 -> "Good progress! 📖 Review the missed topics."
                percentage >= 60 -> "You're learning! 💪 Focus on weak areas."
                else -> "Keep studying! 📝 Every mistake is a learning opportunity!"
            }
            UserType.COMPETITIVE -> when {
                percentage >= 90 -> "Outstanding! 🏆 You're crushing the competition!"
                percentage >= 80 -> "Strong performance! ⚡ Push for perfection!"
                percentage >= 70 -> "Decent score! 🎯 Step up your game!"
                percentage >= 60 -> "Room for improvement! 💯 Train harder!"
                else -> "Challenge yourself! 🔥 Come back stronger!"
            }
            UserType.CASUAL -> when {
                percentage >= 90 -> "Amazing! 🌟 You're a quiz superstar!"
                percentage >= 80 -> "Fantastic! 😊 You really know your stuff!"
                percentage >= 70 -> "Nice work! 👍 That was fun!"
                percentage >= 60 -> "Good effort! 😄 Try another quiz!"
                else -> "No worries! 🙂 Quizzes are meant to be fun!"
            }
            else -> when {
                percentage >= 80 -> "Great job! 👏 Consider creating an account!"
                percentage >= 60 -> "Not bad! 🙂 Sign up to track your progress!"
                else -> "Good try! 😊 Register to unlock more features!"
            }
        }
    }

    fun getBestScoreForCategory(category: String): Int {
        return getQuizHistory().filter { it.category == category }.maxOfOrNull { it.score } ?: 0
    }

    fun getQuizCountForCategory(category: String): Int {
        return getQuizHistory().count { it.category == category }
    }

    fun getAverageScoreForCategory(category: String): Double {
        val categoryResults = getQuizHistory().filter { it.category == category }
        return if (categoryResults.isNotEmpty()) {
            categoryResults.map { it.score }.average()
        } else 0.0
    }

    data class QuizConfig(
        val questionsPerQuiz: Int,
        val timePerQuestion: Int,
        val showCorrectAnswers: Boolean,
        val allowRetakes: Boolean,
        val difficultyLevel: String,
        val pointsMultiplier: Double,
        val hintsAllowed: Int,
        val showExplanations: Boolean
    )

    companion object {
        fun getInstance(context: Context): UserManager {
            return UserManager(context)
        }
    }
}