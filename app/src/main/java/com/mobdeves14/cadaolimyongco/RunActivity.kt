package com.mobdeves14.cadaolimyongco

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.button.MaterialButtonToggleGroup
import com.maps.route.extensions.drawRouteOnMap
import java.security.AccessController.getContext
import java.util.Timer
import java.util.TimerTask


class RunActivity: AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private lateinit var homeTab: ImageButton
    private lateinit var progressTab: ImageButton
    private var isAutocompleteVisible = true
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private lateinit var distanceLayout: LinearLayout
    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var currentloc : LatLng
    private lateinit var distanceDisplay: TextView
    private lateinit var speedDisplay: TextView
    private val speedUpdateInterval = 500 // Update speed every 1 second
    private var speedUpdateTimer: Timer? = null
    private lateinit var ETAduration: TextView
    private lateinit var playPauseButton: ImageButton
    private var isPlaying = false
    private lateinit var metricwidget:LinearLayout
    private lateinit var generateroutebtn: Button
    private lateinit var setroutebtn: Button
    private lateinit var toggleButtonGroup: MaterialButtonToggleGroup
    private var updateRouteTimer: Timer? = null
    private var updateRouteTask: TimerTask? = null
    private lateinit var sharedPrefController: SharedPrefController
    private var running = false
    private lateinit var destinationLatLng: LatLng
    private lateinit var generateDistance: EditText
    companion object{
        const val LOCATION_REQUEST_CODE = 1
         const val TAG = "RunActivity"
    }
    val apiKey = "AIzaSyDm7Z2QpveiwSsWmh4Vr7iFfD_pepJIFtc"
    override fun onResume() {
        super.onResume()

        // Retrieve shared preferences values here
        this.distanceDisplay.text = sharedPrefController.getDistance()
        this.ETAduration.text = sharedPrefController.getETA()
        this.running = sharedPrefController.getRunning()
        if(this.isPlaying){
            playPauseButton.setImageResource(R.drawable.pause)
        }
        else{
            playPauseButton.setImageResource(R.drawable.play)
        }
        if (sharedPrefController.getRunning()) {
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefController = SharedPrefController(this)
        sharedPrefController.saveMetrics("0 km", "0 minutes", false, "", "", false)
        setContentView(R.layout.activity_run)
        sharedPrefController = SharedPrefController(this)
        this.homeTab = findViewById(R.id.home_btn)
        this.metricwidget = findViewById(R.id.metricwidget)
        this.progressTab = findViewById(R.id.progresstab)
        this.playPauseButton = findViewById(R.id.playpause)
        this.generateroutebtn = findViewById(R.id.generateroute)
        this.setroutebtn = findViewById(R.id.setroute)
        this.distanceDisplay = findViewById(R.id.distance)
        this.ETAduration = findViewById(R.id.duration)
        this.speedDisplay = findViewById(R.id.userspeed)
        this.toggleButtonGroup = findViewById(R.id.toggleButtonGroup)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startLocationUpdates()
        this.generateDistance = findViewById(R.id.distanceEditText)
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        this.generateDistance.setOnEditorActionListener { _, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE ||  (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                var value = this.generateDistance.text.toString()
                var num = value.toDouble()
                if (value.isNotEmpty() && num > 0 && num <= 10) {
                    // Convert the input value to a double (kilometers)
                    val distanceInKm = value.toDouble()

                    // Generate a random destination based on the distance
                    val randomDestination = generateRandomDestination(distanceInKm)
                    destinationLatLng = randomDestination
                    running = true
                    startRun()
                    // Save destination and other metrics in SharedPreferences
                    sharedPrefController.saveMetrics(
                        distanceDisplay.text.toString(),
                        ETAduration.text.toString(),
                        running,
                        destinationLatLng.longitude.toString(),
                        destinationLatLng.latitude.toString(),
                        this.isPlaying
                    )
                    // Draw the route to the randomly generated destination
                    updateRouteAndETA(destinationLatLng)
                }
                else{
                    Toast.makeText(this, "Distance has to be within 0-10 km", Toast.LENGTH_SHORT).show()
                }
                inputMethodManager.hideSoftInputFromWindow(this.generateDistance.windowToken, 0)

                return@setOnEditorActionListener true

            }
            return@setOnEditorActionListener false
            }

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




        playPauseButton.setImageResource(R.drawable.play)

        playPauseButton.setOnClickListener {
            if(running){
                // Toggle between play and pause images
                startRun()
                Log.d("checkedButtonId", this.toggleButtonGroup.checkedButtonId.toString())
                Log.d("generateroutebtn", generateroutebtn.id.toString())
            }

        }


        // Initialize the Autocomplete fragment
        autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                as AutocompleteSupportFragment
        homeTab.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT

           startActivity(intent)

        }
        progressTab.setOnClickListener{
            val intent = Intent(this, ProgressActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
        distanceLayout = findViewById(R.id.distanceLayout)
        // Get the SupportMapFragment and request notification when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment

        mapFragment?.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize Places API
        Places.initialize(applicationContext, apiKey) // Replace with your API key
        var placesClient = Places.createClient(this)

        // default to on generate route click
        if (isAutocompleteVisible) {
            // Hide the Autocomplete fragment
            supportFragmentManager.beginTransaction()
                .hide(autocompleteFragment)
                .commit()
            isAutocompleteVisible = false
            // Show the distance layout
            distanceLayout.visibility = View.VISIBLE
        }
    }
    fun startRun(){
        isPlaying = !isPlaying

        if (isPlaying) {
            playPauseButton.setImageResource(R.drawable.pause)
            metricwidget.visibility = View.VISIBLE
            generateroutebtn.visibility = View.GONE
            setroutebtn.visibility = View.GONE
            // Hide the Autocomplete fragment
            supportFragmentManager.beginTransaction()
                .hide(autocompleteFragment)
                .commit()
            isAutocompleteVisible = false
            distanceLayout.visibility = View.GONE

        } else {
            playPauseButton.setImageResource(R.drawable.play)
            metricwidget.visibility = View.GONE
            generateroutebtn.visibility = View.VISIBLE
            setroutebtn.visibility = View.VISIBLE
            // Hide the Autocomplete fragment
            if (this.toggleButtonGroup.checkedButtonId == this.generateroutebtn.id) {
                distanceLayout.visibility = View.VISIBLE
            }
            else if (this.toggleButtonGroup.checkedButtonId == this.setroutebtn.id) {
                supportFragmentManager.beginTransaction()
                    .show(autocompleteFragment)
                    .commit()
                isAutocompleteVisible = true
            }
        }
    }
    fun onGenerateRouteClick(view: View) {
        if (isAutocompleteVisible) {
            // Hide the Autocomplete fragment
            supportFragmentManager.beginTransaction()
                .hide(autocompleteFragment)
                .commit()
            isAutocompleteVisible = false
            // Show the distance layout
            distanceLayout.visibility = View.VISIBLE
        }
    }
    fun onSetRouteClick(view: View) {
        if (!isAutocompleteVisible) {
            // Show the Autocomplete fragment
            supportFragmentManager.beginTransaction()
                .show(autocompleteFragment)
                .commit()
            isAutocompleteVisible = true
            // Hide the distance layout
            distanceLayout.visibility = View.GONE
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true

        setUpMap()
        // Initialize Autocomplete fragment for place selection
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment.view?.setBackgroundColor(Color.rgb(255,255,255))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // Handle place selection
                destinationLatLng = place.latLng
                running = true
                startRun()
                sharedPrefController.saveMetrics(distanceDisplay.text.toString(), ETAduration.text.toString(), running,  destinationLatLng.longitude.toString(), destinationLatLng.latitude.toString(), isPlaying)
                if (destinationLatLng != null) {
                    Log.d(RunActivity.TAG, destinationLatLng.toString())
                    // Draw route to the selected destination
                    updateRouteAndETA(destinationLatLng)
                }
            }

            override fun onError(status: Status) {
                // Handle errors
                Log.e(RunActivity.TAG, "Error: ${status.statusMessage}")
            }
        })
    }
    fun drawRouteToDestination(googleMap: GoogleMap, source: LatLng, destination: LatLng){

        googleMap?.run {
            //moveCameraOnMap(latLng = source) // if you want to zoom the map to any point
            googleMap.clear()
            //Called the drawRouteOnMap extension to draw the polyline/route on google maps
            drawRouteOnMap(
                "AIzaSyDm7Z2QpveiwSsWmh4Vr7iFfD_pepJIFtc", //your API key
                source = source, // Source from where you want to draw path
                destination = destination, // destination to where you want to draw path
                context = this@RunActivity, // Activity Context
                boundMarkers = false,
                travelMode = com.maps.route.model.TravelMode.WALKING//Travel mode, by default it is DRIVING
            ){ estimates ->
                estimates?.let {
                    //Google Estimated time of arrival
                    //Log.d(RunActivity.TAG, "ETA: ${it.duration?.text}, ${it.duration?.value}")
                    //Google suggested path distance
                    //Log.d(RunActivity.TAG, "Distance: ${it.distance?.text}, ${it.distance?.text}")
                    distanceDisplay.text = it.distance?.text;
                    var value = it.distance?.text;

                    //Log.d(RunActivity.TAG, "${value}")
                    if (value != null) {
                        var eta = ((value.split(" ")[0].toDoubleOrNull() ?: 0.0) / 0.107).toInt().toString()
                        if(eta.toInt() < 1){
                            ETAduration.text = "< 1 min"
                        }
                        else{
                            ETAduration.text = eta + " min"
                        }
                    }




                } ?: Log.e(RunActivity.TAG, "Nothing found")
            }
        }

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
        sharedPrefController.saveMetrics(distanceDisplay.text.toString(), ETAduration.text.toString(), running,  destinationLatLng.longitude.toString(), destinationLatLng.latitude.toString(), isPlaying)
        Log.d("currentLOC", "$currentloc")
        drawRouteToDestination(mMap, currentloc, destination)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentloc, 12f))
        // Cancel the previous Timer and TimerTask
        updateRouteTask?.cancel()
        updateRouteTimer?.purge()
        // Schedule the next update in 5 seconds
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
    private fun generateRandomDestination(distanceInKm: Double): LatLng {
        // Earth radius in kilometers
        val earthRadius = 6371.0
        var adjusted = distanceInKm / 1.8
        // Generate a random angle in radians
        val randomAngle = Math.toRadians(Math.random() * 360.0)

        // Calculate the latitude and longitude offsets
        val latOffset = (adjusted/ earthRadius) * (180.0 / Math.PI)
        val lngOffset = (adjusted / earthRadius) * (180.0 / Math.PI) / Math.cos(Math.toRadians(lastLocation.latitude))

        // Calculate the new latitude and longitude
        val newLat = lastLocation.latitude + latOffset
        val newLng = lastLocation.longitude + lngOffset

        return LatLng(newLat, newLng)
    }




    override fun onMapClick(p0: LatLng) {
        TODO("Not yet implemented")
    }
}