package com.mobdeves14.cadaolimyongco

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class WorkoutApplication: Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { WorkoutDatabase.getDatabase(this, applicationScope)}
    val repository by lazy { WorkoutRepository(database.workoutDao())}
}
