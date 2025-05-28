package com.example.brainbuzz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainbuzz.data.model.Question
import com.example.brainbuzz.data.model.QuizResult
import com.example.brainbuzz.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    val quizResults = repository.getAllQuizResults()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadCategories()
        initializeQuestions()
    }

    private fun initializeQuestions() {
        viewModelScope.launch {
            val sampleQuestions = listOf(
                Question(
                    category = "Science",
                    questionText = "What is the largest planet in our solar system?",
                    option1 = "Earth",
                    option2 = "Jupiter",
                    option3 = "Saturn",
                    option4 = "Neptune",
                    correctAnswer = 1,
                    explanation = "Jupiter is the largest planet in our solar system with a diameter of 139,820 km."
                ),
                Question(
                    category = "Science",
                    questionText = "What is the chemical symbol for gold?",
                    option1 = "Go",
                    option2 = "Gd",
                    option3 = "Au",
                    option4 = "Ag",
                    correctAnswer = 2,
                    explanation = "Au comes from the Latin word 'aurum' meaning gold."
                ),
                Question(
                    category = "Geography",
                    questionText = "What is the capital of Australia?",
                    option1 = "Sydney",
                    option2 = "Melbourne",
                    option3 = "Canberra",
                    option4 = "Perth",
                    correctAnswer = 2,
                    explanation = "Canberra is the capital city of Australia, located in the Australian Capital Territory."
                ),
                Question(
                    category = "History",
                    questionText = "In which year did World War II end?",
                    option1 = "1944",
                    option2 = "1945",
                    option3 = "1946",
                    option4 = "1947",
                    correctAnswer = 1,
                    explanation = "World War II ended in 1945 with the surrender of Japan in September."
                ),
                Question(
                    category = "Entertainment",
                    questionText = "Who directed the movie 'Inception'?",
                    option1 = "Steven Spielberg",
                    option2 = "Christopher Nolan",
                    option3 = "Martin Scorsese",
                    option4 = "Quentin Tarantino",
                    correctAnswer = 1,
                    explanation = "Christopher Nolan directed Inception, released in 2010."
                )
            )
            repository.insertQuestions(sampleQuestions)
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _categories.value = repository.getAllCategories()
        }
    }

    fun startQuiz(category: String) {
        viewModelScope.launch {
            val questions = repository.getQuestionsByCategory(category)
            _uiState.value = _uiState.value.copy(
                currentCategory = category,
                questions = questions,
                currentQuestionIndex = 0,
                score = 0,
                selectedAnswers = mutableListOf(),
                isQuizActive = true,
                startTime = System.currentTimeMillis()
            )
        }
    }

    fun selectAnswer(answerIndex: Int) {
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex < currentState.questions.size) {
            _uiState.value = currentState.copy(selectedAnswer = answerIndex)
        }
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        val currentQuestion = currentState.questions[currentState.currentQuestionIndex]

        // Update score if answer is correct
        val newScore = if (currentState.selectedAnswer == currentQuestion.correctAnswer) {
            currentState.score + 1
        } else {
            currentState.score
        }

        // Add selected answer to list
        val newSelectedAnswers = currentState.selectedAnswers.toMutableList()
        newSelectedAnswers.add(currentState.selectedAnswer)

        if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
            // Move to next question
            _uiState.value = currentState.copy(
                currentQuestionIndex = currentState.currentQuestionIndex + 1,
                score = newScore,
                selectedAnswers = newSelectedAnswers,
                selectedAnswer = -1
            )
        } else {
            // Quiz finished
            finishQuiz(newScore, newSelectedAnswers)
        }
    }

    private fun finishQuiz(finalScore: Int, selectedAnswers: List<Int>) {
        val currentState = _uiState.value
        val endTime = System.currentTimeMillis()
        val timeSpent = endTime - currentState.startTime

        _uiState.value = currentState.copy(
            score = finalScore,
            selectedAnswers = selectedAnswers,
            isQuizActive = false,
            isQuizCompleted = true
        )

        // Save quiz result
        viewModelScope.launch {
            val result = QuizResult(
                category = currentState.currentCategory,
                score = finalScore,
                totalQuestions = currentState.questions.size,
                timeSpent = timeSpent
            )
            repository.insertQuizResult(result)
        }
    }

    fun resetQuiz() {
        _uiState.value = QuizUiState()
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}

data class QuizUiState(
    val currentCategory: String = "",
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val selectedAnswer: Int = -1,
    val selectedAnswers: List<Int> = emptyList(),
    val isQuizActive: Boolean = false,
    val isQuizCompleted: Boolean = false,
    val startTime: Long = 0L
)