package com.train2send.data.repository

import com.train2send.data.dao.ExerciseDao
import com.train2send.data.model.ExerciseCategory
import com.train2send.data.model.ExerciseEntity
import kotlinx.coroutines.flow.Flow

class ExerciseRepository(private val exerciseDao: ExerciseDao) {

    fun getAllExercises(): Flow<List<ExerciseEntity>> =
        exerciseDao.getAllExercises()

    fun getExercisesByCategory(category: ExerciseCategory): Flow<List<ExerciseEntity>> =
        exerciseDao.getExercisesByCategory(category)

    fun getExerciseById(id: String): Flow<ExerciseEntity?> =
        exerciseDao.getExerciseById(id)

    suspend fun insertExercise(exercise: ExerciseEntity) =
        exerciseDao.insertExercise(exercise)

    suspend fun updateExercise(exercise: ExerciseEntity) =
        exerciseDao.updateExercise(exercise)

    suspend fun deleteExercise(exercise: ExerciseEntity) =
        exerciseDao.deleteExercise(exercise)
}
