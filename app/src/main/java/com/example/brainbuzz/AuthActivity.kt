package com.example.brainbuzz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AuthActivity : AppCompatActivity() {

    private lateinit var userManager: UserManager
    private var selectedUserType: UserType = UserType.CASUAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        userManager = UserManager.getInstance(this)

        // Check if already logged in
        if (userManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        setupViews()
        updateSelectedType()
    }

    private fun setupViews() {
        // User type selection cards - using LinearLayout instead of CardView
        findViewById<LinearLayout>(R.id.card_casual)?.setOnClickListener {
            selectedUserType = UserType.CASUAL
            updateSelectedType()
            Toast.makeText(this, "Selected: Casual User", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.card_student)?.setOnClickListener {
            selectedUserType = UserType.STUDENT
            updateSelectedType()
            Toast.makeText(this, "Selected: Student", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.card_competitive)?.setOnClickListener {
            selectedUserType = UserType.COMPETITIVE
            updateSelectedType()
            Toast.makeText(this, "Selected: Competitive User", Toast.LENGTH_SHORT).show()
        }

        // Action buttons
        findViewById<Button>(R.id.btn_continue_guest)?.setOnClickListener {
            if (selectedUserType == UserType.CASUAL) {
                continueAsGuest()
            } else {
                Toast.makeText(this, "Guest mode only available for Casual users", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_signup)?.setOnClickListener {
            navigateToSignup()
        }

        findViewById<Button>(R.id.btn_login)?.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun updateSelectedType() {
        val tvSelectedType = findViewById<TextView>(R.id.tv_selected_type)
        val tvTypeDescription = findViewById<TextView>(R.id.tv_type_description)
        val btnContinueGuest = findViewById<Button>(R.id.btn_continue_guest)
        val btnSignup = findViewById<Button>(R.id.btn_signup)

        val (typeName, description) = when (selectedUserType) {
            UserType.CASUAL -> "Casual User" to "Play quick quizzes during free time"
            UserType.STUDENT -> "Student" to "Practice general knowledge for learning"
            UserType.COMPETITIVE -> "Competitive User" to "Track scores and challenge friends"
        }

        tvSelectedType?.text = "Selected: $typeName"
        tvTypeDescription?.text = description

        // Show/hide guest button based on user type
        btnContinueGuest?.visibility = if (selectedUserType == UserType.CASUAL) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }

        // Update signup button text
        btnSignup?.text = "Sign Up as $typeName"
    }

    private fun continueAsGuest() {
        userManager.continueAsGuest()
        Toast.makeText(this, "Welcome, Guest!", Toast.LENGTH_SHORT).show()
        navigateToMain()
    }

    private fun navigateToSignup() {
        val intent = Intent(this, SignupActivity::class.java)
        intent.putExtra("user_type", selectedUserType.name)
        startActivity(intent)
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}