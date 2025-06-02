package com.example.brainbuzz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userManager = UserManager.getInstance(this)
        setupViews()
    }

    private fun setupViews() {
        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val tvSignup = findViewById<TextView>(R.id.tv_signup)
        val tvForgotPassword = findViewById<TextView>(R.id.tv_forgot_password)

        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Login button
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (validateInput(email, password)) {
                performLogin(email, password)
            }
        }

        // Signup link
        tvSignup.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Forgot password (placeholder)
        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot password feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun performLogin(email: String, password: String) {
        val result = userManager.loginUser(email, password)

        when (result) {
            UserManager.LoginResult.SUCCESS -> {
                val user = userManager.getCurrentUser()
                Toast.makeText(this, "Welcome back, ${user?.username}!", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }
            UserManager.LoginResult.INVALID_CREDENTIALS -> {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
            UserManager.LoginResult.USER_NOT_FOUND -> {
                Toast.makeText(this, "Account not found", Toast.LENGTH_SHORT).show()
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