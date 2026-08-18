package com.train4send.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.train4send.data.dao.ExerciseDao
import com.train4send.data.dao.TrainingPlanDao
import com.train4send.data.dao.WorkoutLogDao
import com.train4send.data.model.*

@Database(
    entities = [
        ExerciseEntity::class,
        TrainingPlanEntity::class,
        PlanDayEntity::class,
        PlannedExerciseEntity::class,
        WorkoutLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun trainingPlanDao(): TrainingPlanDao
    abstract fun workoutLogDao(): WorkoutLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "train4send_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
