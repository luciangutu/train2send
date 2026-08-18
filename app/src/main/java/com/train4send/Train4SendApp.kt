package com.train4send

import android.app.Application
import com.train4send.data.local.AppDatabase
import com.train4send.data.repository.ExerciseRepository
import com.train4send.data.repository.TrainingPlanRepository
import com.train4send.data.repository.WorkoutLogRepository

class Train4SendApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var exerciseRepository: ExerciseRepository
        private set

    lateinit var trainingPlanRepository: TrainingPlanRepository
        private set

    lateinit var workoutLogRepository: WorkoutLogRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        exerciseRepository = ExerciseRepository(database.exerciseDao())
        trainingPlanRepository = TrainingPlanRepository(database.trainingPlanDao())
        workoutLogRepository = WorkoutLogRepository(database.workoutLogDao())
    }
}
