package com.train2send.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.train2send.ui.screens.backup.BackupScreen
import com.train2send.ui.screens.exercises.ExerciseEditScreen
import com.train2send.ui.screens.exercises.ExerciseDetailScreen
import com.train2send.ui.screens.exercises.ExerciseListScreen
import com.train2send.ui.screens.home.HomeScreen
import com.train2send.ui.screens.plan.PlanDayDetailScreen
import com.train2send.ui.screens.plan.PlanListScreen
import com.train2send.ui.screens.plan.PlanSetupScreen
import com.train2send.ui.screens.plan.WeeklyPlanScreen
import com.train2send.ui.screens.timer.TimerScreen

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
        composable(
            route = Screen.ExerciseEdit.route,
            arguments = listOf(
                navArgument("exerciseId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId")
            ExerciseEditScreen(exerciseId = exerciseId, navController = navController)
        }
        composable(
            route = "exercises/{exerciseId}",
            arguments = listOf(
                navArgument("exerciseId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
            ExerciseDetailScreen(exerciseId = exerciseId, navController = navController)
        }
        composable(Screen.WeeklyPlan.route) {
            WeeklyPlanScreen(navController = navController)
        }
        composable(Screen.PlanList.route) {
            PlanListScreen(navController = navController)
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
        composable(Screen.Backup.route) {
            BackupScreen(navController = navController)
        }
    }
}
