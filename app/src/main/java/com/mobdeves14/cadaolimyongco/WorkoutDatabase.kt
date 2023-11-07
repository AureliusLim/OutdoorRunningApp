package com.mobdeves14.cadaolimyongco

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = arrayOf(Workout::class), version = 1, exportSchema = false)
abstract class WorkoutDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao

    private class WordDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    var workoutDao = database.workoutDao()

                    // Delete all content here.
                    workoutDao.deleteAll()

                    // Add sample words.
                    var workout = Workout(1, 2, 3.0f)
                    workoutDao.insert(workout)
                    workout = Workout(2, 3, 5.0f)
                    workoutDao.insert(workout)

                    workout = Workout(3, 9, 8.0f)
                    workoutDao.insert(workout)
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): WorkoutDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "workout_database"
                )
                    .addCallback(WordDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
