package com.rushov.mizu.presentation.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.rushov.mizu.R

object NotificationHelper {
    private const val CHANNEL_ID = "mizu_budget_alerts"
    private const val CHANNEL_NAME = "Budget Alerts"
    private const val CHANNEL_DESC = "Notifications when budget limits are reached"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendBudgetAlert(context: Context, category: String, spent: Double, budget: Double, percentage: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Budget Alert: $category")
            .setContentText("You have spent Rs. ${String.format("%.2f", spent)} of Rs. ${String.format("%.2f", budget)} ($percentage%)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(category.hashCode(), notification)
    }
}
