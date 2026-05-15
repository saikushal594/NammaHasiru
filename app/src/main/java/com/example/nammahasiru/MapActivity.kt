package com.example.nammahasiru

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.nammahasiru.data.PlantDatabase
import com.example.nammahasiru.databinding.ActivityMapBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        enableMyLocation()
        loadPlantMarkers()
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            
            // Show feedback
            Toast.makeText(this, "⌛ Finding your exact location...", Toast.LENGTH_SHORT).show()

            // Request fresh high accuracy location immediately
            val cts = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val userLatLng = LatLng(location.latitude, location.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 17f))
                    } else {
                        // Fallback to last location if fresh fails
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                            lastLoc?.let {
                                val userLatLng = LatLng(it.latitude, it.longitude)
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                            }
                        }
                    }
                }
        } else {
            ActivityCompat.requestPermissions(
                this, 
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 
                1001
            )
        }
    }

    private fun loadPlantMarkers() {
        val db = PlantDatabase.getDatabase(this)
        
        lifecycleScope.launch {
            val plants = db.plantDao().getAllPlantsSync()
            
            runOnUiThread {
                if (plants.isNotEmpty()) {
                    for (plant in plants) {
                        // Skip plants with 0,0 (uninitialized or failed GPS)
                        if (plant.latitude != 0.0 && plant.longitude != 0.0) {
                            val location = LatLng(plant.latitude, plant.longitude)
                            val markerColor = if (plant.status == "Alive") {
                                BitmapDescriptorFactory.HUE_GREEN
                            } else {
                                BitmapDescriptorFactory.HUE_RED
                            }
                            
                            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            val dateStr = sdf.format(Date(plant.dateAdded))

                            mMap.addMarker(
                                MarkerOptions()
                                    .position(location)
                                    .title(plant.name)
                                    .snippet("$dateStr | ${plant.speciesName} | ${plant.status}")
                                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        }
    }
}