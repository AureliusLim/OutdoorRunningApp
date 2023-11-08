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

}