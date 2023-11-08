package com.mobdeves14.cadaolimyongco

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_table")
data class Workout (
    @PrimaryKey(autoGenerate = true) val id: Int,
    val distance: Int,
    val duration: Int,
    val avgSpeed: Float
) {
    constructor(distance: Int, duration: Int, avgSpeed: Float):this(0, distance, duration, avgSpeed)
}