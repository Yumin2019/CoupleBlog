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
import android.content.ComponentName
import com.coupleblog.BuildConfig

class CB_DaysWidgetProvider : AppWidgetProvider() {

    private var strDaysKey = ""
    private var strEventType = ""
    private var strCoupleKey = ""

    override fun onReceive(context: Context, intent: Intent) {
        val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, CB_DaysWidgetProvider::class.java))
        val myWidget = CB_DaysWidgetProvider()
        myWidget.onUpdate(context, AppWidgetManager.getInstance(context), ids)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        CB_AppFunc.application = context

        for (appWidgetId in appWidgetIds) {
            val intent = Intent(context, CB_MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            // load data from sharedPreferences
            CB_AppFunc.getSharedPref(context).apply {
                strEventType = getString("strEventType$appWidgetId", "") ?: ""
                strDaysKey = getString("strDaysKey$appWidgetId", "") ?: ""
                strCoupleKey = getString("strCoupleKey", "") ?: ""
            }

            Log.i("WIDGET", "strEventType : $strEventType")
            Log.i("WIDGET", "strDaysKey : $strDaysKey")
            Log.i("WIDGET", "strCoupleKey : $strCoupleKey")

            val remoteViews = RemoteViews(context.packageName, R.layout.cb_days_widget)
            var hasError = false
            var strErrorText = ""

            // error cases
           if (strCoupleKey.isEmpty()) {
                strErrorText = context.getString(R.string.str_widget_couple_error)
                hasError = true
            } else if (strEventType.isEmpty() || strDaysKey.isEmpty()) {
                strErrorText = context.getString(R.string.str_days_data_load_failed)
                hasError = true
            }

            Log.e("WIDGET", "id: $appWidgetId hasError: $hasError")
            if (hasError) {
                if(BuildConfig.DEBUG){
                    remoteViews.apply {
                        setTextViewText(R.id.item_text_view, strErrorText)
                        setViewVisibility(R.id.days_text_view, View.GONE)
                        setViewVisibility(R.id.icon_image_view, View.GONE)
                    }
                }
                continue
            }

            // set pendingIntent to container layout
            remoteViews.setOnClickPendingIntent(R.id.days_widget_container, pendingIntent)

            // load data from firebase with key
            val coupleRef = CB_AppFunc.getCouplesRoot().child(strCoupleKey)
            coupleRef.child(strEventType).child(strDaysKey)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val days = dataSnapshot.getValue<CB_Days>()
                        days?.let {
                            remoteViews.apply {
                                val iResIdx = CB_AppFunc.getDrawableIdentifier(context, it.strIconRes!!)

                                Log.i("WIDGET",  "title : " + days.strTitle!!)
                                Log.i("WIDGET",  "daystime : " + setDaysTime(null, days, false))
                                setTextViewText(R.id.item_text_view, days.strTitle)
                                setTextViewText(R.id.days_text_view, setDaysTime(null, days, false))

                                if(iResIdx != 0){
                                    setImageViewResource(R.id.icon_image_view, iResIdx)
                                }

                                Log.i("WIDGET", "id: $appWidgetId updated")
                                appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.i("WIDGET", "onCancelled : $databaseError")
                    }
                })

        }
    }
}

