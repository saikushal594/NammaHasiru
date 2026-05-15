package com.example.nammahasiru

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * ReminderScheduler — Utility object to schedule the 90-day WorkManager reminder.
 */
object ReminderScheduler {

    private const val DEMO_MODE = true // Set to false for 90-day production reminders

    /**
     * Schedules a one-time reminder.
     * Demo: 2 Minutes | Production: 90 Days
     */
    fun scheduleReminder(context: Context) {
        val delay = if (DEMO_MODE) 2L else 90L
        val timeUnit = if (DEMO_MODE) TimeUnit.MINUTES else TimeUnit.DAYS

        val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, timeUnit)
            .build()

        WorkManager.getInstance(context).enqueue(reminderRequest)
    }
}
