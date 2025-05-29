package com.example.brainbuzz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class CategoriesActivity : AppCompatActivity() {

    private lateinit var buttonScience: Button
    private lateinit var buttonGeography: Button
    private lateinit var buttonHistory: Button
    private lateinit var buttonEntertainment: Button
    private lateinit var buttonBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        buttonScience = findViewById(R.id.buttonScience)
        buttonGeography = findViewById(R.id.buttonGeography)
        buttonHistory = findViewById(R.id.buttonHistory)
        buttonEntertainment = findViewById(R.id.buttonEntertainment)
        buttonBack = findViewById(R.id.buttonBack)

        buttonScience.setOnClickListener {
            startQuiz("Science")
        }

        buttonGeography.setOnClickListener {
            startQuiz("Geography")
        }

        buttonHistory.setOnClickListener {
            startQuiz("History")
        }

        buttonEntertainment.setOnClickListener {
            startQuiz("Entertainment")
        }

        buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun startQuiz(category: String) {
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("category", category)
        startActivity(intent)
    }
}