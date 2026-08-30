package com.train2send.utils

import androidx.compose.ui.graphics.Color
import com.train2send.data.model.ExerciseCategory

/**
 * Parses the category's hex color string into a Compose Color.
 * Returns [Color.Gray] if parsing fails.
 */
val ExerciseCategory.color: Color
    get() = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (_: Exception) {
        Color.Gray
    }
