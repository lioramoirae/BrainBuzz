package com.example.brainbuzz

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AuthActivity : AppCompatActivity() {

    private lateinit var localUserManager: LocalUserManager
    private var isLoginMode = true

    // UI Elements
    private lateinit var tvTitle: TextView
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnAction: Button
    private lateinit var btnToggleMode: Button
    private lateinit var btnGuest: Button
    private lateinit var spinnerUserType: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        localUserManager = LocalUserManager(this)

        // Check if already logged in
        if (localUserManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        initializeViews()
        setupUI()
        setupClickListeners()
    }

    private fun initializeViews() {
        tvTitle = findViewById(R.id.tv_title)
        etUsername = findViewById(R.id.et_username)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        btnAction = findViewById(R.id.btn_action)
        btnToggleMode = findViewById(R.id.btn_toggle_mode)
        btnGuest = findViewById(R.id.btn_guest)
        spinnerUserType = findViewById(R.id.spinner_user_type)
    }

    private fun setupUI() {
        // Setup user type spinner
        val userTypes = arrayOf("Casual", "Student", "Competitive")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUserType.adapter = adapter

        updateUIForMode()
    }

    private fun updateUIForMode() {
        if (isLoginMode) {
            // Login mode
            tvTitle.text = "🧠 Welcome Back to BrainBuzz!"
            etUsername.hint = "Username or Email"
            etEmail.visibility = android.view.View.GONE
            etConfirmPassword.visibility = android.view.View.GONE
            spinnerUserType.visibility = android.view.View.GONE
            btnAction.text = "Login"
            btnToggleMode.text = "Don't have an account? Sign up"
            btnGuest.text = "🎮 Continue as Guest"
        } else {
            // Signup mode
            tvTitle.text = "🚀 Join BrainBuzz!"
            etUsername.hint = "Choose Username"
            etEmail.visibility = android.view.View.VISIBLE
            etConfirmPassword.visibility = android.view.View.VISIBLE
            spinnerUserType.visibility = android.view.View.VISIBLE
            btnAction.text = "Create Account"
            btnToggleMode.text = "Already have an account? Login"
            btnGuest.text = "🎮 Skip & Continue as Guest"
        }
    }

    private fun setupClickListeners() {
        btnAction.setOnClickListener {
            if (isLoginMode) {
                performLogin()
            } else {
                performSignup()
            }
        }

        btnToggleMode.setOnClickListener {
            isLoginMode = !isLoginMode
            updateUIForMode()
            clearFields()
        }

        btnGuest.setOnClickListener {
            createGuestUser()
        }
    }

    private fun performLogin() {
        val usernameOrEmail = etUsername.text.toString().trim()
        val password = etPassword.text.toString()

        if (usernameOrEmail.isEmpty()) {
            Toast.makeText(this, "Please enter username or email", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
            return
        }

        val result = localUserManager.loginUser(usernameOrEmail, password)

        when (result) {
            LocalUserManager.LoginResult.SUCCESS -> {
                Toast.makeText(this, "Welcome back! 🎉", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }
            LocalUserManager.LoginResult.INVALID_CREDENTIALS -> {
                Toast.makeText(this, "Invalid username/email or password", Toast.LENGTH_LONG).show()
            }
            LocalUserManager.LoginResult.FAILED -> {
                Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performSignup() {
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        // Validation
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedUserType = when (spinnerUserType.selectedItemPosition) {
            0 -> UserType.CASUAL
            1 -> UserType.STUDENT
            2 -> UserType.COMPETITIVE
            else -> UserType.CASUAL
        }

        val result = localUserManager.registerUser(username, email, password, selectedUserType)

        when (result) {
            LocalUserManager.RegisterResult.SUCCESS -> {
                Toast.makeText(this, "Account created successfully! Welcome to BrainBuzz! 🎉", Toast.LENGTH_LONG).show()
                navigateToMain()
            }
            LocalUserManager.RegisterResult.USERNAME_TOO_SHORT -> {
                Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show()
            }
            LocalUserManager.RegisterResult.PASSWORD_TOO_SHORT -> {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            }
            LocalUserManager.RegisterResult.INVALID_EMAIL -> {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            }
            LocalUserManager.RegisterResult.USER_EXISTS -> {
                Toast.makeText(this, "Username or email already exists", Toast.LENGTH_LONG).show()
            }
            LocalUserManager.RegisterResult.FAILED -> {
                Toast.makeText(this, "Failed to create account. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createGuestUser() {
        val selectedUserType = when (spinnerUserType.selectedItemPosition) {
            0 -> UserType.CASUAL
            1 -> UserType.STUDENT
            2 -> UserType.COMPETITIVE
            else -> UserType.CASUAL
        }

        val success = localUserManager.createGuestUser(selectedUserType)

        if (success) {
            Toast.makeText(this, "Welcome, Guest! Your progress will be saved locally. 🎮", Toast.LENGTH_LONG).show()
            navigateToMain()
        } else {
            Toast.makeText(this, "Failed to create guest user. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearFields() {
        etUsername.text.clear()
        etEmail.text.clear()
        etPassword.text.clear()
        etConfirmPassword.text.clear()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}