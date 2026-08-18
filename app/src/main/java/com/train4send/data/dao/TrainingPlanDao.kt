package com.train4send.data.dao

import androidx.room.*
import com.train4send.data.model.PlanDayEntity
import com.train4send.data.model.PlannedExerciseEntity
import com.train4send.data.model.TrainingPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingPlanDao {

    @Query("SELECT * FROM training_plans ORDER BY createdAt DESC")
    fun getAllPlans(): Flow<List<TrainingPlanEntity>>

    @Query("SELECT * FROM training_plans WHERE isActive = 1 LIMIT 1")
    fun getActivePlan(): Flow<TrainingPlanEntity?>

    @Query("SELECT * FROM training_plans WHERE id = :id")
    suspend fun getPlanById(id: String): TrainingPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: TrainingPlanEntity)

    @Update
    suspend fun updatePlan(plan: TrainingPlanEntity)

    @Query("UPDATE training_plans SET isActive = 0")
    suspend fun deactivateAllPlans()

    @Query("UPDATE training_plans SET isActive = 1 WHERE id = :planId")
    suspend fun setPlanActive(planId: String)

    @Transaction
    suspend fun activatePlan(planId: String) {
        deactivateAllPlans()
        setPlanActive(planId)
    }

    @Delete
    suspend fun deletePlan(plan: TrainingPlanEntity)

    // Plan Days
    @Query("SELECT * FROM plan_days WHERE planId = :planId ORDER BY dayOfWeek ASC")
    fun getDaysForPlan(planId: String): Flow<List<PlanDayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanDay(day: PlanDayEntity)

    @Update
    suspend fun updatePlanDay(day: PlanDayEntity)

    @Delete
    suspend fun deletePlanDay(day: PlanDayEntity)

    // Planned Exercises
    @Query("SELECT * FROM planned_exercises WHERE planDayId = :planDayId ORDER BY orderIndex ASC")
    fun getExercisesForDay(planDayId: String): Flow<List<PlannedExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedExercise(exercise: PlannedExerciseEntity)

    @Update
    suspend fun updatePlannedExercise(exercise: PlannedExerciseEntity)

    @Delete
    suspend fun deletePlannedExercise(exercise: PlannedExerciseEntity)
}
