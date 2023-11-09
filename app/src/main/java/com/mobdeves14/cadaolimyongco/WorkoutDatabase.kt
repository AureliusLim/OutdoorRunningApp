package com.mobdeves14.cadaolimyongco

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

                    val calendar = Calendar.getInstance()
                    val listofmonthday = ArrayList<String>()
                    val listofweekday= ArrayList<String>()
                    val listofDates= ArrayList<String>()
                    val dateFormatFullMonth = SimpleDateFormat("EEEE, MMMM dd yyyy", Locale.US)
                    val data = ArrayList<WorkoutModel>()
                    for(i in 0 until 5){

                        val currentDate = calendar.time
                        val formattedDateFullMonth = dateFormatFullMonth.format(currentDate)
                        listofDates.add(formattedDateFullMonth)
                        listofmonthday.add( SimpleDateFormat("d", Locale.US).format(currentDate))
                        listofweekday.add(SimpleDateFormat("E", Locale.US).format(currentDate))
                        calendar.add(Calendar.DAY_OF_YEAR, -1)
                    }
                    // Delete all content here.
                    workoutDao.deleteAll()

                    // Add sample words.
                    var workout = Workout(2.0, 1, 1.0, listofDates[0], listofmonthday[0], listofweekday[0], 12)
                    workoutDao.insert(workout)
                    workout = Workout(1.0, 2, 2.0, listofDates[1], listofmonthday[1], listofweekday[1], 21)
                    workoutDao.insert(workout)
                    workout = Workout(2.0, 4, 9.0, listofDates[2], listofmonthday[2], listofweekday[2], 31)
                    workoutDao.insert(workout)
                    workout = Workout(3.0, 3, 3.0, listofDates[3], listofmonthday[3], listofweekday[3], 45)
                    workoutDao.insert(workout)
                    workout = Workout(4.0, 7, 4.0, listofDates[4], listofmonthday[4], listofweekday[4], 66)
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
