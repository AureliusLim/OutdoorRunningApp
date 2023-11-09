package com.mobdeves14.cadaolimyongco

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_table")
data class Workout (
    @PrimaryKey(autoGenerate = true) val id: Int,
    var distance: Double,
    var duration: Int,
    var avgSpeed: Double,
    var date: String,
    var monthDay: String,
    var weekDay: String,
    var calories: Int,
) {
    constructor(distance: Double, duration: Int, avgSpeed: Double, date: String, monthDay: String, weekDay: String, calories: Int):this(0, distance, duration, avgSpeed, date, monthDay, weekDay, calories)
}