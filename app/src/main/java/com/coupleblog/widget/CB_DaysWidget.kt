package com.coupleblog.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import com.coupleblog.R
import com.coupleblog.singleton.CB_AppFunc
import com.google.firebase.ktx.Firebase

/**
 * Implementation of App Widget functionality.
 */
class CB_DaysWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Construct the RemoteViews object
    val remoteViews = RemoteViews(context.packageName, R.layout.c_b__days_widget)
    if(CB_AppFunc.getAuth().currentUser == null)
    {
        remoteViews.setTextViewText(R.id.item_text_view, CB_AppFunc.getString(R.string.str_widget_error))
        remoteViews.setViewVisibility(R.id.days_text_view, View.GONE)
        remoteViews.setViewVisibility(R.id.icon_image_view, View.GONE)
        return
    }

    // Give a chance to user to choice an item from days list
    remoteViews.setImageViewResource(R.id.icon_image_view, R.drawable.error_icon)
    remoteViews.setTextViewText(R.id.days_text_view, "300days")
    remoteViews.setTextViewText(R.id.item_text_view, "This is title")
    // remoteViews.setTextViewText(R.id.appwidget_text, widgetText)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
}