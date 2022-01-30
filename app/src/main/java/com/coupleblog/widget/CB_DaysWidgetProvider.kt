package com.coupleblog.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.coupleblog.CB_MainActivity
import com.coupleblog.R
import com.coupleblog.model.CB_Days
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.util.setDaysTime
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import android.app.PendingIntent

class CB_DaysWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val intent = Intent(context, CB_MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            var strDaysKey = ""
            var strEventType = ""

            // load data from sharedPreferences
            CB_AppFunc.getSharedPref(context).apply {
                strEventType = getString("strEventType$appWidgetId", "") ?: ""
                strDaysKey = getString("strDaysKey$appWidgetId", "") ?: ""
            }

            val remoteViews = RemoteViews(context.packageName, R.layout.c_b__days_widget)
            var hasError = false
            var strErrorText = ""

            // error cases
            if (CB_AppFunc.getAuth().currentUser == null || CB_AppFunc._curUser == null) {
                strErrorText = CB_AppFunc.getString(R.string.str_widget_login_error)
                hasError = true
            } else if (CB_AppFunc.curUser.strCoupleKey.isNullOrEmpty()) {
                strErrorText = CB_AppFunc.getString(R.string.str_widget_couple_error)
                hasError = true
            } else if (strEventType.isEmpty() || strDaysKey.isEmpty()) {
                strErrorText = CB_AppFunc.getString(R.string.str_days_data_load_failed)
                hasError = true
            }

            if (hasError) {
                remoteViews.apply {
                    setTextViewText(R.id.item_text_view, strErrorText)
                    setViewVisibility(R.id.days_text_view, View.GONE)
                    setViewVisibility(R.id.icon_image_view, View.GONE)
                }
                continue
            }

            // set pendingIntent to container layout
            remoteViews.setOnClickPendingIntent(R.id.days_widget_container, pendingIntent)

            // load data from firebase with key
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

                                appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
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

