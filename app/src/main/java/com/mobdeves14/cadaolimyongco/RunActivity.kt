package com.mobdeves14.cadaolimyongco

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
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
import com.maps.route.extensions.drawRouteOnMap
import com.maps.route.extensions.moveCameraOnMap
import java.util.Timer


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
    companion object{
        const val LOCATION_REQUEST_CODE = 1
         const val TAG = "RunActivity"
    }
    val apiKey = "AIzaSyDm7Z2QpveiwSsWmh4Vr7iFfD_pepJIFtc"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run)
        this.homeTab = findViewById(R.id.home_btn)
        this.metricwidget = findViewById(R.id.metricwidget)
        this.progressTab = findViewById(R.id.progresstab)
        this.playPauseButton = findViewById(R.id.playpause)
        this.generateroutebtn = findViewById(R.id.generateroute)
        this.setroutebtn = findViewById(R.id.setroute)
        this.distanceDisplay = findViewById(R.id.distance)
        this.ETAduration = findViewById(R.id.duration)
        this.speedDisplay = findViewById(R.id.userspeed)

        playPauseButton.setImageResource(R.drawable.play)
        playPauseButton.setOnClickListener {
            // Toggle between play and pause images
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
                supportFragmentManager.beginTransaction()
                    .hide(autocompleteFragment)
                    .commit()
                isAutocompleteVisible = false
                distanceLayout.visibility = View.VISIBLE
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
                val destinationLatLng = place.latLng
                if (destinationLatLng != null) {
                    Log.d(RunActivity.TAG, destinationLatLng.toString())

                    // Draw route to the selected destination
                    drawRouteToDestination(googleMap,currentloc,destinationLatLng)
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
            moveCameraOnMap(latLng = source) // if you want to zoom the map to any point
            googleMap.clear()
            //Called the drawRouteOnMap extension to draw the polyline/route on google maps
            drawRouteOnMap(
                "AIzaSyDm7Z2QpveiwSsWmh4Vr7iFfD_pepJIFtc", //your API key
                source = source, // Source from where you want to draw path
                destination = destination, // destination to where you want to draw path
                context = this@RunActivity, // Activity Context

                travelMode = com.maps.route.model.TravelMode.WALKING//Travel mode, by default it is DRIVING
            ){ estimates ->
                estimates?.let {
                    //Google Estimated time of arrival
                    Log.d(RunActivity.TAG, "ETA: ${it.duration?.text}, ${it.duration?.value}")
                    //Google suggested path distance
                    Log.d(RunActivity.TAG, "Distance: ${it.distance?.text}, ${it.distance?.text}")
                    distanceDisplay.text = it.distance?.text;
                    var value = it.distance?.text;

                    Log.d(RunActivity.TAG, "${value}")
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

    override fun onMapClick(p0: LatLng) {
        TODO("Not yet implemented")
    }
}