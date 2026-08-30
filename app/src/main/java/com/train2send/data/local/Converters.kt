package com.train2send.data.local

import androidx.room.TypeConverter
import com.train2send.data.model.ClimbingType
import com.train2send.data.model.ExerciseCategory
import com.train2send.data.model.ExerciseSection

class Converters {

    @TypeConverter
    fun fromExerciseCategory(value: ExerciseCategory): String = value.name

    @TypeConverter
    fun toExerciseCategory(value: String): ExerciseCategory =
        try { ExerciseCategory.valueOf(value) } catch (_: Exception) { ExerciseCategory.STRENGTH }

    @TypeConverter
    fun fromExerciseSection(value: ExerciseSection): String = value.name

    @TypeConverter
    fun toExerciseSection(value: String): ExerciseSection =
        try { ExerciseSection.valueOf(value) } catch (_: Exception) { ExerciseSection.MAIN }

    @TypeConverter
    fun fromClimbingType(value: ClimbingType): String = value.name

    @TypeConverter
    fun toClimbingType(value: String): ClimbingType =
        try { ClimbingType.valueOf(value) } catch (_: Exception) { ClimbingType.ANY }
}
