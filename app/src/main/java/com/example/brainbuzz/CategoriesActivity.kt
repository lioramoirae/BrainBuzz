package com.example.brainbuzz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CategoriesActivity : AppCompatActivity() {

    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        userManager = UserManager.getInstance(this)

        setupViews()
        showUserTypeFeatures()
    }

    private fun setupViews() {
        // Back button
        findViewById<Button>(R.id.btn_back)?.setOnClickListener {
            finish()
        }

        // Category buttons - these will start your existing QuizActivity
        findViewById<Button>(R.id.btn_science)?.setOnClickListener {
            startQuiz("Science")
        }

        findViewById<Button>(R.id.btn_geography)?.setOnClickListener {
            startQuiz("Geography")
        }

        findViewById<Button>(R.id.btn_history)?.setOnClickListener {
            startQuiz("History")
        }

        findViewById<Button>(R.id.btn_entertainment)?.setOnClickListener {
            startQuiz("Entertainment")
        }

        // Show user info
        updateUserDisplay()
    }

    private fun startQuiz(category: String) {
        val user = userManager.getCurrentUser()

        // Show user-specific message
        val message = when (user?.userType) {
            UserType.STUDENT -> "Starting $category quiz in study mode!"
            UserType.COMPETITIVE -> "Starting $category quiz - beat your best score!"
            else -> "Starting $category quiz!"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        // Start your existing QuizActivity with the selected category
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("category", category)
        startActivity(intent)
    }

    private fun showUserTypeFeatures() {
        val user = userManager.getCurrentUser()
        user?.let {
            val featureMessage = when (it.userType) {
                UserType.STUDENT -> "📚 Study Mode: You'll see explanations for wrong answers!"
                UserType.COMPETITIVE -> "🏆 Competitive Mode: Your scores are saved to leaderboards!"
                UserType.CASUAL -> if (it.isGuest) "🎮 Guest Mode: Have fun with quick quizzes!" else "🎮 Casual Mode: Enjoy learning!"
            }
            Toast.makeText(this, featureMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUserDisplay() {
        val user = userManager.getCurrentUser()
        user?.let { currentUser ->
            // Update welcome message
            findViewById<TextView>(R.id.tv_welcome)?.text =
                if (currentUser.isGuest) "Welcome, Guest!" else "Welcome, ${currentUser.username}!"

            // Update user type display
            findViewById<TextView>(R.id.tv_user_type)?.text = when (currentUser.userType) {
                UserType.CASUAL -> "Casual User"
                UserType.STUDENT -> "Student"
                UserType.COMPETITIVE -> "Competitive User"
            }

            // Update stats
            findViewById<TextView>(R.id.tv_user_stats)?.text =
                "Total Score: ${currentUser.totalScore} | Quizzes: ${currentUser.quizzesCompleted}"
        }
    }
}