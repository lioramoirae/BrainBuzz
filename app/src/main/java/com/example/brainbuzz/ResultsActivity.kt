package com.example.brainbuzz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class ResultsActivity : AppCompatActivity() {

    private lateinit var textViewScore: TextView
    private lateinit var textViewPercentage: TextView
    private lateinit var textViewMessage: TextView
    private lateinit var buttonPlayAgain: Button
    private lateinit var buttonHome: Button
    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        userManager = UserManager.getInstance(this)

        initializeViews()
        displayResults()
        setupClickListeners()
        saveQuizResult()
    }

    private fun initializeViews() {
        textViewScore = findViewById(R.id.textViewScore)
        textViewPercentage = findViewById(R.id.textViewPercentage)
        textViewMessage = findViewById(R.id.textViewMessage)
        buttonPlayAgain = findViewById(R.id.buttonPlayAgain)
        buttonHome = findViewById(R.id.buttonHome)
    }

    private fun displayResults() {
        val score = intent.getIntExtra("score", 0)
        val total = intent.getIntExtra("total", 0)
        val category = intent.getStringExtra("category") ?: "Unknown"

        val percentage = if (total > 0) (score * 100) / total else 0

        textViewScore.text = "$score/$total"
        textViewPercentage.text = "$percentage%"

        // Use the category in the message (fixes the warning)
        val message = when {
            percentage >= 90 -> "Excellent! Outstanding performance in $category!"
            percentage >= 80 -> "Great job! Well done in $category!"
            percentage >= 70 -> "Good work in $category! Keep it up!"
            percentage >= 60 -> "Not bad in $category! Room for improvement."
            else -> "Keep practicing $category! You'll get better!"
        }
        textViewMessage.text = message
    }

    private fun saveQuizResult() {
        val score = intent.getIntExtra("score", 0)
        val total = intent.getIntExtra("total", 0)
        val category = intent.getStringExtra("category") ?: "Unknown"

        // Save using UserManager
        val success = userManager.updateUserStats(score, total, 0, category)

        if (success) {
            // Also save to SharedPreferences for backward compatibility
            saveToSharedPreferences(score, total, category)
        }
    }

    private fun saveToSharedPreferences(score: Int, total: Int, category: String) {
        val percentage = if (total > 0) (score * 100) / total else 0
        val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        val sharedPreferences = getSharedPreferences("quiz_history", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val existingHistory = sharedPreferences.getString("history", "") ?: ""
        val newEntry = "$dateTime|$category|$score/$total|$percentage%"

        val updatedHistory = if (existingHistory.isEmpty()) {
            newEntry
        } else {
            val entries = existingHistory.split("\n").toMutableList()
            entries.add(0, newEntry)
            if (entries.size > 10) {
                entries.subList(0, 10)
            } else {
                entries
            }.joinToString("\n")
        }

        editor.putString("history", updatedHistory)
        editor.apply()
    }

    private fun setupClickListeners() {
        buttonPlayAgain.setOnClickListener {
            val intent = Intent(this, CategoriesActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}