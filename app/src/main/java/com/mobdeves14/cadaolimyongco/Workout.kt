package com.mobdeves14.cadaolimyongco

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_table")
data class Workout (
    @PrimaryKey(autoGenerate = true) val id: Int,
    var distance: Double,
    var duration: Long, // in seconds
    var avgSpeed: Double,
    var date: String,
    var monthDay: String,
    var weekDay: String,
    var calories: Double,
) {
    constructor(distance: Double, duration: Long, avgSpeed: Double, date: String, monthDay: String, weekDay: String, calories: Double):this(0, distance, duration, avgSpeed, date, monthDay, weekDay, calories)
}