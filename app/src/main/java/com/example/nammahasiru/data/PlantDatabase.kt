package com.example.nammahasiru.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * PlantDatabase — the main Room database class.
 * Uses singleton pattern so only one instance exists at a time.
 */
@Database(entities = [Plant::class], version = 1, exportSchema = false)
abstract class PlantDatabase : RoomDatabase() {

    // Abstract function to get the DAO
    abstract fun plantDao(): PlantDao

    companion object {
        // Volatile ensures the instance is always up-to-date across threads
        @Volatile
        private var INSTANCE: PlantDatabase? = null

        /**
         * Returns the singleton database instance.
         * Creates it if it doesn't exist yet.
         */
        fun getDatabase(context: Context): PlantDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlantDatabase::class.java,
                    "plant_database" // Name of the .db file
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}