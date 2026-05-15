package com.example.nammahasiru

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nammahasiru.data.Plant
import com.example.nammahasiru.data.PlantDatabase
import com.example.nammahasiru.databinding.ActivityViewPlantsBinding
import kotlinx.coroutines.launch

/**
 * ViewPlantsActivity — Shows all saved plants in a RecyclerView.
 */
class ViewPlantsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewPlantsBinding
    private lateinit var plantAdapter: PlantAdapter
    private var allPlants: List<Plant> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewPlantsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back button
        binding.btnBack.setOnClickListener { finish() }

        // Setup RecyclerView
        val db = PlantDatabase.getDatabase(this)
        plantAdapter = PlantAdapter { plant ->
            // Toggle status logic
            val newStatus = if (plant.status == "Alive") "Dead" else "Alive"
            lifecycleScope.launch {
                db.plantDao().updateStatus(plant.id, newStatus)
                // Note: LiveData will auto-refresh the list
                runOnUiThread {
                    Toast.makeText(this@ViewPlantsActivity, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.recyclerViewPlants.apply {
            adapter = plantAdapter
            layoutManager = LinearLayoutManager(this@ViewPlantsActivity)
        }

        // Setup Search
        binding.etSearch.addTextChangedListener { text ->
            filterPlants(text.toString())
        }

        // Observe the LiveData from Room — auto-updates when data changes
        db.plantDao().getAllPlants().observe(this, Observer { plants ->
            allPlants = plants
            filterPlants(binding.etSearch.text.toString())
        })
    }

    private fun filterPlants(query: String) {
        val filteredList = if (query.isEmpty()) {
            allPlants
        } else {
            allPlants.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.speciesName.contains(query, ignoreCase = true)
            }
        }

        plantAdapter.submitList(filteredList)

        // Show empty state if no plants match search or if list is empty
        if (filteredList.isEmpty()) {
            binding.llEmptyState.visibility = android.view.View.VISIBLE
            binding.recyclerViewPlants.visibility = android.view.View.GONE
        } else {
            binding.llEmptyState.visibility = android.view.View.GONE
            binding.recyclerViewPlants.visibility = android.view.View.VISIBLE
        }
    }
}
