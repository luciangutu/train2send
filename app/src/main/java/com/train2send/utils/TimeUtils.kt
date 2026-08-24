package com.train2send.utils

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
