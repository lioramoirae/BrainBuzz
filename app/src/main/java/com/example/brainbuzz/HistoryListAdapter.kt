package com.example.brainbuzz

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class HistoryListAdapter(
    private val context: Context,
    private var quizResults: List<QuizResult>
) : BaseAdapter() {

    override fun getCount(): Int = quizResults.size
    override fun getItem(position: Int): Any = quizResults[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.item_history_simple, parent, false
        )

        val result = quizResults[position]

        view.findViewById<TextView>(R.id.textViewCategory).text = result.category
        view.findViewById<TextView>(R.id.textViewScore).text = "Score: ${result.score}/${result.maxScore}"

        val percentage = if (result.maxScore > 0) (result.score * 100) / result.maxScore else 0
        val percentageView = view.findViewById<TextView>(R.id.textViewPercentage)
        percentageView.text = "$percentage%"

        val backgroundColor = when {
            percentage >= 90 -> Color.parseColor("#4CAF50") // Green
            percentage >= 70 -> Color.parseColor("#FF9800") // Orange
            percentage >= 50 -> Color.parseColor("#2196F3") // Blue
            else -> Color.parseColor("#F44336") // Red
        }
        percentageView.setBackgroundColor(backgroundColor)

        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        view.findViewById<TextView>(R.id.textViewDate).text = dateFormat.format(Date(result.dateTaken))

        return view
    }

    fun updateData(newResults: List<QuizResult>) {
        quizResults = newResults
        notifyDataSetChanged()
    }
}