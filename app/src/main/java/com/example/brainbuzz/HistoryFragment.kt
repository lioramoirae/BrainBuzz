package com.example.brainbuzz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class HistoryFragment : Fragment() {

    private lateinit var userManager: UserManager
    private lateinit var localUserManager: LocalUserManager
    private lateinit var historyListView: ListView
    private lateinit var historyAdapter: HistoryListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Use your existing history layout
        return inflater.inflate(R.layout.activity_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userManager = UserManager.getInstance(requireContext())
        localUserManager = LocalUserManager(requireContext())

        setupListView(view)
        setupViews(view)
        loadHistory()
    }

    private fun setupListView(view: View) {
        val historyContainer = view.findViewById<LinearLayout>(R.id.history_container)

        historyListView = ListView(requireContext())
        historyAdapter = HistoryListAdapter(requireContext(), emptyList())
        historyListView.adapter = historyAdapter
        historyListView.dividerHeight = 1

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        historyListView.layoutParams = layoutParams

        historyContainer.removeAllViews()
        historyContainer.addView(historyListView)
    }

    private fun setupViews(view: View) {
        // Hide back button in fragment
        view.findViewById<Button>(R.id.btn_back)?.visibility = View.GONE

        view.findViewById<Button>(R.id.btn_clear_history)?.setOnClickListener {
            showClearHistoryDialog()
        }

        view.findViewById<Button>(R.id.btn_export_history)?.setOnClickListener {
            exportHistory()
        }

        updateUserStats(view)
    }

    private fun updateUserStats(view: View) {
        val user = userManager.getCurrentUser()
        user?.let { currentUser ->
            view.findViewById<TextView>(R.id.tv_user_name)?.text =
                if (currentUser.isGuest) "Guest User" else currentUser.username

            view.findViewById<TextView>(R.id.tv_user_type)?.text = when (currentUser.userType) {
                UserType.CASUAL -> "Casual User"
                UserType.STUDENT -> "Student"
                UserType.COMPETITIVE -> "Competitive User"
            }

            view.findViewById<TextView>(R.id.tv_total_score)?.text =
                "Total Score: ${currentUser.totalScore}"

            view.findViewById<TextView>(R.id.tv_total_quizzes)?.text =
                "Quizzes: ${currentUser.quizzesCompleted}"

            view.findViewById<TextView>(R.id.tv_average_score)?.text =
                "Average: ${"%.1f".format(currentUser.averageScore)}"
        }
    }

    private fun loadHistory() {
        val user = userManager.getCurrentUser()

        if (user?.isGuest == true) {
            showNoHistoryMessage()
            return
        }

        val quizResults = userManager.getQuizHistory(20)

        if (quizResults.isNotEmpty()) {
            historyListView.visibility = View.VISIBLE
            historyAdapter.updateData(quizResults)
        } else {
            showNoHistoryMessage()
        }
    }

    private fun showNoHistoryMessage() {
        historyListView.visibility = View.GONE

        val noHistoryText = TextView(requireContext()).apply {
            text = "📊 No quiz history yet!\n\nComplete some quizzes to see your progress here."
            textSize = 16f
            gravity = android.view.Gravity.CENTER
            setPadding(32, 64, 32, 64)
            setTextColor(requireContext().getColor(android.R.color.darker_gray))
        }

        val historyContainer = view?.findViewById<LinearLayout>(R.id.history_container)
        historyContainer?.removeAllViews()
        historyContainer?.addView(noHistoryText)
    }

    private fun showClearHistoryDialog() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Clear History")
            .setMessage("This will permanently delete all your quiz history.")
            .setPositiveButton("Clear") { _, _ ->
                clearHistory()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearHistory() {
        val user = userManager.getCurrentUser()
        if (user != null && !user.isGuest) {
            // Use the resetUserStatistics method from UserManager
            val success = userManager.resetUserStatistics()

            if (success) {
                // Update UI
                historyAdapter.updateData(emptyList())
                view?.let { updateUserStats(it) }
                showNoHistoryMessage()

                Toast.makeText(context, "History cleared successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to clear history", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportHistory() {
        val user = userManager.getCurrentUser()
        if (user?.isGuest == true) {
            Toast.makeText(context, "Export not available for guest users", Toast.LENGTH_SHORT).show()
            return
        }

        val quizResults = userManager.getQuizHistory(100)

        if (quizResults.isEmpty()) {
            Toast.makeText(context, "No history to export", Toast.LENGTH_SHORT).show()
            return
        }

        // Create export data
        val exportData = StringBuilder()
        exportData.append("BrainBuzz Quiz History Export\n")
        exportData.append("User: ${user?.username}\n")
        exportData.append("Export Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n\n")

        quizResults.forEach { result ->
            exportData.append("Category: ${result.category}\n")
            exportData.append("Score: ${result.score}/${result.maxScore} (${if (result.maxScore > 0) (result.score * 100) / result.maxScore else 0}%)\n")
            exportData.append("Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(result.dateTaken))}\n")
            exportData.append("---\n")
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Export History")
            .setMessage("Export data ready:\n\n${exportData.toString().take(500)}...")
            .setPositiveButton("OK", null)
            .show()

        Toast.makeText(context, "History export ready", Toast.LENGTH_SHORT).show()
    }
}