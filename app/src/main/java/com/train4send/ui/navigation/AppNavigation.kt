package com.train4send.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.train4send.ui.screens.exercises.ExerciseCreateScreen
import com.train4send.ui.screens.exercises.ExerciseListScreen
import com.train4send.ui.screens.history.HistoryScreen
import com.train4send.ui.screens.home.HomeScreen
import com.train4send.ui.screens.plan.PlanDayDetailScreen
import com.train4send.ui.screens.plan.PlanSetupScreen
import com.train4send.ui.screens.plan.WeeklyPlanScreen
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
        composable(Screen.WeeklyPlan.route) {
            WeeklyPlanScreen(navController = navController)
        }
        composable(
            route = "plan-setup?planId={planId}",
            arguments = listOf(
                navArgument("planId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId")
            PlanSetupScreen(planId = planId, navController = navController)
        }
        composable(
            route = "plan-day/{planDayId}",
            arguments = listOf(
                navArgument("planDayId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val planDayId = backStackEntry.arguments?.getString("planDayId") ?: ""
            PlanDayDetailScreen(planDayId = planDayId, navController = navController)
        }
        composable(Screen.Timer.route) {
            TimerScreen(navController = navController)
        }
        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }
    }
}
