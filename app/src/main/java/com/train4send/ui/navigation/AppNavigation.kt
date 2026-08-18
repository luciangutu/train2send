package com.train4send.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.train4send.ui.screens.exercises.ExerciseCreateScreen
import com.train4send.ui.screens.exercises.ExerciseListScreen
import com.train4send.ui.screens.history.HistoryScreen
import com.train4send.ui.screens.home.HomeScreen
import com.train4send.ui.screens.timer.TimerScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Exercises.route) {
            ExerciseListScreen(navController = navController)
        }
        composable(Screen.ExerciseCreate.route) {
            ExerciseCreateScreen(navController = navController)
        }
        composable(Screen.Timer.route) {
            TimerScreen(navController = navController)
        }
        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }
    }
}
