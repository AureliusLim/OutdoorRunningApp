package com.mobdeves14.cadaolimyongco

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.api.model.Place.Field
import com.google.android.gms.common.api.Status
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

    val apiKey = "AIzaSyDm7Z2QpveiwSsWmh4Vr7iFfD_pepJIFtc"
    companion object{
        private const val LOCATION_REQUEST_CODE = 1
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_home)
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
            startActivity(intent)

        }
        runTab.setOnClickListener{
            val intent = Intent(this, RunActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize Places API
        Places.initialize(applicationContext, apiKey) // Replace with your API key
        var placesClient = Places.createClient(this)

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
        // Initialize Autocomplete fragment for place selection
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Field.ID, Field.NAME, Field.LAT_LNG))
        autocompleteFragment.view?.setBackgroundColor(Color.rgb(242, 245, 244))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // Handle place selection
                val destinationLatLng = place.latLng
                if (destinationLatLng != null) {
                    Log.d(TAG, destinationLatLng.toString())

                    // Draw route to the selected destination
                    drawRouteToDestination(googleMap,currentloc,destinationLatLng)
                }
            }

            override fun onError(status: Status) {
                // Handle errors
                Log.e(TAG, "Error: ${status.statusMessage}")
            }
        })
    }
   private fun setUpMap(){
       if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
           != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
           != PackageManager.PERMISSION_GRANTED
       ) {
           ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
           return
       }
       mMap.isMyLocationEnabled = true
       fusedLocationClient.lastLocation.addOnSuccessListener(this){location ->
           if(location != null){
               lastLocation = location
               val currentLatLong = LatLng(location.latitude, location.longitude)
               placeMarkerOnMap(currentLatLong)
               mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 12f))
               currentloc = currentLatLong
           }
       }

   }

    private fun placeMarkerOnMap(currentLatLong: LatLng){
//        val markerOptions = MarkerOptions().position(currentLatLong)
//        markerOptions.title("$currentLatLong")
//        mMap.addMarker(markerOptions)
//        Log.d(TAG, currentLatLong.toString())
    }

    override fun onMapClick(p0: LatLng) {
        TODO("Not yet implemented")
    }

}