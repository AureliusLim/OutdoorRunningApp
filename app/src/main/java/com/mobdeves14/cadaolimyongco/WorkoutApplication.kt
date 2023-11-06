package com.mobdeves14.cadaolimyongco

import android.app.Application

class WorkoutApplication: Application() {
    val database by lazy { WorkoutDatabase.getDatabase(this)}
    val repository by lazy { WorkoutDatabase.getDatabase(this)}
}
