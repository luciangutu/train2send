package com.train4send.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Exercises : Screen("exercises")
    data object ExerciseCreate : Screen("exercises/create")
    data object ExerciseDetail : Screen("exercises/{exerciseId}") {
        fun createRoute(exerciseId: String) = "exercises/$exerciseId"
    }
    data object WeeklyPlan : Screen("weekly-plan")
    data object PlanList : Screen("plans")
    data object PlanSetup : Screen("plan-setup?planId={planId}") {
        const val route_base = "plan-setup"
        fun createRoute(planId: String? = null): String =
            if (planId != null) "plan-setup?planId=$planId" else "plan-setup"
    }
    data object PlanDayDetail : Screen("plan-day/{planDayId}") {
        fun createRoute(planDayId: String) = "plan-day/$planDayId"
    }
    data object Timer : Screen("timer")
    data object Backup : Screen("backup")
}
