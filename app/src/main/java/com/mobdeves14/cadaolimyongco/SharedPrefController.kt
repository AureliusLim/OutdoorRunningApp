package com.mobdeves14.cadaolimyongco


import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.maps.model.LatLng

class SharedPrefController(context: Context){
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

    fun saveMetrics(distance: String, ETA: String, running: Boolean, longitudeString: String, latitudeString: String, play: Boolean){
        val editor = sharedPreferences.edit()
        editor.putString("distance", distance)
        editor.putString("ETA", ETA)
        editor.putBoolean("running", running)
        // add code here on saving longitude string and latitude string separately
        editor.putString("latLng", "$latitudeString,$longitudeString")
        editor.putBoolean("play",play)
        editor.apply()
    }


    fun saveStats(totalDistance: String, timeElapsed: String, pace: String, calories: String, avgSpeed: String){
        val editor = sharedPreferences.edit()
        editor.putString("totalDistance", totalDistance)
        editor.putString("timeElapsed", timeElapsed)
        editor.putString("pace", pace)
        editor.putString("calories", calories)
        editor.putString("avgSpeed", avgSpeed)
        editor.apply()
    }

    fun getDistance(): String? {
        return sharedPreferences.getString("distance", "null")
    }
    fun getETA(): String? {
        return sharedPreferences.getString("ETA", "0 minutes")
    }
    fun getRunning(): Boolean {
        return sharedPreferences.getBoolean("running", false)
    }

    fun getDestination(): String? {
        return sharedPreferences.getString("latLng", null)
    }
    fun getPlay(): Boolean{
        return sharedPreferences.getBoolean("play", false)
    }

    fun getStopped(): Boolean{
        return sharedPreferences.getBoolean("stop", false)
    }

    fun changePlay(play: Boolean){
        val editor = sharedPreferences.edit()
        editor.putBoolean("play",!play)
        editor.apply()
    }

    fun setStopped(stop: Boolean){
        val editor = sharedPreferences.edit()
        editor.putBoolean("stop", stop)
        editor.apply()
    }

    fun gettotalDistance(): String? {
        return sharedPreferences.getString("totalDistance", null)
    }
    fun getPace(): String? {
        return sharedPreferences.getString("pace", null)
    }
    fun getElapsedTime(): String? {
        return sharedPreferences.getString("timeElapsed", null)
    }
    fun getAvgSpeed(): String? {
        return sharedPreferences.getString("avgSpeed", null)
    }
    fun getCalories(): String? {
        return sharedPreferences.getString("calories", null)
    }



}
