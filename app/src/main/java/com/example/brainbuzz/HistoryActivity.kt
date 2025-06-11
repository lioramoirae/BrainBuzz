package com.example.brainbuzz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HistoryActivity : AppCompatActivity() {

    private lateinit var userManager: UserManager
    private lateinit var historyContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        userManager = UserManager.getInstance(this)

        setupViews()
        loadHistory()
    }

    private fun setupViews() {
        historyContainer = findViewById(R.id.history_container)

        // Back button
        findViewById<Button>(R.id.btn_back)?.setOnClickListener {
            finish()
        }

        // Clear history button
        findViewById<Button>(R.id.btn_clear_history)?.setOnClickListener {
            showClearHistoryDialog()
        }

        // Export history button
        findViewById<Button>(R.id.btn_export_history)?.setOnClickListener {
            exportHistory()
        }

        updateUserStats()
    }

    private fun updateUserStats() {
        val user = userManager.getCurrentUser()
        user?.let { currentUser ->
            findViewById<TextView>(R.id.tv_user_name)?.text =
                if (currentUser.isGuest) "Guest User" else currentUser.username

            findViewById<TextView>(R.id.tv_total_score)?.text =
                "Total Score: ${currentUser.totalScore}"

            findViewById<TextView>(R.id.tv_total_quizzes)?.text =
                "Quizzes Completed: ${currentUser.quizzesCompleted}"

            findViewById<TextView>(R.id.tv_average_score)?.text =
                "Average Score: ${"%.1f".format(currentUser.averageScore)}"

            findViewById<TextView>(R.id.tv_user_type)?.text = when (currentUser.userType) {
                UserType.CASUAL -> "Casual User"
                UserType.STUDENT -> "Student"
                UserType.COMPETITIVE -> "Competitive User"
            }
        }
    }

    private fun loadHistory() {
        val user = userManager.getCurrentUser()

        if (user?.isGuest == true) {
            showGuestMessage()
            return
        }

        // Try to get history from UserManager first (for registered users)
        val quizResults = userManager.getQuizHistory(20)

        if (quizResults.isNotEmpty()) {
            displayQuizResults(quizResults)
        } else {
            // Fallback to SharedPreferences for backward compatibility
            loadFromSharedPreferences()
        }
    }

    private fun displayQuizResults(quizResults: List<QuizResult>) {
        // Add header
        val headerView = createHeaderView("📊 Your Quiz History")
        historyContainer.addView(headerView)

        // Add each quiz result
        quizResults.forEach { result ->
            val historyCard = createQuizResultCard(result)
            historyContainer.addView(historyCard)
        }
    }

    private fun createQuizResultCard(result: QuizResult): LinearLayout {
        val cardLayout = LinearLayout(this)
        cardLayout.orientation = LinearLayout.VERTICAL
        cardLayout.setBackgroundColor(getColor(android.R.color.white))
        cardLayout.setPadding(20, 16, 20, 16)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 8, 16, 8)
        cardLayout.layoutParams = layoutParams

        // Category title
        val titleText = TextView(this)
        titleText.text = "${result.category} Quiz"
        titleText.textSize = 16f
        titleText.setTypeface(null, android.graphics.Typeface.BOLD)
        titleText.setTextColor(getColor(android.R.color.black))
        cardLayout.addView(titleText)

        // Score
        val percentage = if (result.maxScore > 0) (result.score * 100) / result.maxScore else 0
        val scoreDisplayText = TextView(this)
        scoreDisplayText.text = "Score: ${result.score}/${result.maxScore} ($percentage%)"
        scoreDisplayText.textSize = 14f
        scoreDisplayText.setTextColor(getScoreColor(percentage.toDouble()))
        cardLayout.addView(scoreDisplayText)

        // Date
        val dateDisplayText = TextView(this)
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        dateDisplayText.text = "Date: ${dateFormat.format(java.util.Date(result.dateTaken))}"
        dateDisplayText.textSize = 12f
        dateDisplayText.setTextColor(getColor(android.R.color.darker_gray))
        cardLayout.addView(dateDisplayText)

        return cardLayout
    }

    private fun loadFromSharedPreferences() {
        // Load from SharedPreferences as fallback
        val sharedPreferences = getSharedPreferences("quiz_history", MODE_PRIVATE)
        val historyString = sharedPreferences.getString("history", "") ?: ""

        if (historyString.isNotEmpty()) {
            val headerView = createHeaderView("📊 Your Quiz History")
            historyContainer.addView(headerView)

            val historyEntries = historyString.split("\n")
            historyEntries.forEach { entry ->
                if (entry.isNotEmpty()) {
                    val historyCard = createHistoryCard(entry)
                    historyContainer.addView(historyCard)
                }
            }
        } else {
            showNoHistoryMessage()
        }
    }

    private fun createHeaderView(title: String): TextView {
        val headerView = TextView(this)
        headerView.text = title
        headerView.textSize = 18f
        headerView.setTypeface(null, android.graphics.Typeface.BOLD)
        headerView.setTextColor(getColor(android.R.color.black))
        headerView.setPadding(16, 24, 16, 16)
        return headerView
    }

    private fun createHistoryCard(entry: String): LinearLayout {
        val parts = entry.split("|")
        if (parts.size < 4) return LinearLayout(this) // Invalid entry

        val dateTime = parts[0]
        val category = parts[1]
        val scoreText = parts[2]
        val percentageText = parts[3]

        // Create card container
        val cardLayout = LinearLayout(this)
        cardLayout.orientation = LinearLayout.VERTICAL
        cardLayout.setBackgroundColor(getColor(android.R.color.white))
        cardLayout.setPadding(20, 16, 20, 16)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 8, 16, 8)
        cardLayout.layoutParams = layoutParams

        // Category title
        val titleText = TextView(this)
        titleText.text = "$category Quiz"
        titleText.textSize = 16f
        titleText.setTypeface(null, android.graphics.Typeface.BOLD)
        titleText.setTextColor(getColor(android.R.color.black))
        cardLayout.addView(titleText)

        // Score
        val scoreDisplayText = TextView(this)
        scoreDisplayText.text = "Score: $scoreText ($percentageText)"
        scoreDisplayText.textSize = 14f

        // Extract percentage for color
        val percentage = percentageText.replace("%", "").toDoubleOrNull() ?: 0.0
        scoreDisplayText.setTextColor(getScoreColor(percentage))
        cardLayout.addView(scoreDisplayText)

        // Date
        val dateDisplayText = TextView(this)
        dateDisplayText.text = "Date: $dateTime"
        dateDisplayText.textSize = 12f
        dateDisplayText.setTextColor(getColor(android.R.color.darker_gray))
        cardLayout.addView(dateDisplayText)

        return cardLayout
    }

    private fun showNoHistoryMessage() {
        val noHistoryText = TextView(this)
        noHistoryText.text = "📚 No quiz history yet!\n\nTake your first quiz to see your progress here."
        noHistoryText.textSize = 16f
        noHistoryText.gravity = android.view.Gravity.CENTER
        noHistoryText.setTextColor(getColor(android.R.color.darker_gray))
        noHistoryText.setPadding(32, 64, 32, 64)
        historyContainer.addView(noHistoryText)
    }

    private fun showGuestMessage() {
        val guestText = TextView(this)
        guestText.text = "🎮 Guest Mode\n\nSign up to save and view your quiz history!\n\nGuest sessions don't save permanent history."
        guestText.textSize = 16f
        guestText.gravity = android.view.Gravity.CENTER
        guestText.setTextColor(getColor(android.R.color.darker_gray))
        guestText.setPadding(32, 64, 32, 64)
        historyContainer.addView(guestText)

        // Add sign up button
        val signUpButton = Button(this)
        signUpButton.text = "Sign Up to Save History"
        signUpButton.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }

        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        buttonParams.gravity = android.view.Gravity.CENTER_HORIZONTAL
        buttonParams.setMargins(32, 16, 32, 32)
        signUpButton.layoutParams = buttonParams

        historyContainer.addView(signUpButton)
    }

    private fun showClearHistoryDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Clear History")
            .setMessage("This will permanently delete all your quiz history. This action cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                clearHistory()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearHistory() {
        // Clear SharedPreferences history
        val sharedPreferences = getSharedPreferences("quiz_history", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Clear user stats (reset to 0)
        val user = userManager.getCurrentUser()
        if (user != null && !user.isGuest) {
            userManager.resetUserStatistics()
        }

        // Refresh display
        historyContainer.removeAllViews()
        updateUserStats()
        loadHistory()

        Toast.makeText(this, "History cleared successfully", Toast.LENGTH_SHORT).show()
    }

    private fun exportHistory() {
        val user = userManager.getCurrentUser()

        if (user?.isGuest == true) {
            Toast.makeText(this, "History export not available for guest users", Toast.LENGTH_SHORT).show()
            return
        }

        // Get all history data
        val quizHistory = userManager.getQuizHistory(50)
        val sharedPrefs = getSharedPreferences("quiz_history", MODE_PRIVATE)
        val sharedHistory = sharedPrefs.getString("history", "") ?: ""

        val exportText = buildString {
            append("BrainBuzz Quiz History Export\n")
            append("Generated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n\n")

            user?.let {
                append("User: ${it.username}\n")
                append("User Type: ${when(it.userType) {
                    UserType.CASUAL -> "Casual"
                    UserType.STUDENT -> "Student"
                    UserType.COMPETITIVE -> "Competitive"
                }}\n")
                append("Total Score: ${it.totalScore}\n")
                append("Total Quizzes: ${it.quizzesCompleted}\n")
                append("Average Score: ${"%.1f".format(it.averageScore)}\n\n")
            }

            if (quizHistory.isNotEmpty()) {
                append("Database Quiz History:\n")
                quizHistory.forEach { result ->
                    val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(result.dateTaken))
                    val percentage = if (result.maxScore > 0) (result.score * 100) / result.maxScore else 0
                    append("$date | ${result.category} | ${result.score}/${result.maxScore} | $percentage%\n")
                }
                append("\n")
            }

            if (sharedHistory.isNotEmpty()) {
                append("Legacy Quiz History:\n")
                sharedHistory.split("\n").forEach { entry ->
                    if (entry.isNotEmpty()) {
                        append("$entry\n")
                    }
                }
            }

            if (quizHistory.isEmpty() && sharedHistory.isEmpty()) {
                append("No quiz history found.\n")
            }
        }

        // Show export dialog
        android.app.AlertDialog.Builder(this)
            .setTitle("Quiz History Export")
            .setMessage(exportText)
            .setPositiveButton("OK", null)
            .setNegativeButton("Share") { _, _ ->
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, exportText)
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My BrainBuzz Quiz History")
                startActivity(Intent.createChooser(shareIntent, "Share Quiz History"))
            }
            .show()
    }

    private fun getScoreColor(percentage: Double): Int {
        return when {
            percentage >= 90 -> getColor(android.R.color.holo_green_dark)
            percentage >= 70 -> getColor(android.R.color.holo_orange_dark)
            percentage >= 50 -> getColor(android.R.color.holo_blue_dark)
            else -> getColor(android.R.color.holo_red_dark)
        }
    }
}