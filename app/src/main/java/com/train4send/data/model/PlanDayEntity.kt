package com.train4send.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "plan_days",
    foreignKeys = [
        ForeignKey(
            entity = TrainingPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planId")]
)
data class PlanDayEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val planId: String,
    val dayOfWeek: Int, // 1 (Mon) to 7 (Sun)
    val dayTitle: String
)
