package com.example.brainbuzz.utils.extension

import androidx.compose.ui.graphics.Color

object QuizColors {
    val Primary = Color(0xFF6A1B9A)
    val Secondary = Color(0xFF9C27B0)
    val Accent = Color(0xFFBB86FC)
    val Light = Color(0xFFE1BEE7)
    val Background = Color(0xFFF5F5F5)
    val Success = Color(0xFF4CAF50)
    val Error = Color(0xFFFF5252)
}

fun Int.toPercentage(total: Int): Int = (this * 100) / total

fun String.capitalize(): String =
    this.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
