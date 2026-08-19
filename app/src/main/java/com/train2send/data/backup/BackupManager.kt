package com.train2send.data.backup

import com.train2send.Train2SendApp
import com.train2send.data.model.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

class BackupManager(private val app: Train2SendApp) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Export all app data to a JSON string.
     */
    suspend fun exportToJson(): String {
        val exercises = app.exerciseRepository.getAllExercises().first()
        val plans = app.trainingPlanRepository.getAllPlans().first()

        val planBackups = plans.map { plan ->
            val days = app.trainingPlanRepository.getDaysForPlan(plan.id).first()
            val dayBackups = days.map { day ->
                val plannedExercises = app.trainingPlanRepository.getExercisesForDay(day.id).first()
                PlanDayBackup(
                    id = day.id,
                    dayOfWeek = day.dayOfWeek,
                    dayTitle = day.dayTitle,
                    exercises = plannedExercises.map { pe ->
                        PlannedExerciseBackup(
                            id = pe.id,
                            exerciseId = pe.exerciseId,
                            section = pe.section.name,
                            orderIndex = pe.orderIndex,
                            customSets = pe.customSets,
                            customReps = pe.customReps,
                            customDurationSec = pe.customDurationSec,
                            customRestSec = pe.customRestSec
                        )
                    }
                )
            }
            PlanBackup(
                id = plan.id,
                title = plan.title,
                isActive = plan.isActive,
                createdAt = plan.createdAt,
                days = dayBackups
            )
        }

        val backup = BackupData(
            exercises = exercises.map { ex ->
                ExerciseBackup(
                    id = ex.id,
                    name = ex.name,
                    category = ex.category.name,
                    description = ex.description,
                    defaultSets = ex.defaultSets,
                    defaultReps = ex.defaultReps,
                    defaultDurationSec = ex.defaultDurationSec,
                    defaultRestSec = ex.defaultRestSec
                )
            },
            plans = planBackups
        )

        return json.encodeToString(BackupData.serializer(), backup)
    }

    /**
     * Import data from a JSON string. Merges with existing data (upsert by ID).
     * Returns a summary of what was imported.
     */
    suspend fun importFromJson(jsonString: String): ImportResult {
        val backup = json.decodeFromString(BackupData.serializer(), jsonString)

        var exercisesImported = 0
        var plansImported = 0
        var lastActivePlanId: String? = null

        // Import exercises
        backup.exercises.forEach { exBackup ->
            val category = try {
                ExerciseCategory.valueOf(exBackup.category)
            } catch (_: Exception) {
                ExerciseCategory.STRENGTH // fallback
            }

            app.exerciseRepository.insertExercise(
                ExerciseEntity(
                    id = exBackup.id,
                    name = exBackup.name,
                    category = category,
                    description = exBackup.description,
                    defaultSets = exBackup.defaultSets,
                    defaultReps = exBackup.defaultReps,
                    defaultDurationSec = exBackup.defaultDurationSec,
                    defaultRestSec = exBackup.defaultRestSec
                )
            )
            exercisesImported++
        }

        // Import plans
        backup.plans.forEach { planBackup ->
            app.trainingPlanRepository.insertPlan(
                TrainingPlanEntity(
                    id = planBackup.id,
                    title = planBackup.title,
                    isActive = planBackup.isActive,
                    createdAt = planBackup.createdAt
                )
            )
            
            if (planBackup.isActive) {
                lastActivePlanId = planBackup.id
            }

            planBackup.days.forEach { dayBackup ->
                app.trainingPlanRepository.insertPlanDay(
                    PlanDayEntity(
                        id = dayBackup.id,
                        planId = planBackup.id,
                        dayOfWeek = dayBackup.dayOfWeek,
                        dayTitle = dayBackup.dayTitle
                    )
                )

                dayBackup.exercises.forEach { peBackup ->
                    val section = try {
                        ExerciseSection.valueOf(peBackup.section)
                    } catch (_: Exception) {
                        ExerciseSection.MAIN
                    }

                    app.trainingPlanRepository.insertPlannedExercise(
                        PlannedExerciseEntity(
                            id = peBackup.id,
                            planDayId = dayBackup.id,
                            exerciseId = peBackup.exerciseId,
                            section = section,
                            orderIndex = peBackup.orderIndex,
                            customSets = peBackup.customSets,
                            customReps = peBackup.customReps,
                            customDurationSec = peBackup.customDurationSec,
                            customRestSec = peBackup.customRestSec
                        )
                    )
                }
            }
            plansImported++
        }
        
        // Ensure only one plan is active if any active plans were imported
        lastActivePlanId?.let { id ->
            app.trainingPlanRepository.activatePlan(id)
        }

        return ImportResult(exercisesImported, plansImported)
    }

    data class ImportResult(
        val exercisesImported: Int,
        val plansImported: Int
    )
}
