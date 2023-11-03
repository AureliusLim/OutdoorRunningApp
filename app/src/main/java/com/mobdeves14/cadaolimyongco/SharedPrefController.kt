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
        editor.apply()
    }

    fun getDistance(): String? {
        return sharedPreferences.getString("distance", "0 km")
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



}
