package com.train4send.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Exercises : Screen("exercises")
    data object ExerciseCreate : Screen("exercises/create")
    data object Plans : Screen("plans")
    data object PlanDetail : Screen("plans/{planId}") {
        fun createRoute(planId: String) = "plans/$planId"
    }
    data object Timer : Screen("timer")
    data object History : Screen("history")
}
