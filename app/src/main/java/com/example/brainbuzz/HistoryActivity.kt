package com.example.brainbuzz

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HistoryActivity : AppCompatActivity() {

    private lateinit var buttonBack: Button
    private lateinit var textViewMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        buttonBack = findViewById(R.id.buttonBack)
        textViewMessage = findViewById(R.id.textViewMessage)

        loadQuizHistory()

        buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun loadQuizHistory() {
        val sharedPreferences = getSharedPreferences("quiz_history", MODE_PRIVATE)
        val history = sharedPreferences.getString("history", "") ?: ""

        if (history.isEmpty()) {
            textViewMessage.text = "No quiz history yet.\nComplete some quizzes to see your results here!"
        } else {
            val entries = history.split("\n")
            val formattedHistory = StringBuilder("Recent Quiz Results:\n\n")

            entries.forEach { entry ->
                val parts = entry.split("|")
                if (parts.size == 4) {
                    val date = parts[0]
                    val category = parts[1]
                    val score = parts[2]
                    val percentage = parts[3]

                    formattedHistory.append("📅 $date\n")
                    formattedHistory.append("📚 $category\n")
                    formattedHistory.append("📊 $score ($percentage)\n\n")
                }
            }

            textViewMessage.text = formattedHistory.toString()
        }
    }
}