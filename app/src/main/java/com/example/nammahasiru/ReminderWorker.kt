package com.example.nammahasiru

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * ReminderWorker — WorkManager Worker that sends a plant growth check notification.
 * Triggered 90 days after adding a plant.
 */
class ReminderWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "plant_reminder_channel"
        const val NOTIFICATION_ID = 1001
    }

    override fun doWork(): Result {
        // Create the notification channel (required for Android 8+)
        createNotificationChannel()

        // Build and show the notification
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_plant_notification)
            .setContentTitle("🌿 NammaHasiru Reminder")
            .setContentText("Time to check your plant growth!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Check your plant growth and update the status in the app!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        return Result.success() // Tell WorkManager the task completed
    }

    /**
     * Creates the notification channel required for Android 8.0 (Oreo) and above.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Plant Growth Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you to check plant growth after 90 days"
            }
            val manager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}