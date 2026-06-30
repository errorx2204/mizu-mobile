package com.rushov.mizu.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.rushov.mizu.R

object NotificationHelper {
    private const val CHANNEL_ID = "mizu_budget_alerts"
    private const val CHANNEL_NAME = "Budget Alerts"

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when you approach budget limits"
        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    fun showBudgetAlert(context: Context, category: String, percentage: Int) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Budget Alert: $category")
            .setContentText("You have used $percentage% of your budget!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        notificationManager.notify(category.hashCode(), notification)
    }
}
