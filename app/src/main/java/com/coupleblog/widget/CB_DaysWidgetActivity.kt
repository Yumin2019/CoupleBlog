package com.coupleblog.widget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.ListView
import android.widget.RemoteViews
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.coupleblog.R
import com.coupleblog.model.CB_Days
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.util.CB_DaysItemClickListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import android.content.Intent
import com.coupleblog.base.CB_BaseActivity
import com.coupleblog.singleton.CB_SingleSystemMgr

class CB_DaysWidgetActivity : CB_BaseActivity(CB_SingleSystemMgr.ACTIVITY_TYPE.DAYS_WIDGET), CB_DaysItemClickListener
{
    private var eventAdapters: ArrayList<CB_DaysWidgetListViewAdapter> = arrayListOf()
    private var coupleRef: DatabaseReference? = null
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private fun errorMsgAndExit(@StringRes iErrorMsg: Int)
    {
        CB_AppFunc.okDialog(this@CB_DaysWidgetActivity, R.string.str_information,
            iErrorMsg, R.drawable.info_icon, true,
            listener = { _, _ ->
                finish()
                CB_AppFunc.bottomToTopAnimation(this@CB_DaysWidgetActivity)
            })
        Log.d(strTag, "errorMsgAndExit")
    }

    override fun clickedDaysItem(tDays: CB_Days, strDaysKey: String)
    {
        Log.d(strTag, "clickedDaysItem")
        val widgetManager = AppWidgetManager.getInstance(this@CB_DaysWidgetActivity)
        val remoteViews = RemoteViews(packageName, R.layout.activity_cb_days_widget)

        // send data to widget
        sendBroadcast(Intent(CB_DaysWidgetProvider.DAYS_UPDATE).apply {
            putExtra("strDaysKey", strDaysKey)
            putExtra("strEventType", tDays.getEventTypeString())
        })

        widgetManager.updateAppWidget(widgetId, remoteViews)
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        finish()
        CB_AppFunc.bottomToTopAnimation(this@CB_DaysWidgetActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cb_days_widget)
        setResult(RESULT_CANCELED)
        CB_AppFunc.application = application

        // error cases
        intent.extras?.let {
            widgetId = it.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        if(widgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
        {
            errorMsgAndExit(R.string.str_widget_info_error)
            return
        }

        if(CB_AppFunc.getAuth().currentUser == null || CB_AppFunc._curUser == null)
        {
            errorMsgAndExit(R.string.str_widget_login_error)
            return
        }

        if(CB_AppFunc.curUser.strCoupleKey.isNullOrEmpty())
        {
            errorMsgAndExit(R.string.str_widget_couple_error)
            return
        }

        // load data from firebase
        coupleRef = CB_AppFunc.getCouplesRoot().child(CB_AppFunc.curUser.strCoupleKey!!)

        val queries = arrayListOf(
            coupleRef!!.child("past-event-list"),
            coupleRef!!.child("future-event-list"),
            coupleRef!!.child("annual-event-list"))

        for(i: Int in 0 until 3)
        {
            queries[i].addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot)
                {
                    val daysList = ArrayList<CB_Days>()
                    val keyList = ArrayList<String>()

                    for (child in dataSnapshot.children) {
                        val data = child.getValue<CB_Days>()
                        if(data == null){
                            Log.e(strTag, "onDataChange: child.getValue<CB_Days>() null", )
                            continue
                        }
                        daysList.add(data)
                        keyList.add(child.key!!)
                    }

                    eventAdapters.add(CB_DaysWidgetListViewAdapter(this@CB_DaysWidgetActivity, daysList, keyList))
                    Log.d(strTag, "eventAdapters added")
                }

                override fun onCancelled(databaseError: DatabaseError)
                {
                    Log.e(strTag, "onCancelled: $databaseError")
                }
            })
        }

        // wait for loading time
        CB_AppFunc.postDelayedUI(1000, null, funcSecond = {
            findViewById<ListView>(R.id.past_event_list_view).adapter = eventAdapters[0]
            findViewById<ListView>(R.id.future_event_list_view).adapter = eventAdapters[1]
            findViewById<ListView>(R.id.annual_event_list_view).adapter = eventAdapters[2]
        })
    }
}