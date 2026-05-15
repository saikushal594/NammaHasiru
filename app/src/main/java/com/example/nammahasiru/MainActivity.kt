package com.example.nammahasiru

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nammahasiru.databinding.ActivityMainBinding

/**
 * MainActivity — The Home Screen of NammaHasiru.
 * Shows the app title and three navigation buttons.
 */
class MainActivity : AppCompatActivity() {

    // View Binding — safer alternative to findViewById
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Button: Add New Plant → opens AddPlantActivity
        binding.cardAddPlant.setOnClickListener {
            startActivity(Intent(this, AddPlantActivity::class.java))
        }

        // Button: View Plants → opens ViewPlantsActivity
        binding.cardViewPlants.setOnClickListener {
            startActivity(Intent(this, ViewPlantsActivity::class.java))
        }

        // Button: Survival Dashboard → opens DashboardActivity
        binding.cardDashboard.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }

        // Button: Plant Map → opens MapActivity
        binding.cardPlantMap.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }
    }
}