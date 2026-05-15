package com.example.nammahasiru

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.nammahasiru.data.Plant
import com.example.nammahasiru.data.PlantDatabase
import com.example.nammahasiru.databinding.ActivityAddPlantBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * AddPlantActivity — Screen to add a new plant.
 * Features: photo capture, GPS location, form validation, Room save.
 */
class AddPlantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPlantBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Stores the path of the captured photo
    private var currentPhotoPath: String = ""
    private var currentPhotoUri: Uri? = null
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var selectedDate: Long = System.currentTimeMillis()

    // Activity Result Launchers (Modern API)
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            binding.ivPlantPhoto.setImageURI(currentPhotoUri)
            binding.tvPhotoStatus.text = "✅ Photo captured!"
        } else {
            Toast.makeText(this, "Failed to capture photo", Toast.LENGTH_SHORT).show()
        }
    }

    private val locationPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val lat = result.data?.getDoubleExtra("lat", 0.0) ?: 0.0
            val lng = result.data?.getDoubleExtra("lng", 0.0) ?: 0.0
            if (lat != 0.0 && lng != 0.0) {
                currentLatitude = lat
                currentLongitude = lng
                binding.tvLocationStatus.text = "📍 Manual: %.4f, %.4f".format(lat, lng)
            }
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            dispatchTakePictureIntent()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            fetchLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Reminders won't work without notification permission", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Initialize the GPS location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Set initial date
        updateDateLabel()

        // Back arrow click
        binding.btnBack.setOnClickListener { finish() }

        // Date Picker click
        binding.etDate.setOnClickListener {
            showDatePicker()
        }

        // Take Photo button
        binding.btnTakePhoto.setOnClickListener {
            checkCameraPermissionAndCapture()
        }

        // Get Location button
        binding.btnGetLocation.setOnClickListener {
            checkLocationPermissionAndFetch()
        }

        // Pick Location button
        binding.btnPickLocation.setOnClickListener {
            locationPickerLauncher.launch(Intent(this, LocationPickerActivity::class.java))
        }

        // Save Plant button
        binding.btnSavePlant.setOnClickListener {
            savePlant()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                selectedDate = selectedCalendar.timeInMillis
                updateDateLabel()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun updateDateLabel() {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        binding.etDate.setText(sdf.format(Date(selectedDate)))
    }

    // ─── Camera Logic ────────────────────────────────────────────────────────

    private fun checkCameraPermissionAndCapture() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        val photoFile: File? = try { createImageFile() } catch (e: Exception) { null }
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                it
            )
            currentPhotoUri = photoURI
            takePictureLauncher.launch(photoURI)
        }
    }

    private fun createImageFile(): File {
        // Create a unique filename using timestamp
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PLANT_${timeStamp}_", ".jpg", storageDir).also {
            currentPhotoPath = it.absolutePath
        }
    }

    // ─── Location Logic ──────────────────────────────────────────────────────

    private fun checkLocationPermissionAndFetch() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            fetchLocation()
        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            binding.tvLocationStatus.text = "⌛ Fetching precise GPS fix..."
            
            // Skip lastLocation as it can be stale (wrong). Request a fresh HIGH ACCURACY fix.
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    updateLocationUI(location)
                } else {
                    binding.tvLocationStatus.text = "❌ Error: Unable to get GPS fix."
                    Toast.makeText(this, "Make sure GPS is turned ON and you are outdoors", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener {
                binding.tvLocationStatus.text = "❌ Error: ${it.message}"
            }
        }
    }

    private fun updateLocationUI(location: Location) {
        currentLatitude = location.latitude
        currentLongitude = location.longitude
        binding.tvLocationStatus.text =
            "📍 Lat: %.6f, Lon: %.6f".format(currentLatitude, currentLongitude)
    }

    // ─── Save Logic ──────────────────────────────────────────────────────────

    private fun savePlant() {
        val name = binding.etPlantName.text.toString().trim()
        val species = binding.etSpeciesName.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        // Validation: Plant name is required
        if (name.isEmpty()) {
            binding.etPlantName.error = "Plant name is required"
            return
        }

        // Validation: Species name is required
        if (species.isEmpty()) {
            binding.etSpeciesName.error = "Species name is required"
            return
        }

        // Create the Plant object
        val plant = Plant(
            name = name,
            speciesName = species,
            notes = notes,
            photoPath = currentPhotoPath,
            latitude = currentLatitude,
            longitude = currentLongitude,
            dateAdded = selectedDate
        )

        // Save to Room Database using a coroutine (background thread)
        val db = PlantDatabase.getDatabase(this)
        lifecycleScope.launch {
            db.plantDao().insertPlant(plant)

            // Show success message and schedule reminder
            runOnUiThread {
                Toast.makeText(this@AddPlantActivity, "🌱 Plant saved successfully!", Toast.LENGTH_LONG).show()
                finish() // Go back to previous screen
            }

            // Schedule a reminder (2 minutes for demo)
            ReminderScheduler.scheduleReminder(this@AddPlantActivity)
        }
    }
}