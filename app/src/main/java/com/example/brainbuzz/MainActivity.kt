package com.example.brainbuzz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userManager = UserManager.getInstance(this)

        // Check if user is logged in
        if (!userManager.isLoggedIn()) {
            navigateToAuth()
            return
        }

        // Show welcome message
        showWelcomeMessage()

        // Setup all buttons
        setupButtons()

        // Update user display
        updateUserDisplay()
    }

    private fun showWelcomeMessage() {
        val user = userManager.getCurrentUser()
        user?.let {
            val message = if (it.isGuest) {
                "Welcome, Guest! 🎮"
            } else {
                "Welcome back, ${it.username}! 👋"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupButtons() {
        // Main Start Quiz Button
        findViewById<Button>(R.id.btn_start_quiz)?.setOnClickListener {
            Toast.makeText(this, "Starting quiz selection...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, CategoriesActivity::class.java))
        }

        // Categories Button (same as start quiz)
        findViewById<Button>(R.id.btn_categories)?.setOnClickListener {
            Toast.makeText(this, "Opening categories...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, CategoriesActivity::class.java))
        }

        // History Button
        findViewById<Button>(R.id.btn_history)?.setOnClickListener {
            val user = userManager.getCurrentUser()
            if (user?.isGuest == true) {
                Toast.makeText(this, "Sign up to view your quiz history! 📊", Toast.LENGTH_LONG).show()
            } else {
                try {
                    startActivity(Intent(this, HistoryActivity::class.java))
                } catch (e: Exception) {
                    Toast.makeText(this, "History feature coming soon! 📈", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Settings Button
        findViewById<Button>(R.id.btn_settings)?.setOnClickListener {
            Toast.makeText(this, "Opening settings...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Guest Signup Button - NEW!
        findViewById<Button>(R.id.btn_guest_signup)?.setOnClickListener {
            showSignupDialog()
        }

        // Guest Features Info Button - NEW!
        findViewById<Button>(R.id.btn_guest_features)?.setOnClickListener {
            showGuestFeaturesDialog()
        }
    }

    private fun updateUserDisplay() {
        val user = userManager.getCurrentUser()
        user?.let { currentUser ->
            // Update user info
            findViewById<TextView>(R.id.tv_user_info)?.text =
                if (currentUser.isGuest) "Playing as Guest" else "Hello, ${currentUser.username}!"

            // Update user stats
            findViewById<TextView>(R.id.tv_user_stats)?.text =
                "Score: ${currentUser.totalScore} | Quizzes: ${currentUser.quizzesCompleted}"

            // Update user type
            findViewById<TextView>(R.id.tv_user_type)?.text = when (currentUser.userType) {
                UserType.CASUAL -> "Casual"
                UserType.STUDENT -> "Student"
                UserType.COMPETITIVE -> "Competitive"
            }

            // Update progress details with correct percentage calculation
            val userStats = userManager.getUserStats()
            findViewById<TextView>(R.id.tv_progress_details)?.text =
                if ((userStats?.totalQuizzes ?: 0) == 0) {
                    "Take your first quiz to see your progress!"
                } else {
                    val percent = ((userStats?.averageScore ?: 0.0) / 3.0) * 100  // assuming max score is set to 3
                    "Average Score: ${"%.0f".format(percent)}% • Total Quizzes: ${userStats?.totalQuizzes ?: 0}"
                }

            // Show/hide guest-specific UI elements
            setupGuestUI(currentUser.isGuest)

            // Show user type specific features
            showUserTypeFeatures(currentUser)
        }
    }

    private fun setupGuestUI(isGuest: Boolean) {
        // Show guest-specific buttons and messages
        val guestSignupButton = findViewById<Button>(R.id.btn_guest_signup)
        val guestFeaturesButton = findViewById<Button>(R.id.btn_guest_features)
        val guestMessage = findViewById<TextView>(R.id.tv_guest_message)

        if (isGuest) {
            // Show guest UI elements
            guestSignupButton?.visibility = View.VISIBLE
            guestFeaturesButton?.visibility = View.VISIBLE
            guestMessage?.visibility = View.VISIBLE
            guestMessage?.text = "🎮 You're in Guest Mode!\nSign up to save your progress and unlock more features!"
        } else {
            // Hide guest UI elements for registered users
            guestSignupButton?.visibility = View.GONE
            guestFeaturesButton?.visibility = View.GONE
            guestMessage?.visibility = View.GONE
        }
    }

    private fun showSignupDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🚀 Create Your Account")
            .setMessage("Sign up to unlock these amazing features:\n\n" +
                    "📊 Save your quiz history\n" +
                    "🏆 Track your progress over time\n" +
                    "🎯 Unlock all user types\n" +
                    "⚙️ Access advanced settings\n" +
                    "📈 View detailed statistics\n\n" +
                    "Your current progress will be saved!")
            .setPositiveButton("Sign Up Now") { _, _ ->
                navigateToSignup()
            }
            .setNegativeButton("Maybe Later") { _, _ ->
                Toast.makeText(this, "You can sign up anytime from Settings! 😊", Toast.LENGTH_LONG).show()
            }
            .setNeutralButton("Change User Type") { _, _ ->
                navigateToAuth()
            }
            .show()
    }

    private fun showGuestFeaturesDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🎮 Guest Mode Features")
            .setMessage("As a Guest, you can:\n\n" +
                    "✅ Take unlimited quizzes\n" +
                    "✅ See your current session score\n" +
                    "✅ Access all quiz categories\n" +
                    "✅ Use basic settings\n\n" +
                    "⚠️ Limitations:\n" +
                    "❌ No permanent progress saving\n" +
                    "❌ No quiz history\n" +
                    "❌ Limited user type features\n\n" +
                    "💡 Sign up to unlock everything!")
            .setPositiveButton("Sign Up Now") { _, _ ->
                navigateToSignup()
            }
            .setNegativeButton("Continue as Guest") { _, _ ->
                Toast.makeText(this, "Enjoy your quizzes! 🎯", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun navigateToSignup() {
        val intent = Intent(this, SignupActivity::class.java)
        // Pre-select the guest's current user type
        val user = userManager.getCurrentUser()
        intent.putExtra("user_type", user?.userType?.name ?: UserType.CASUAL.name)
        startActivity(intent)
    }

    private fun showUserTypeFeatures(user: User) {
        val featureMessage = when (user.userType) {
            UserType.STUDENT -> "📚 Student features: Study mode with detailed explanations!"
            UserType.COMPETITIVE -> "🏆 Competitive features: Leaderboards and challenges!"
            UserType.CASUAL -> if (user.isGuest) "🎮 Guest mode: Quick and easy quizzes!" else "🎮 Casual mode: Fun learning experience!"
        }

        // Show this as a toast after a short delay so it doesn't overlap with welcome
        findViewById<TextView>(R.id.tv_progress_details)?.postDelayed({
            Toast.makeText(this, featureMessage, Toast.LENGTH_LONG).show()
        }, 1500)
    }

    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Update user display when returning to main
        if (userManager.isLoggedIn()) {
            updateUserDisplay()
        }
    }
}