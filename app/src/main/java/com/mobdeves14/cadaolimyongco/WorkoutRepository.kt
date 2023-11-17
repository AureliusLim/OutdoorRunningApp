package com.mobdeves14.cadaolimyongco

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val workoutDao: WorkoutDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allWorkouts: Flow<List<Workout>> = workoutDao.getWorkouts()
    val filteredWorkouts: Flow<List<Workout>> = workoutDao.getWorkoutByMultipleDates()

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(workout: Workout) {
        workoutDao.insert(workout)
    }
}
