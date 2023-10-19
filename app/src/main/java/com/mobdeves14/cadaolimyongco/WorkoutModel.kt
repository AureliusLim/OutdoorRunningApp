package com.mobdeves14.cadaolimyongco

class WorkoutModel(distance: Int, duration: Int, avgSpeed: Double, heartRate: Int, calories: Int, dayofweek: String, dayofmonth: String, actualdate:String) {
    var distance = distance
        private set
    var duration = duration
        private set
    var avgSpeed = avgSpeed
        private set
    var dayofweek = dayofweek
        private set
    var dayofmonth = dayofmonth
        private set
    var heartRate = heartRate
        private set
    var calories = calories
        private set
    var actualdate = actualdate


    override fun toString(): String {
        return "Model{" +
                "distance='" + distance + '\'' +
                ", duration='" + duration + '\'' +
                ", avgSpeed='" + avgSpeed + '\'' +
                ", date='" + dayofweek + " " + dayofmonth +  '\'' +
                ", heartRate=" + heartRate +
                ", calories=" + calories +
                '}'
    }
}