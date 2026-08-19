package com.train2send.data.repository

import com.train2send.data.dao.WorkoutLogDao
import com.train2send.data.model.WorkoutLogEntity
import kotlinx.coroutines.flow.Flow

class WorkoutLogRepository(private val logDao: WorkoutLogDao) {

    fun getAllLogs(): Flow<List<WorkoutLogEntity>> =
        logDao.getAllLogs()

    fun getLogsForExercise(exerciseId: String): Flow<List<WorkoutLogEntity>> =
        logDao.getLogsForExercise(exerciseId)

    fun getLogsBetween(startTime: Long, endTime: Long): Flow<List<WorkoutLogEntity>> =
        logDao.getLogsBetween(startTime, endTime)

    suspend fun insertLog(log: WorkoutLogEntity) =
        logDao.insertLog(log)

    suspend fun deleteLog(log: WorkoutLogEntity) =
        logDao.deleteLog(log)
}
