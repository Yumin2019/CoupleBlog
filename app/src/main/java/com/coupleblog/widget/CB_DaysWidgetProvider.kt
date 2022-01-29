package com.coupleblog.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.coupleblog.R
import com.coupleblog.model.CB_Days
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.util.setDaysTime
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

/**
 * Implementation of App Widget functionality.
 */
class CB_DaysWidgetProvider : AppWidgetProvider() {

    companion object {
        const val DAYS_UPDATE = "com.coupleblog.DAYS_UPDATE"
    }

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

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action == DAYS_UPDATE) {

            if (CB_AppFunc.getAuth().currentUser == null || CB_AppFunc._curUser == null) {
                return
            }

            if (CB_AppFunc.curUser.strCoupleKey.isNullOrEmpty()) {
                return
            }

            val strEventType = intent.getStringExtra("strEventType")!!
            val strDaysKey = intent.getStringExtra("strDaysKey")!!
            val remoteViews = RemoteViews(context?.packageName, R.layout.c_b__days_widget)

            val coupleRef = CB_AppFunc.getCouplesRoot().child(CB_AppFunc.curUser.strCoupleKey!!)
            coupleRef.child(strEventType).child(strDaysKey)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val days = dataSnapshot.getValue<CB_Days>()
                        days?.let {
                            remoteViews.apply {
                                val iResIdx = CB_AppFunc.getDrawableIdentifier(it.strIconRes!!)
                                setTextViewText(R.id.item_text_view, days.strTitle)
                                setTextViewText(R.id.days_text_view, setDaysTime(null, days))
                                if(iResIdx != 0){
                                    setImageViewResource(R.id.icon_image_view, iResIdx)
                                }
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("daysWidgetProvider", "onCancelled: $databaseError")
                    }
                })
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val remoteViews = RemoteViews(context.packageName, R.layout.c_b__days_widget)
    var hasError = false
    var strErrorText = ""

    if (CB_AppFunc.getAuth().currentUser == null || CB_AppFunc._curUser == null) {
        strErrorText = CB_AppFunc.getString(R.string.str_widget_login_error)
        hasError = true
    } else if (CB_AppFunc.curUser.strCoupleKey.isNullOrEmpty()) {
        strErrorText = CB_AppFunc.getString(R.string.str_widget_couple_error)
        hasError = true
    }

    if (hasError) {
        remoteViews.apply {
            setTextViewText(R.id.item_text_view, strErrorText)
            setViewVisibility(R.id.days_text_view, View.GONE)
            setViewVisibility(R.id.icon_image_view, View.GONE)
        }
        return
    }

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
}
