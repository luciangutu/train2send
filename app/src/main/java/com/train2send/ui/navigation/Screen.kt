package com.train2send.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Exercises : Screen("exercises")
    data object ExerciseEdit : Screen("exercises/edit?exerciseId={exerciseId}") {
        fun createRoute(exerciseId: String? = null): String =
            if (exerciseId != null) "exercises/edit?exerciseId=$exerciseId" else "exercises/edit"
    }
    data object ExerciseDetail : Screen("exercises/{exerciseId}?plannedId={plannedId}") {
        fun createRoute(exerciseId: String, plannedId: String? = null) = 
            if (plannedId != null) "exercises/$exerciseId?plannedId=$plannedId" 
            else "exercises/$exerciseId"
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
    data object Timer : Screen("timer?work={work}&restRep={restRep}&reps={reps}&sets={sets}&restSet={restSet}&description={description}") {
        fun createRoute(
            work: Int? = null,
            restRep: Int? = null,
            reps: Int? = null,
            sets: Int? = null,
            restSet: Int? = null,
            description: String? = null
        ): String {
            val params = mutableListOf<String>()
            work?.let { params.add("work=$it") }
            restRep?.let { params.add("restRep=$it") }
            reps?.let { params.add("reps=$it") }
            sets?.let { params.add("sets=$it") }
            restSet?.let { params.add("restSet=$it") }
            description?.let { params.add("description=$it") }
            return if (params.isEmpty()) "timer" else "timer?${params.joinToString("&")}"
        }
    }
    data object Backup : Screen("backup")
}
