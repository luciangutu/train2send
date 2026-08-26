package com.train2send.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "planned_exercises",
    foreignKeys = [
        ForeignKey(
            entity = PlanDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["planDayId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planDayId"), Index("exerciseId")]
)
data class PlannedExerciseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val planDayId: String,
    val exerciseId: String,
    val section: ExerciseSection,
    val alternativeGroupId: String? = null,
    val isSelected: Boolean = true,
    val orderIndex: Int = 0,
    val notes: String? = null,
    val customSets: Int? = null,
    val customReps: Int? = null,
    val customDurationSec: Int? = null,
    val customRestSec: Int? = null,
    val customRestBetweenSetsSec: Int? = null
)
