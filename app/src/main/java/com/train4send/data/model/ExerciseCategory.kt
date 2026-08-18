package com.train4send.data.model

enum class ExerciseCategory(val label: String, val colorHex: String) {
    STRENGTH("Strength", "#E53935"),
    POWER("Power", "#8E24AA"),
    POWER_ENDURANCE("Power Endurance", "#FB8C00"),
    ENDURANCE("Endurance", "#1E88E5"),
    MOBILITY("Mobility & Recovery", "#4CAF50"),
    CONDITIONING("Antagonist & Core", "#00ACC1")
}
