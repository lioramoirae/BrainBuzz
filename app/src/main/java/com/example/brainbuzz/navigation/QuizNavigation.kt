package com.example.brainbuzz.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.brainbuzz.ui.screens.*
import com.example.brainbuzz.viewmodel.QuizViewModel

@Composable
fun QuizNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val quizResults by viewModel.quizResults.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "onboarding"
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onNavigateToWelcome = {
                    navController.navigate("welcome") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("welcome") {
            WelcomeScreen(
                onNavigateToCategories = { navController.navigate("categories") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToHistory = { navController.navigate("history") }
            )
        }

        composable("categories") {
            CategoriesScreen(
                categories = categories,
                onCategorySelected = { category ->
                    viewModel.startQuiz(category)
                    navController.navigate("quiz")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("quiz") {
            QuizScreen(
                uiState = uiState,
                onAnswerSelected = viewModel::selectAnswer,
                onNextQuestion = viewModel::nextQuestion,
                onNavigateToResults = { navController.navigate("results") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("results") {
            ResultsScreen(
                uiState = uiState,
                onPlayAgain = {
                    viewModel.resetQuiz()
                    navController.navigate("categories") {
                        popUpTo("welcome") { inclusive = false }
                    }
                },
                onNavigateHome = {
                    viewModel.resetQuiz()
                    navController.navigate("welcome") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        composable("history") {
            HistoryScreen(
                quizResults = quizResults,
                onNavigateBack = { navController.popBackStack() },
                onClearHistory = viewModel::clearHistory
            )
        }

        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}