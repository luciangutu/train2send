package com.train4send.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "training_plans")
data class TrainingPlanEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
