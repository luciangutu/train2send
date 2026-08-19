package com.train2send.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: ExerciseCategory,
    val description: String? = null,
    val defaultSets: Int? = null,
    val defaultReps: Int? = null,
    val defaultDurationSec: Int? = null,
    val defaultRestSec: Int? = null
)
