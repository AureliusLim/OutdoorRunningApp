package com.mobdeves14.cadaolimyongco

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    // TODO("Add a feature that sorts by date in descending order"
    @Query("SELECT * FROM workout_table")
    fun getWorkouts(): Flow<List<Workout>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(workout: Workout)
    @Query("DELETE FROM workout_table")
    suspend fun deleteAll()

    @Query("SELECT id, ROUND(SUM(distance), 2) AS distance, ROUND(AVG(pace), 2) AS pace, SUM(duration) AS duration, ROUND(AVG(avgSpeed), 2) AS avgSpeed, date, monthDay, weekDay, ROUND(SUM(calories), 2) AS calories " +
            "FROM workout_table " +
            "GROUP BY date " +
            "Order BY date DESC")
    fun getWorkoutByMultipleDates(): Flow<List<Workout>>

}