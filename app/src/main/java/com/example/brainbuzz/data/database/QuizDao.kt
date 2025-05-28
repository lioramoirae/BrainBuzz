package com.example.brainbuzz.data.database

import androidx.room.*
import com.example.brainbuzz.data.model.Question
import com.example.brainbuzz.data.model.QuizResult
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Query("SELECT * FROM questions WHERE category = :category ORDER BY RANDOM() LIMIT :limit")
    suspend fun getQuestionsByCategory(category: String, limit: Int = 10): List<Question>

    @Query("SELECT * FROM questions")
    fun getAllQuestions(): Flow<List<Question>>

    @Query("SELECT DISTINCT category FROM questions")
    suspend fun getAllCategories(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>)

    @Insert
    suspend fun insertQuizResult(result: QuizResult)

    @Query("SELECT * FROM quiz_results ORDER BY timestamp DESC")
    fun getAllQuizResults(): Flow<List<QuizResult>>

    @Query("SELECT * FROM quiz_results WHERE category = :category ORDER BY timestamp DESC")
    fun getQuizResultsByCategory(category: String): Flow<List<QuizResult>>

    @Query("DELETE FROM quiz_results")
    suspend fun clearHistory()
}
