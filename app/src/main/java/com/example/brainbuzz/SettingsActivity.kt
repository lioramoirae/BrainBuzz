package com.example.brainbuzz

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : AppCompatActivity() {

    private lateinit var userManager: UserManager
    private lateinit var sharedPrefs: SharedPreferences

    // UI Elements
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvTotalScore: TextView
    private lateinit var tvQuizzesCompleted: TextView
    private lateinit var tvAverageScore: TextView
    private lateinit var spinnerUserType: Spinner
    private lateinit var spinnerFontSize: Spinner
    private lateinit var switchNotifications: Switch
    private lateinit var switchDarkMode: Switch
    private lateinit var switchSound: Switch
    private lateinit var btnChangePassword: Button
    private lateinit var btnSignupFromSettings: Button
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        userManager = UserManager.getInstance(this)
        sharedPrefs = getSharedPreferences("app_settings", MODE_PRIVATE)

        initializeViews()
        setupSpinners()
        loadUserData()
        loadSettings()
        setupClickListeners()
        setupToggleListeners()
    }

    private fun initializeViews() {
        // User info
        tvUserName = findViewById(R.id.tv_user_name)
        tvUserEmail = findViewById(R.id.tv_user_email)
        tvTotalScore = findViewById(R.id.tv_total_score)
        tvQuizzesCompleted = findViewById(R.id.tv_quizzes_completed)
        tvAverageScore = findViewById(R.id.tv_average_score)

        // Spinners
        spinnerUserType = findViewById(R.id.spinner_user_type)
        spinnerFontSize = findViewById(R.id.spinner_font_size)

        // Switches
        switchNotifications = findViewById(R.id.switch_notifications)
        switchDarkMode = findViewById(R.id.switch_dark_mode)
        switchSound = findViewById(R.id.switch_sound)

        // Buttons
        btnChangePassword = findViewById(R.id.btn_change_password)
        btnSignupFromSettings = findViewById(R.id.btn_signup_from_settings)
        btnLogout = findViewById(R.id.btn_logout)
    }

    private fun setupSpinners() {
        // User Type Spinner
        val userTypes = arrayOf("Casual", "Student", "Competitive")
        val userTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userTypes)
        userTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUserType.adapter = userTypeAdapter

        // Font Size Spinner
        val fontSizes = arrayOf("Small", "Medium", "Large", "Extra Large")
        val fontSizeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fontSizes)
        fontSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFontSize.adapter = fontSizeAdapter

        // User Type Change Listener
        spinnerUserType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedUserType = when (position) {
                    0 -> UserType.CASUAL
                    1 -> UserType.STUDENT
                    2 -> UserType.COMPETITIVE
                    else -> UserType.CASUAL
                }
                updateUserType(selectedUserType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Font Size Change Listener
        spinnerFontSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                saveFontSizeSetting(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadUserData() {
        val user = userManager.getCurrentUser()
        user?.let { currentUser ->
            if (currentUser.isGuest) {
                // Guest user display
                tvUserName.text = "Guest User"
                tvUserEmail.text = "Playing in Guest Mode"
                tvUserEmail.setTextColor(getColor(android.R.color.darker_gray))

                // Show signup button for guests - FIXED
                btnSignupFromSettings.visibility = View.VISIBLE
                btnSignupFromSettings.isEnabled = true
                btnSignupFromSettings.isClickable = true
                btnChangePassword.visibility = View.GONE

                // Debug log
                println("DEBUG: Guest user detected, signup button should be visible and clickable")

                // Set user type without triggering listener
                val userTypePosition = when (currentUser.userType) {
                    UserType.CASUAL -> 0
                    UserType.STUDENT -> 1
                    UserType.COMPETITIVE -> 2
                }
                spinnerUserType.setSelection(userTypePosition, false)
            } else {
                // Registered user display
                tvUserName.text = currentUser.username
                tvUserEmail.text = currentUser.email
                tvUserEmail.setTextColor(getColor(android.R.color.black))

                // Show password change for registered users
                btnSignupFromSettings.visibility = View.GONE
                btnChangePassword.visibility = View.VISIBLE
                btnChangePassword.isEnabled = true
                btnChangePassword.isClickable = true

                // Set user type without triggering listener
                val userTypePosition = when (currentUser.userType) {
                    UserType.CASUAL -> 0
                    UserType.STUDENT -> 1
                    UserType.COMPETITIVE -> 2
                }
                spinnerUserType.setSelection(userTypePosition, false)
            }

            // Update stats
            tvTotalScore.text = currentUser.totalScore.toString()
            tvQuizzesCompleted.text = currentUser.quizzesCompleted.toString()
            tvAverageScore.text = String.format("%.1f", currentUser.averageScore)
        }
    }

    private fun loadSettings() {
        // Load saved settings
        switchNotifications.isChecked = sharedPrefs.getBoolean("notifications_enabled", true)
        switchDarkMode.isChecked = sharedPrefs.getBoolean("dark_mode_enabled", false)
        switchSound.isChecked = sharedPrefs.getBoolean("sound_enabled", true)

        val fontSizePosition = sharedPrefs.getInt("font_size_position", 1) // Default to Medium
        spinnerFontSize.setSelection(fontSizePosition, false)
    }

    private fun setupClickListeners() {
        // Back button
        findViewById<Button>(R.id.buttonBack).setOnClickListener {
            finish()
        }

        // Change password
        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        // Signup from settings (for guests) - FIXED
        btnSignupFromSettings.setOnClickListener {
            Toast.makeText(this, "Opening signup...", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, SignupActivity::class.java)
            val user = userManager.getCurrentUser()
            intent.putExtra("user_type", user?.userType?.name ?: UserType.CASUAL.name)

            try {
                startActivity(intent)
            } catch (e: Exception) {
                // Fallback to AuthActivity if SignupActivity doesn't exist
                val authIntent = Intent(this, AuthActivity::class.java)
                startActivity(authIntent)
            }
        }

        // Logout
        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun setupToggleListeners() {
        // Notifications toggle
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            showSettingUpdateToast("Notifications", isChecked)
        }

        // Dark mode toggle
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("dark_mode_enabled", isChecked).apply()
            applyDarkMode(isChecked)
            showSettingUpdateToast("Dark Mode", isChecked)
        }

        // Sound toggle
        switchSound.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("sound_enabled", isChecked).apply()
            showSettingUpdateToast("Sound Effects", isChecked)
        }
    }

    private fun updateUserType(newUserType: UserType) {
        val user = userManager.getCurrentUser()
        if (user != null && user.userType != newUserType) {
            val success = userManager.updateUserType(newUserType)
            if (success) {
                Toast.makeText(this, "User type updated to ${newUserType.name.lowercase().replaceFirstChar { it.uppercase() }}", Toast.LENGTH_SHORT).show()

                // Show user type specific message
                showUserTypeFeatures(newUserType)
            } else {
                Toast.makeText(this, "Failed to update user type", Toast.LENGTH_SHORT).show()
                // Revert spinner to previous selection
                loadUserData()
            }
        }
    }

    private fun showUserTypeFeatures(userType: UserType) {
        val message = when (userType) {
            UserType.CASUAL -> "🎮 Casual Mode: Enjoy fun, relaxed quizzes at your own pace!"
            UserType.STUDENT -> "📚 Student Mode: Access detailed explanations and study features!"
            UserType.COMPETITIVE -> "🏆 Competitive Mode: Challenge yourself with leaderboards and time limits!"
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun saveFontSizeSetting(position: Int) {
        sharedPrefs.edit().putInt("font_size_position", position).apply()
        val fontSizeNames = arrayOf("Small", "Medium", "Large", "Extra Large")
        Toast.makeText(this, "Font size: ${fontSizeNames[position]}", Toast.LENGTH_SHORT).show()
    }

    private fun applyDarkMode(enabled: Boolean) {
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun showSettingUpdateToast(settingName: String, enabled: Boolean) {
        val status = if (enabled) "enabled" else "disabled"
        Toast.makeText(this, "$settingName $status", Toast.LENGTH_SHORT).show()
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null)

        AlertDialog.Builder(this)
            .setTitle("🔒 Change Password")
            .setMessage("This feature will allow you to update your password securely.\n\nComing soon in the next update!")
            .setPositiveButton("OK", null)
            .setNeutralButton("Reset Password") { _, _ ->
                Toast.makeText(this, "Password reset email sent! (Feature coming soon)", Toast.LENGTH_LONG).show()
            }
            .show()
    }

    private fun showLogoutDialog() {
        val user = userManager.getCurrentUser()
        val message = if (user?.isGuest == true) {
            "Are you sure you want to exit guest mode?\n\nYour current session progress will be lost."
        } else {
            "Are you sure you want to logout?\n\nYour progress is saved and you can login anytime."
        }

        AlertDialog.Builder(this)
            .setTitle("🚪 Logout")
            .setMessage(message)
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // Clear user session
        userManager.logout()

        // Clear any cached data if needed
        val editor = sharedPrefs.edit()
        // Keep app settings but clear user-specific data
        editor.remove("last_login")
        editor.apply()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate back to authentication
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Refresh user data when returning to settings
        loadUserData()
    }
}