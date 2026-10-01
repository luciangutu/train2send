package com.train2send.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.train2send.data.dao.ExerciseDao
import com.train2send.data.dao.TrainingPlanDao
import com.train2send.data.model.*

@Database(
    entities = [
        ExerciseEntity::class,
        TrainingPlanEntity::class,
        PlanDayEntity::class,
        PlannedExerciseEntity::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun trainingPlanDao(): TrainingPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE training_plans ADD COLUMN description TEXT DEFAULT NULL")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Recategorize known finger/hangboard exercises to the new FINGER category.
                // Matches exercises imported from bundled asset plans by their stable IDs.
                db.execSQL(
                    """
                    UPDATE exercises SET category = 'FINGER' WHERE id IN (
                        'ex-abrahangs',
                        'ex-dead-hang-jugs',
                        'ex-dead-hang-small-edge',
                        'ex-max-finger',
                        'ex-no-hangs',
                        'ex-hangboard-max',
                        'ex-hangboard-repeaters',
                        'ex-strength-endurance-hangboard',
                        'ex-repeaters',
                        'ex-megos-pe-finger',
                        'ex-hangboard-pe',
                        'ex-finger-curls',
                        'ex-finger-lift'
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "train2send_db"
                )
                    .addMigrations(MIGRATION_7_8, MIGRATION_8_9)
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
