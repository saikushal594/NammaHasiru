package com.example.nammahasiru.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * PlantDao — Data Access Object.
 * Defines all database operations for the Plant entity.
 */
@Dao
interface PlantDao {

    // Insert a new plant into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: Plant)

    // Get all plants, ordered by newest first
    @Query("SELECT * FROM plants ORDER BY dateAdded DESC")
    fun getAllPlants(): LiveData<List<Plant>>

    // Get all plants as a plain list (for dashboard calculations)
    @Query("SELECT * FROM plants")
    suspend fun getAllPlantsSync(): List<Plant>

    // Count total plants
    @Query("SELECT COUNT(*) FROM plants")
    suspend fun getTotalCount(): Int

    // Count alive plants
    @Query("SELECT COUNT(*) FROM plants WHERE status = 'Alive'")
    suspend fun getAliveCount(): Int

    // Count dead plants
    @Query("SELECT COUNT(*) FROM plants WHERE status = 'Dead'")
    suspend fun getDeadCount(): Int

    // Update plant status (Alive/Dead)
    @Query("UPDATE plants SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    // Delete a plant by ID
    @Query("DELETE FROM plants WHERE id = :id")
    suspend fun deletePlant(id: Int)
}