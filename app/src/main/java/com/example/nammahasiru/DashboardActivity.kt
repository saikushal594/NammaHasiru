package com.example.nammahasiru

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nammahasiru.data.PlantDatabase
import com.example.nammahasiru.databinding.ActivityDashboardBinding
import kotlinx.coroutines.launch

/**
 * DashboardActivity — Shows survival statistics.
 * Displays total, alive, dead counts and a survival percentage.
 */
class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back button
        binding.btnBack.setOnClickListener { finish() }

        loadStats()
    }

    /**
     * Loads all statistics from the database and updates the UI.
     */
    private fun loadStats() {
        val db = PlantDatabase.getDatabase(this)

        lifecycleScope.launch {
            val total = db.plantDao().getTotalCount()
            val alive = db.plantDao().getAliveCount()
            val dead = db.plantDao().getDeadCount()

            // Calculate survival percentage (avoid divide by zero)
            val survivalPercent = if (total > 0) ((alive.toFloat() / total) * 100).toInt() else 0

            // Update UI on the main thread
            runOnUiThread {
                binding.tvTotalCount.text = total.toString()
                binding.tvAliveCount.text = alive.toString()
                binding.tvDeadCount.text = dead.toString()
                binding.tvSurvivalPercent.text = "$survivalPercent%"

                // Update the progress bar
                binding.progressSurvival.progress = survivalPercent

                // Set descriptive message based on percentage
                binding.tvSurvivalMessage.text = when {
                    survivalPercent >= 80 -> "🌿 Excellent! Your garden is thriving!"
                    survivalPercent >= 50 -> "🌱 Good progress! Keep nurturing your plants."
                    survivalPercent > 0   -> "⚠️ Some plants need attention."
                    else                  -> "🌾 Add plants to see your statistics."
                }
            }
        }
    }

    // Reload stats every time screen becomes visible
    override fun onResume() {
        super.onResume()
        loadStats()
    }
}