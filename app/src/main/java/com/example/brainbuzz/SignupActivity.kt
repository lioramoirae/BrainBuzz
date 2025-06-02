package com.example.brainbuzz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    private lateinit var userManager: UserManager
    private lateinit var selectedUserType: UserType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        userManager = UserManager.getInstance(this)

        // Get user type from intent or default to CASUAL
        val userTypeString = intent.getStringExtra("user_type") ?: UserType.CASUAL.name
        selectedUserType = UserType.valueOf(userTypeString)

        setupViews()
    }

    private fun setupViews() {
        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etUsername = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val etConfirmPassword = findViewById<EditText>(R.id.et_confirm_password)
        val btnSignup = findViewById<Button>(R.id.btn_signup)
        val tvLogin = findViewById<TextView>(R.id.tv_login)

        // Optional: Show user type in title or as toast
        supportActionBar?.title = "Sign Up as ${selectedUserType.name.lowercase().replaceFirstChar { it.uppercase() }}"

        // Back button
        btnBack?.setOnClickListener {
            finish()
        }

        // Signup button
        btnSignup?.setOnClickListener {
            // Fixed: Remove redundant Elvis operators
            val email = etEmail?.text?.toString()?.trim() ?: ""
            val username = etUsername?.text?.toString()?.trim() ?: ""
            val password = etPassword?.text?.toString() ?: ""
            val confirmPassword = etConfirmPassword?.text?.toString() ?: ""

            if (validateInput(email, username, password, confirmPassword)) {
                performSignup(email, username, password)
            }
        }

        // Login link
        tvLogin?.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateInput(email: String, username: String, password: String, confirmPassword: String): Boolean {
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return false
        }

        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            return false
        }

        if (username.length < 3) {
            Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun performSignup(email: String, username: String, password: String) {
        val result = userManager.registerUser(email, username, password, selectedUserType)

        when (result) {
            UserManager.RegisterResult.SUCCESS -> {
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }
            UserManager.RegisterResult.EMAIL_EXISTS -> {
                Toast.makeText(this, "An account with this email already exists", Toast.LENGTH_LONG).show()
            }
            UserManager.RegisterResult.USERNAME_TOO_SHORT -> {
                Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show()
            }
            UserManager.RegisterResult.PASSWORD_TOO_SHORT -> {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            }
            UserManager.RegisterResult.FAILED -> {
                Toast.makeText(this, "Failed to create account. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}