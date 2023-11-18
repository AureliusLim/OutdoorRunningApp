package com.mobdeves14.cadaolimyongco

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.api.model.Place.Field
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.maps.route.extensions.drawRouteOnMap
import com.maps.route.extensions.moveCameraOnMap
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Timer
import java.util.TimerTask


/**
 * An activity that displays a Google map with a marker (pin) to indicate a particular location.
 */
class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation:Location
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var currentloc : LatLng
    private lateinit var distanceDisplay: TextView
    private lateinit var speedDisplay: TextView
    private val speedUpdateInterval = 500 // Update speed every 1 second
    private var speedUpdateTimer: Timer? = null
    private lateinit var ETAduration: TextView
    private lateinit var progressTab: ImageButton
    private lateinit var currDate: TextView
    private lateinit var runTab: ImageButton
    private var savedMapState: Bundle? = null
    private var updateRouteTimer: Timer? = null
    private var updateRouteTask: TimerTask? = null
    private lateinit var sharedPrefController : SharedPrefController
    private lateinit var destinationLatLng: LatLng
    private val updateInterval = 1000 // Update speed every 1 second
    private var updateMetricsTimer: Timer? = null
    private var updateMetricsTask: TimerTask? = null
    private lateinit var timeElapsed: TextView
    private lateinit var calories: TextView
    private lateinit var pace: TextView
    private lateinit var avgSpeed: TextView

    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private var totalDistance: Double = 0.0
    private var totalCaloriesBurned: Double = 0.0

    val apiKey = "AIzaSyDm7Z2QpveiwSsWmh4Vr7iFfD_pepJIFtc"
    private lateinit var handlerThread: HandlerThread
    private lateinit var handler: Handler

    companion object{
        private const val LOCATION_REQUEST_CODE = 1
        private const val TAG = "MainActivity"

    }



    override fun onResume() {
        super.onResume()
        // Retrieve shared preferences values here
        if(sharedPrefController.getRunning()){
            this.distanceDisplay.text = sharedPrefController.getDistance()
        }
        else{
            this.distanceDisplay.text = "0 km"
        }
        this.ETAduration.text = sharedPrefController.getETA()


        if(sharedPrefController.getStopped()){
            updateRouteTimer?.purge()
            updateRouteTask?.cancel()
//            mMap?.run {
//                mMap.clear()
//            }
//            mMap?.clear() // uncommenting this would cause a mmap lateinit error idk why
            sharedPrefController.setStopped(false)




        }
        if (sharedPrefController.getRunning()) {
            Log.d("updatedmain","updated main map")
            val latLngString = sharedPrefController.getDestination()
            if (latLngString != null) {
                val parts = latLngString.split(",")
                if (parts.size == 2) {
                    val latitude = parts[0].toDouble()
                    val longitude = parts[1].toDouble()
                    val retrievedLatLng = LatLng(latitude, longitude)
                    this.destinationLatLng = retrievedLatLng
                    updateRouteAndETA(this.destinationLatLng)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handlerThread.quitSafely()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefController = SharedPrefController(this)
        sharedPrefController.saveMetrics("0 km", "0 minutes", false, "", "", false)
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_home)
        //setup statistics
        this.timeElapsed = findViewById(R.id.duration_min11)
        this.calories = findViewById(R.id.calories_tv)
        this.pace = findViewById(R.id.avg_speed_tv2)
        this.avgSpeed = findViewById(R.id.avg_speed_tv)
        handlerThread = HandlerThread("UpdateMetricsThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)



        //setup metrics
        this.progressTab = findViewById(R.id.progresstab)
        this.distanceDisplay = findViewById(R.id.distance)
        this.ETAduration = findViewById((R.id.duration))
        this.currDate = findViewById(R.id.tv_date2)
        this.runTab = findViewById(R.id.runtab)
        val calendar = Calendar.getInstance()
        val dateFormatFullMonth = SimpleDateFormat("EEEE, MMMM dd yyyy", Locale.US)
        val currentDate = calendar.time
        val formattedDateFullMonth = dateFormatFullMonth.format(currentDate)
        this.currDate.text = formattedDateFullMonth
        progressTab.setOnClickListener {
            // Create an Intent to switch to the progress activity
            val intent = Intent(this, ProgressActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            updateRouteTask?.cancel()
            updateRouteTimer?.purge()
            startActivity(intent)

        }
        runTab.setOnClickListener{
            val intent = Intent(this, RunActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            updateRouteTask?.cancel()
            updateRouteTimer?.purge()
            startActivity(intent)
        }

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize Places API
        Places.initialize(applicationContext, apiKey) // Replace with your API key
        var placesClient = Places.createClient(this)

        startLocationUpdates()
        updateMetrics()
        // test user speed
        var userSpeed = UserSpeed()
        userSpeed.start(this)
        userSpeed.startLocationUpdates(this)
        this.speedDisplay = findViewById(R.id.userspeed)
        speedUpdateTimer = Timer()
        speedUpdateTimer?.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    // Update the speed display with the latest speed
                    speedDisplay.text = userSpeed.getUserSpeed() + " km/h"

                }
            }
        }, 0, speedUpdateInterval.toLong())


    }




    fun drawRouteToDestination(googleMap: GoogleMap, source: LatLng, destination: LatLng){

        googleMap?.run {
            moveCameraOnMap(latLng = source) // if you want to zoom the map to any point
            googleMap.clear()
            //Called the drawRouteOnMap extension to draw the polyline/route on google maps
            drawRouteOnMap(
                "AIzaSyDm7Z2QpveiwSsWmh4Vr7iFfD_pepJIFtc", //your API key
                source = source, // Source from where you want to draw path
                destination = destination, // destination to where you want to draw path
                context = this@MainActivity, // Activity Context
                boundMarkers = false,
                travelMode = com.maps.route.model.TravelMode.WALKING//Travel mode, by default it is DRIVING
            ){ estimates ->
                estimates?.let {
                    //Google Estimated time of arrival
                    Log.d(TAG, "ETA: ${it.duration?.text}, ${it.duration?.value}")
                    //Google suggested path distance
                    Log.d(TAG, "Distance: ${it.distance?.text}, ${it.distance?.text}")
                    distanceDisplay.text = it.distance?.text;
                    var value = it.distance?.text;

                    Log.d(TAG, "${value}")
                    if (value != null) {
                        var eta = ((value.split(" ")[0].toDoubleOrNull() ?: 0.0) / 0.107).toInt().toString()
                        if(eta.toInt() < 1){
                            ETAduration.text = "< 1 min"
                        }
                        else{
                            ETAduration.text = eta + " min"
                        }
                    }


                } ?: Log.e(TAG, "Nothing found")
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true

        setUpMap()
    }
    private fun setUpMap(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                RunActivity.LOCATION_REQUEST_CODE
            )
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this){location ->
            if(location != null){
                lastLocation = location
                val currentLatLong = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 12f))
                currentloc = currentLatLong
            }
        }

    }

    fun updateRouteAndETA(destination: LatLng) {
        sharedPrefController.saveMetrics(distanceDisplay.text.toString(), ETAduration.text.toString(), true,  destinationLatLng.longitude.toString(), destinationLatLng.latitude.toString(), sharedPrefController.getPlay())

        drawRouteToDestination(mMap, currentloc, destination)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentloc, 16f))
        // Cancel the previous Timer and TimerTask
        updateRouteTask?.cancel()
        updateRouteTimer?.purge()
        // Schedule the next update in 5 seconds
        if(sharedPrefController.getRunning()){
            updateRouteTimer = Timer()
            updateRouteTask = object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        updateRouteAndETA(destination)
                    }
                }
            }

            updateRouteTimer?.schedule(updateRouteTask, 5000) // 5 seconds
        }

    }
    // Add a helper function to start location updates
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(5000) // Update every 5 seconds
            .setFastestInterval(5000)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }
    // Define a location callback to handle location updates
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                lastLocation = location
                currentloc = LatLng(location.latitude, location.longitude)
                // Update the map or any other logic you need with the new location
            }
        }
    }
    private fun updateMetrics() {
        //Log.d("UPDATEMETRIC","UPDATING")
        if(sharedPrefController.getPlay()){
            startTime += 1


            elapsedTime = startTime
            val elapsedSeconds = elapsedTime
            val elapsedMinutes = elapsedSeconds / 60
            val elapsedHours = elapsedMinutes / 60


            // Calculate average speed (using current speed as an example)
            val currentSpeed = speedDisplay.text.toString().replace(" km/h", "").toDoubleOrNull() ?: 0.0
            val totalSpeed = totalDistance / (elapsedHours + elapsedMinutes / 60.0)

            totalDistance += currentSpeed * (elapsedSeconds / 3600)

            Log.d("currentSpeed","$currentSpeed")
            // Calculate average pace (time to cover 1 km)
            val averagePace = if (totalDistance > 0) {
                (elapsedMinutes + elapsedHours * 60) / totalDistance
            } else {
                0.0
            }


            val caloriesBurned = totalDistance * 0.1

            totalCaloriesBurned += caloriesBurned
            this.timeElapsed.text = formatTime(startTime)
            this.pace.text = averagePace.toString()
            this.calories.text = totalCaloriesBurned.toString()
            this.avgSpeed.text = currentSpeed.toString()

            sharedPrefController.saveStats(this.totalDistance.toString(), startTime, averagePace.toLong(), totalCaloriesBurned.toString(), currentSpeed.toString())
        }

        updateMetricsTask?.cancel()
        updateMetricsTimer?.purge()

        updateMetricsTimer = Timer()
        updateMetricsTask = object : TimerTask() {
            override fun run() {
                runOnUiThread{
                    updateMetrics()

                }
            }
        }
        updateMetricsTimer?.schedule(updateMetricsTask, 1000)


    }
    private fun formatTime(seconds: Long): String {
        val minutes = (seconds / 60).toInt()
        val remainingSeconds = (seconds % 60).toInt()
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    override fun onMapClick(p0: LatLng) {
        TODO("Not yet implemented")
    }

}