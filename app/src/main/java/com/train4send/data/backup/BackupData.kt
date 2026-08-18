package com.train4send.data.backup

import kotlinx.serialization.Serializable

/**
 * Top-level JSON structure for full app backup.
 */
@Serializable
data class BackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val exercises: List<ExerciseBackup> = emptyList(),
    val plans: List<PlanBackup> = emptyList()
)

@Serializable
data class ExerciseBackup(
    val id: String,
    val name: String,
    val category: String,
    val description: String? = null,
    val defaultSets: Int? = null,
    val defaultReps: Int? = null,
    val defaultDurationSec: Int? = null,
    val defaultRestSec: Int? = null
)

@Serializable
data class PlanBackup(
    val id: String,
    val title: String,
    val isActive: Boolean,
    val createdAt: Long,
    val days: List<PlanDayBackup> = emptyList()
)

@Serializable
data class PlanDayBackup(
    val id: String,
    val dayOfWeek: Int,
    val dayTitle: String,
    val exercises: List<PlannedExerciseBackup> = emptyList()
)

@Serializable
data class PlannedExerciseBackup(
    val id: String,
    val exerciseId: String,
    val section: String,
    val orderIndex: Int = 0,
    val customSets: Int? = null,
    val customReps: Int? = null,
    val customDurationSec: Int? = null,
    val customRestSec: Int? = null
)
