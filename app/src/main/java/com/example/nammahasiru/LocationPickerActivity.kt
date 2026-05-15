package com.example.nammahasiru

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.nammahasiru.databinding.ActivityLocationPickerBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource

class LocationPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityLocationPickerBinding
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_picker) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnConfirmLocation.setOnClickListener {
            val center = mMap.cameraPosition.target
            val resultIntent = Intent()
            resultIntent.putExtra("lat", center.latitude)
            resultIntent.putExtra("lng", center.longitude)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMyLocationButtonEnabled = true
        
        // Update live coordinates as user moves the map
        mMap.setOnCameraMoveListener {
            val center = mMap.cameraPosition.target
            binding.tvLiveCoords.text = "Lat: %.6f, Lng: %.6f".format(center.latitude, center.longitude)
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            
            // Go to current location with fresh high accuracy request
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            val cts = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 18f))
                        binding.tvLiveCoords.text = "Lat: %.6f, Lng: %.6f".format(location.latitude, location.longitude)
                    }
                }
        }
    }
}