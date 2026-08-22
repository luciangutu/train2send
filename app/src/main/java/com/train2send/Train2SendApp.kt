package com.train2send

import android.app.Application
import com.train2send.data.backup.BackupManager
import com.train2send.data.local.AppDatabase
import com.train2send.data.repository.ExerciseRepository
import com.train2send.data.repository.TrainingPlanRepository
import com.train2send.data.repository.UserPreferencesRepository
import com.train2send.data.repository.WorkoutLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class Train2SendApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var exerciseRepository: ExerciseRepository
        private set

    lateinit var trainingPlanRepository: TrainingPlanRepository
        private set

    lateinit var workoutLogRepository: WorkoutLogRepository
        private set

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        exerciseRepository = ExerciseRepository(database.exerciseDao())
        trainingPlanRepository = TrainingPlanRepository(database.trainingPlanDao())
        workoutLogRepository = WorkoutLogRepository(database.workoutLogDao())
        userPreferencesRepository = UserPreferencesRepository(this)

        prepopulateDataIfNeeded()
    }

    private fun prepopulateDataIfNeeded() {
        MainScope().launch(Dispatchers.IO) {
            val exercises = exerciseRepository.getAllExercises().first()
            if (exercises.isEmpty()) {
                try {
                    val jsonString = assets.open("demo_climbing_plans.json").bufferedReader().use { it.readText() }
                    BackupManager(this@Train2SendApp).importFromJson(jsonString)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
