package com.example.nammahasiru.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Plant Entity — represents a single plant record in the Room database.
 * Each field maps to a column in the "plants" table.
 */
@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,           // Common name e.g. "Neem"
    val speciesName: String,    // Scientific name e.g. "Azadirachta indica"
    val notes: String,          // User notes
    val photoPath: String,      // File path to the saved photo
    val latitude: Double,       // GPS latitude
    val longitude: Double,      // GPS longitude
    val status: String = "Alive", // "Alive" or "Dead"
    val dateAdded: Long = System.currentTimeMillis() // Timestamp in millis
)