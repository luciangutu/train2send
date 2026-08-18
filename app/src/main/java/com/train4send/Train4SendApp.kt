package com.train4send

import android.app.Application
import com.train4send.data.backup.BackupManager
import com.train4send.data.local.AppDatabase
import com.train4send.data.repository.ExerciseRepository
import com.train4send.data.repository.TrainingPlanRepository
import com.train4send.data.repository.WorkoutLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

        prepopulateDataIfNeeded()
    }

    private fun prepopulateDataIfNeeded() {
        MainScope().launch(Dispatchers.IO) {
            val exercises = exerciseRepository.getAllExercises().first()
            if (exercises.isEmpty()) {
                try {
                    val jsonString = assets.open("demo_climbing_plans.json").bufferedReader().use { it.readText() }
                    BackupManager(this@Train4SendApp).importFromJson(jsonString)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
