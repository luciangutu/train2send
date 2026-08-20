package com.train2send.data.repository

import com.train2send.data.dao.TrainingPlanDao
import com.train2send.data.model.PlanDayEntity
import com.train2send.data.model.PlannedExerciseEntity
import com.train2send.data.model.TrainingPlanEntity
import kotlinx.coroutines.flow.Flow

class TrainingPlanRepository(private val planDao: TrainingPlanDao) {

    fun getAllPlans(): Flow<List<TrainingPlanEntity>> =
        planDao.getAllPlans()

    fun getActivePlan(): Flow<TrainingPlanEntity?> =
        planDao.getActivePlan()

    suspend fun getPlanById(id: String): TrainingPlanEntity? =
        planDao.getPlanById(id)

    suspend fun insertPlan(plan: TrainingPlanEntity) =
        planDao.insertPlan(plan)

    suspend fun updatePlan(plan: TrainingPlanEntity) =
        planDao.updatePlan(plan)

    suspend fun activatePlan(planId: String) =
        planDao.activatePlan(planId)

    suspend fun deletePlan(plan: TrainingPlanEntity) =
        planDao.deletePlan(plan)

    // Days
    fun getDaysForPlan(planId: String): Flow<List<PlanDayEntity>> =
        planDao.getDaysForPlan(planId)

    suspend fun insertPlanDay(day: PlanDayEntity) =
        planDao.insertPlanDay(day)

    suspend fun updatePlanDay(day: PlanDayEntity) =
        planDao.updatePlanDay(day)

    suspend fun deletePlanDay(day: PlanDayEntity) =
        planDao.deletePlanDay(day)

    // Planned Exercises
    fun getExercisesForDay(planDayId: String): Flow<List<PlannedExerciseEntity>> =
        planDao.getExercisesForDay(planDayId)

    suspend fun getPlannedExerciseById(id: String): PlannedExerciseEntity? =
        planDao.getPlannedExerciseById(id)

    suspend fun insertPlannedExercise(exercise: PlannedExerciseEntity) =
        planDao.insertPlannedExercise(exercise)

    suspend fun updatePlannedExercise(exercise: PlannedExerciseEntity) =
        planDao.updatePlannedExercise(exercise)

    suspend fun deletePlannedExercise(exercise: PlannedExerciseEntity) =
        planDao.deletePlannedExercise(exercise)
}
