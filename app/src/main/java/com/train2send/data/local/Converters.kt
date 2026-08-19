package com.train2send.data.local

import androidx.room.TypeConverter
import com.train2send.data.model.ExerciseCategory
import com.train2send.data.model.ExerciseSection

class Converters {

    @TypeConverter
    fun fromExerciseCategory(value: ExerciseCategory): String = value.name

    @TypeConverter
    fun toExerciseCategory(value: String): ExerciseCategory = ExerciseCategory.valueOf(value)

    @TypeConverter
    fun fromExerciseSection(value: ExerciseSection): String = value.name

    @TypeConverter
    fun toExerciseSection(value: String): ExerciseSection = ExerciseSection.valueOf(value)
}
