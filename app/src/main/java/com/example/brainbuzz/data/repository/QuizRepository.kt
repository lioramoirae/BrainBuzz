package com.example.brainbuzz.data.repository

import com.example.brainbuzz.data.database.QuizDao
import com.example.brainbuzz.data.model.Question
import com.example.brainbuzz.data.model.QuizResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepository @Inject constructor(
    private val quizDao: QuizDao
) {
    suspend fun getQuestionsByCategory(category: String, limit: Int = 10): List<Question> {
        return quizDao.getQuestionsByCategory(category, limit)
    }

    fun getAllQuestions(): Flow<List<Question>> {
        return quizDao.getAllQuestions()
    }

    suspend fun getAllCategories(): List<String> {
        return quizDao.getAllCategories()
    }

    suspend fun insertQuestions(questions: List<Question>) {
        quizDao.insertQuestions(questions)
    }

    suspend fun insertQuizResult(result: QuizResult) {
        quizDao.insertQuizResult(result)
    }

    fun getAllQuizResults(): Flow<List<QuizResult>> {
        return quizDao.getAllQuizResults()
    }

    fun getQuizResultsByCategory(category: String): Flow<List<QuizResult>> {
        return quizDao.getQuizResultsByCategory(category)
    }

    suspend fun clearHistory() {
        quizDao.clearHistory()
    }
}
