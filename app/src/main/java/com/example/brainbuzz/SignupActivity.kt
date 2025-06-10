package com.example.brainbuzz

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    private lateinit var userManager: UserManager
    private lateinit var selectedUserType: UserType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        userManager = UserManager.getInstance(this)

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
        val checkboxTerms = findViewById<CheckBox>(R.id.checkbox_terms)
        val btnSignup = findViewById<Button>(R.id.btn_create_account)
        val tvLogin = findViewById<TextView>(R.id.tv_login)

        supportActionBar?.title =
            "Sign Up as ${selectedUserType.name.lowercase().replaceFirstChar { it.uppercase() }}"

        btnBack.setOnClickListener { finish() }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        fun updateButtonState() {
            val email = etEmail.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()
            val agreed = checkboxTerms.isChecked

            btnSignup.isEnabled = email.isNotEmpty() &&
                    username.isNotEmpty() &&
                    password.isNotEmpty() &&
                    confirmPassword.isNotEmpty() &&
                    password == confirmPassword &&
                    agreed
        }

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = updateButtonState()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        etEmail.addTextChangedListener(watcher)
        etUsername.addTextChangedListener(watcher)
        etPassword.addTextChangedListener(watcher)
        etConfirmPassword.addTextChangedListener(watcher)
        checkboxTerms.setOnCheckedChangeListener { _, _ -> updateButtonState() }

        btnSignup.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (validateInput(etEmail, etUsername, etPassword, etConfirmPassword)) {
                performSignup(email, username, password)
            }
        }
    }

    private fun validateInput(
        etEmail: EditText,
        etUsername: EditText,
        etPassword: EditText,
        etConfirmPassword: EditText
    ): Boolean {
        var isValid = true

        val email = etEmail.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        etEmail.error = null
        etUsername.error = null
        etPassword.error = null
        etConfirmPassword.error = null

        if (email.isEmpty()) {
            etEmail.error = "Please enter your email"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Invalid email format"
            isValid = false
        }

        if (username.isEmpty()) {
            etUsername.error = "Please enter a username"
            isValid = false
        } else if (username.length < 3) {
            etUsername.error = "Username must be at least 3 characters"
            isValid = false
        }

        if (password.isEmpty()) {
            etPassword.error = "Please enter a password"
            isValid = false
        } else if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }

    private fun performSignup(email: String, username: String, password: String) {
        val result = userManager.registerUser(username, email, password, selectedUserType)

        when (result) {
            RegisterResult.SUCCESS -> {
                navigateToMain()
            }
            RegisterResult.USER_EXISTS -> {
                findViewById<EditText>(R.id.et_email).error =
                    "Email or username already exists"
            }
            RegisterResult.USERNAME_TOO_SHORT -> {
                findViewById<EditText>(R.id.et_username).error =
                    "Username must be at least 3 characters"
            }
            RegisterResult.PASSWORD_TOO_SHORT -> {
                findViewById<EditText>(R.id.et_password).error =
                    "Password must be at least 6 characters"
            }
            RegisterResult.INVALID_EMAIL -> {
                findViewById<EditText>(R.id.et_email).error =
                    "Please enter a valid email address"
            }
            RegisterResult.FAILED -> {
                Toast.makeText(this, "Unexpected error. Try again.", Toast.LENGTH_SHORT).show()
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
