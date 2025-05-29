package com.example.brainbuzz

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var buttonBack: Button
    private lateinit var textViewMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        buttonBack = findViewById(R.id.buttonBack)
        textViewMessage = findViewById(R.id.textViewMessage)

        textViewMessage.text = "Settings coming soon!\nYou'll be able to customize your quiz experience here."

        buttonBack.setOnClickListener {
            finish()
        }
    }
}