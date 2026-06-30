package com.rushov.mizu.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.rushov.mizu.R
import com.rushov.mizu.data.local.DataStoreManager
import kotlinx.coroutines.runBlocking

class MizuBalanceWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.widget_balance)
    
    val dataStore = DataStoreManager(context)
    val balance = runBlocking {
        try {
            "Rs. 0.00"
        } catch (e: Exception) {
            "Tap to setup"
        }
    }
    
    views.setTextViewText(R.id.widget_balance_text, balance)
    views.setTextViewText(R.id.widget_title, "MIZU Balance")
    
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
