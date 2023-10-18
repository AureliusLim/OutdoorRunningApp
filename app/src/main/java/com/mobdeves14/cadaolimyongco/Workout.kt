package com.mobdeves14.cadaolimyongco

import androidx.room.Entity
import androidx.room.PrimaryKey

data class Workout (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val distance: Int,
    val duration: Int,
    val avgSpeed: Float
)