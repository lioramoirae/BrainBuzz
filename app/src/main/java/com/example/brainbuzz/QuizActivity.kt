package com.example.brainbuzz

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class QuizActivity : AppCompatActivity() {

    private lateinit var textViewQuestion: TextView
    private lateinit var textViewQuestionNumber: TextView
    private lateinit var textViewCategory: TextView
    private lateinit var textViewFeedback: TextView  // NEW: For showing feedback
    private lateinit var buttonOption1: Button
    private lateinit var buttonOption2: Button
    private lateinit var buttonOption3: Button
    private lateinit var buttonOption4: Button
    private lateinit var buttonNext: Button
    private lateinit var buttonBack: Button

    private var currentQuestionIndex = 0
    private var selectedAnswer = -1
    private var score = 0
    private var category = ""
    private lateinit var questions: List<Question>

    // Simple data class for questions
    data class Question(
        val questionText: String,
        val option1: String,
        val option2: String,
        val option3: String,
        val option4: String,
        val correctAnswer: Int,
        val explanation: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        category = intent.getStringExtra("category") ?: "Science"

        initializeViews()
        loadQuestions()
        displayQuestion()
        setupClickListeners()
    }

    private fun initializeViews() {
        textViewQuestion = findViewById(R.id.textViewQuestion)
        textViewQuestionNumber = findViewById(R.id.textViewQuestionNumber)
        textViewCategory = findViewById(R.id.textViewCategory)
        textViewFeedback = findViewById(R.id.textViewFeedback)  // NEW: Add this to your layout
        buttonOption1 = findViewById(R.id.buttonOption1)
        buttonOption2 = findViewById(R.id.buttonOption2)
        buttonOption3 = findViewById(R.id.buttonOption3)
        buttonOption4 = findViewById(R.id.buttonOption4)
        buttonNext = findViewById(R.id.buttonNext)
        buttonBack = findViewById(R.id.buttonBack)

        textViewCategory.text = category
        buttonNext.isEnabled = false
        textViewFeedback.visibility = View.GONE  // Hide feedback initially
    }

    private fun loadQuestions() {
        questions = when (category) {
            "Science" -> listOf(
                Question(
                    "What is the largest planet in our solar system?",
                    "Earth", "Jupiter", "Saturn", "Neptune",
                    1, "Jupiter is the largest planet."
                ),
                Question(
                    "What is the chemical symbol for gold?",
                    "Go", "Gd", "Au", "Ag",
                    2, "Au comes from the Latin word 'aurum'."
                ),
                Question(
                    "What gas do plants absorb during photosynthesis?",
                    "Oxygen", "Nitrogen", "Carbon Dioxide", "Hydrogen",
                    2, "Plants absorb CO2 and release oxygen."
                )
            )
            "Geography" -> listOf(
                Question(
                    "What is the capital of Australia?",
                    "Sydney", "Melbourne", "Canberra", "Perth",
                    2, "Canberra is the capital of Australia."
                ),
                Question(
                    "Which is the longest river in the world?",
                    "Amazon", "Nile", "Mississippi", "Yangtze",
                    1, "The Nile River is about 6,650 km long."
                ),
                Question(
                    "What is the smallest country in the world?",
                    "Monaco", "San Marino", "Vatican City", "Liechtenstein",
                    2, "Vatican City is 0.17 square miles."
                )
            )
            "History" -> listOf(
                Question(
                    "In which year did World War II end?",
                    "1944", "1945", "1946", "1947",
                    1, "WWII ended in 1945."
                ),
                Question(
                    "Who was the first person on the Moon?",
                    "Buzz Aldrin", "Neil Armstrong", "John Glenn", "Alan Shepard",
                    1, "Neil Armstrong on July 20, 1969."
                ),
                Question(
                    "Which wonder was in Alexandria?",
                    "Hanging Gardens", "Colossus", "Lighthouse", "Temple",
                    2, "The Lighthouse of Alexandria."
                )
            )
            "Entertainment" -> listOf(
                Question(
                    "Who directed 'Inception'?",
                    "Spielberg", "Christopher Nolan", "Scorsese", "Tarantino",
                    1, "Christopher Nolan directed Inception."
                ),
                Question(
                    "Which movie won Best Picture in 2020?",
                    "1917", "Joker", "Parasite", "Once Upon a Time",
                    2, "Parasite won Best Picture."
                ),
                Question(
                    "Highest-grossing film of all time?",
                    "Titanic", "Avatar", "Avengers Endgame", "Star Wars",
                    1, "Avatar with over $2.9 billion."
                )
            )
            else -> listOf()
        }
    }

    private fun setupClickListeners() {
        buttonOption1.setOnClickListener { selectAnswer(0) }
        buttonOption2.setOnClickListener { selectAnswer(1) }
        buttonOption3.setOnClickListener { selectAnswer(2) }
        buttonOption4.setOnClickListener { selectAnswer(3) }

        buttonNext.setOnClickListener { nextQuestion() }
        buttonBack.setOnClickListener { finish() }
    }

    private fun displayQuestion() {
        if (currentQuestionIndex < questions.size) {
            val question = questions[currentQuestionIndex]

            textViewQuestion.text = question.questionText
            textViewQuestionNumber.text = "${currentQuestionIndex + 1}/${questions.size}"

            buttonOption1.text = question.option1
            buttonOption2.text = question.option2
            buttonOption3.text = question.option3
            buttonOption4.text = question.option4

            resetButtonColors()
            clearFeedback()  // NEW: Clear feedback when showing new question
            enableAnswerButtons()  // NEW: Re-enable buttons
            selectedAnswer = -1
            buttonNext.isEnabled = false
        }
    }

    private fun selectAnswer(answerIndex: Int) {
        selectedAnswer = answerIndex
        resetButtonColors()

        val question = questions[currentQuestionIndex]
        val isCorrect = answerIndex == question.correctAnswer

        // Color the selected answer
        val selectedButton = when (answerIndex) {
            0 -> buttonOption1
            1 -> buttonOption2
            2 -> buttonOption3
            else -> buttonOption4
        }

        selectedButton.setBackgroundColor(
            if (isCorrect) getColor(android.R.color.holo_green_light)
            else getColor(android.R.color.holo_red_light)
        )

        // Show correct answer if wrong
        if (!isCorrect) {
            val correctButton = when (question.correctAnswer) {
                0 -> buttonOption1
                1 -> buttonOption2
                2 -> buttonOption3
                else -> buttonOption4
            }
            correctButton.setBackgroundColor(getColor(android.R.color.holo_green_light))
        }

        // FIXED: Replace Toast with inline feedback
        showFeedback(isCorrect, question.explanation)

        if (isCorrect) {
            score++
        }

        // Disable buttons to prevent multiple selections
        disableAnswerButtons()

        buttonNext.isEnabled = true
        buttonNext.text = if (currentQuestionIndex < questions.size - 1) "Next" else "Finish"
    }

    // NEW: Show feedback in TextView instead of Toast
    private fun showFeedback(isCorrect: Boolean, explanation: String) {
        textViewFeedback.visibility = View.VISIBLE
        textViewFeedback.text = if (isCorrect) {
            "✓ Correct! $explanation"
        } else {
            "✗ Wrong! $explanation"
        }
        textViewFeedback.setTextColor(
            if (isCorrect) getColor(android.R.color.holo_green_dark)
            else getColor(android.R.color.holo_red_dark)
        )
    }

    // NEW: Clear feedback
    private fun clearFeedback() {
        textViewFeedback.visibility = View.GONE
        textViewFeedback.text = ""
    }

    // NEW: Disable answer buttons after selection
    private fun disableAnswerButtons() {
        buttonOption1.isEnabled = false
        buttonOption2.isEnabled = false
        buttonOption3.isEnabled = false
        buttonOption4.isEnabled = false
    }

    // NEW: Enable answer buttons for new question
    private fun enableAnswerButtons() {
        buttonOption1.isEnabled = true
        buttonOption2.isEnabled = true
        buttonOption3.isEnabled = true
        buttonOption4.isEnabled = true
    }

    private fun resetButtonColors() {
        val defaultColor = getColor(android.R.color.white)
        buttonOption1.setBackgroundColor(defaultColor)
        buttonOption2.setBackgroundColor(defaultColor)
        buttonOption3.setBackgroundColor(defaultColor)
        buttonOption4.setBackgroundColor(defaultColor)
    }

    private fun nextQuestion() {
        currentQuestionIndex++

        if (currentQuestionIndex < questions.size) {
            displayQuestion()
        } else {
            // Quiz finished - go to results
            val intent = Intent(this, ResultsActivity::class.java)
            intent.putExtra("score", score)
            intent.putExtra("total", questions.size)
            intent.putExtra("category", category)
            startActivity(intent)
            finish()
        }
    }
}