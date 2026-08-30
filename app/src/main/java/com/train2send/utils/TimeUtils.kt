package com.train2send.utils

import com.train2send.data.model.ExerciseEntity
import com.train2send.data.model.PlannedExerciseEntity

fun formatDuration(totalSeconds: Int): String {
    if (totalSeconds <= 0) return "0s"
    
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val parts = mutableListOf<String>()
    if (hours > 0) parts.add("${hours}h")
    if (minutes > 0) parts.add("${minutes}m")
    if (seconds > 0 || parts.isEmpty()) parts.add("${seconds}s")
    
    return parts.joinToString(" ")
}

/**
 * Formats seconds as a compact countdown timer string: "45", "1:05", "14:40", "1:02:30".
 * Designed for large font timer displays where space is limited.
 */
fun formatCountdown(totalSeconds: Int): String {
    if (totalSeconds <= 0) return "0"

    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        hours > 0 -> "%d:%02d:%02d".format(hours, minutes, seconds)
        minutes > 0 -> "%d:%02d".format(minutes, seconds)
        else -> "$seconds"
    }
}

fun formatDurationRounded(totalSeconds: Int): String {
    if (totalSeconds <= 0) return "0m"
    
    // Round to nearest minute
    val totalMinutes = (totalSeconds + 30) / 60
    val displayMinutes = if (totalMinutes == 0) 1 else totalMinutes
    
    val hours = displayMinutes / 60
    val minutes = displayMinutes % 60
    
    val parts = mutableListOf<String>()
    if (hours > 0) parts.add("${hours}h")
    if (minutes > 0 || (hours == 0)) parts.add("${minutes}m")
    
    return parts.joinToString(" ")
}

fun calculateExerciseDuration(
    sets: Int?,
    reps: Int?,
    workRepSec: Int?,
    restRepSec: Int?,
    restSetSec: Int?
): Int {
    val s = sets ?: 1
    val r = reps ?: 1
    val t = workRepSec ?: 0
    val rr = restRepSec ?: 0
    val rs = restSetSec ?: 0

    if (s <= 0) return 0

    val workPerSet = r * t + (if (r > 1) (r - 1) * rr else 0)
    return s * workPerSet + (s - 1) * rs
}

/**
 * Resolves the effective exercise parameters by coalescing planned custom values
 * with the exercise defaults.
 */
data class ResolvedExerciseParams(
    val sets: Int?,
    val reps: Int?,
    val durationSec: Int?,
    val restSec: Int?,
    val restBetweenSetsSec: Int?
)

fun resolveExerciseParams(
    planned: PlannedExerciseEntity,
    exercise: ExerciseEntity?
): ResolvedExerciseParams = ResolvedExerciseParams(
    sets = planned.customSets ?: exercise?.defaultSets,
    reps = planned.customReps ?: exercise?.defaultReps,
    durationSec = planned.customDurationSec ?: exercise?.defaultDurationSec,
    restSec = planned.customRestSec ?: exercise?.defaultRestSec,
    restBetweenSetsSec = planned.customRestBetweenSetsSec ?: exercise?.defaultRestBetweenSetsSec
)

/**
 * Estimates the total duration in seconds for a list of planned exercises.
 */
fun estimateDuration(
    exercises: List<PlannedExerciseEntity>,
    exerciseMap: Map<String, ExerciseEntity>
): Int {
    if (exercises.isEmpty()) return 0
    return exercises.sumOf { planned ->
        val params = resolveExerciseParams(planned, exerciseMap[planned.exerciseId])
        calculateExerciseDuration(
            sets = params.sets,
            reps = params.reps,
            workRepSec = params.durationSec,
            restRepSec = params.restSec,
            restSetSec = params.restBetweenSetsSec
        )
    }
}
