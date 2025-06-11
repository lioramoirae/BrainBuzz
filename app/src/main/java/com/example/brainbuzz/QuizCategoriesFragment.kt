package com.example.brainbuzz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class QuizCategoriesFragment : Fragment() {

    private lateinit var userManager: UserManager
    private var onCategorySelected: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Use your existing activity_categories.xml layout
        return inflater.inflate(R.layout.activity_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userManager = UserManager.getInstance(requireContext())
        setupViews(view)
        updateUserDisplay(view)
    }

    private fun setupViews(view: View) {
        // Hide/remove back button since we're in a fragment
        view.findViewById<Button>(R.id.btn_back)?.visibility = View.GONE

        // Category buttons - reuse your existing IDs
        view.findViewById<Button>(R.id.btn_science)?.setOnClickListener {
            selectCategory("Science")
        }

        view.findViewById<Button>(R.id.btn_geography)?.setOnClickListener {
            selectCategory("Geography")
        }

        view.findViewById<Button>(R.id.btn_history)?.setOnClickListener {
            selectCategory("History")
        }

        view.findViewById<Button>(R.id.btn_entertainment)?.setOnClickListener {
            selectCategory("Entertainment")
        }
    }

    private fun selectCategory(category: String) {
        val user = userManager.getCurrentUser()

        val message = when (user?.userType) {
            UserType.STUDENT -> "Starting $category quiz in study mode!"
            UserType.COMPETITIVE -> "Starting $category quiz - beat your best score!"
            else -> "Starting $category quiz!"
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

        onCategorySelected?.invoke(category)
    }

    private fun updateUserDisplay(view: View) {
        val user = userManager.getCurrentUser()
        user?.let { currentUser ->
            view.findViewById<TextView>(R.id.tv_welcome)?.text =
                if (currentUser.isGuest) "Welcome, Guest!" else "Welcome, ${currentUser.username}!"

            view.findViewById<TextView>(R.id.tv_user_type)?.text = when (currentUser.userType) {
                UserType.CASUAL -> "Casual User"
                UserType.STUDENT -> "Student"
                UserType.COMPETITIVE -> "Competitive User"
            }

            view.findViewById<TextView>(R.id.tv_user_stats)?.text =
                "Total Score: ${currentUser.totalScore} | Quizzes: ${currentUser.quizzesCompleted}"
        }
    }

    fun setCategorySelectionListener(listener: (String) -> Unit) {
        onCategorySelected = listener
    }
}
