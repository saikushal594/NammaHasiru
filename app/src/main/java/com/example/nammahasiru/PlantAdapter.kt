package com.example.nammahasiru

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nammahasiru.data.Plant
import com.example.nammahasiru.databinding.ItemPlantCardBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * PlantAdapter — RecyclerView adapter for showing plant cards.
 * Uses ListAdapter with DiffUtil for efficient updates.
 */
class PlantAdapter(private val onStatusClick: (Plant) -> Unit) : ListAdapter<Plant, PlantAdapter.PlantViewHolder>(DIFF_CALLBACK) {

    // DiffUtil compares old and new lists to update only changed items
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Plant>() {
            override fun areItemsTheSame(oldItem: Plant, newItem: Plant) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Plant, newItem: Plant) = oldItem == newItem
        }
    }

    // Creates a new ViewHolder (inflates the card layout)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val binding = ItemPlantCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PlantViewHolder(binding)
    }

    // Binds data to an existing ViewHolder
    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder — holds references to the views in each card.
     */
    inner class PlantViewHolder(private val binding: ItemPlantCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(plant: Plant) {
            // Set plant name and species
            binding.tvCardPlantName.text = plant.name
            binding.tvCardSpecies.text = plant.speciesName

            // Format and set the date
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvCardDate.text = "Added: ${dateFormat.format(Date(plant.dateAdded))}"

            // Set status badge color: green for Alive, red for Dead
            if (plant.status == "Alive") {
                binding.tvCardStatus.text = "🌿 Alive"
                binding.tvCardStatus.setBackgroundResource(R.drawable.bg_status_alive)
            } else {
                binding.tvCardStatus.text = "🍂 Dead"
                binding.tvCardStatus.setBackgroundResource(R.drawable.bg_status_dead)
            }

            // Click to toggle status
            binding.tvCardStatus.setOnClickListener {
                onStatusClick(plant)
            }

            // Show photo if available, otherwise show placeholder
            if (plant.photoPath.isNotEmpty() && File(plant.photoPath).exists()) {
                binding.ivCardPhoto.setImageURI(Uri.fromFile(File(plant.photoPath)))
            } else {
                binding.ivCardPhoto.setImageResource(R.drawable.ic_plant_placeholder)
            }

            // Show location if available
            if (plant.latitude != 0.0 && plant.longitude != 0.0) {
                binding.tvCardLocation.text = "📍 %.4f, %.4f".format(plant.latitude, plant.longitude)
            } else {
                binding.tvCardLocation.text = "📍 No location recorded"
            }
        }
    }
}