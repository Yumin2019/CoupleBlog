package com.coupleblog.widget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.StringRes
import com.coupleblog.R
import com.coupleblog.model.CB_Days
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.util.CB_DaysItemClickListener
import com.google.firebase.database.DatabaseReference
import android.content.Intent
import com.coupleblog.base.CB_BaseActivity
import com.coupleblog.singleton.CB_SingleSystemMgr
import android.app.PendingIntent
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.coupleblog.CB_MainActivity
import com.coupleblog.adapter.CB_DaysAdapter
import com.coupleblog.fragment.DaysBinding
import com.coupleblog.singleton.CB_ViewModel
import com.coupleblog.util.setDaysTime
import com.firebase.ui.database.FirebaseRecyclerOptions

class CB_DaysWidgetActivity : CB_BaseActivity(CB_SingleSystemMgr.ACTIVITY_TYPE.DAYS_WIDGET), CB_DaysItemClickListener
{
    private var eventAdapters: ArrayList<CB_DaysAdapter> = arrayListOf()
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var coupleRef: DatabaseReference

    private var _binding            : DaysBinding? = null
    private val binding get() = _binding!!

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
        val remoteViews = RemoteViews(packageName, R.layout.cb_days_widget)

        // add pendingIntent and update remote views
        val intent = Intent(this, CB_MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        remoteViews.apply {
            val iResIdx = CB_AppFunc.getDrawableIdentifier(tDays.strIconRes!!)
            setTextViewText(R.id.item_text_view, tDays.strTitle)
            setTextViewText(R.id.days_text_view, setDaysTime(null, tDays))
            if(iResIdx != 0){
                setImageViewResource(R.id.icon_image_view, iResIdx)
            }
            setOnClickPendingIntent(R.id.days_widget_container, pendingIntent)
            widgetManager.updateAppWidget(widgetId, remoteViews)
        }

        // save key and event type to sharedPreferences
        CB_AppFunc.getSharedPref(this@CB_DaysWidgetActivity).edit().apply {
            putString("strEventType$widgetId", tDays.getEventTypeString())
            putString("strDaysKey$widgetId", strDaysKey)
        }.apply()

        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        finish()
        CB_AppFunc.bottomToTopAnimation(this@CB_DaysWidgetActivity)
    }

    override fun onStart()
    {
        super.onStart()
        for(i: Int in eventAdapters.indices)
        {
            eventAdapters[i].startListening()
        }
    }

    override fun onStop()
    {
        super.onStop()
        for(i: Int in eventAdapters.indices)
        {
            eventAdapters[i].apply {
                stopListening()
                notifyItemRangeChanged(0, itemCount)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            coupleRef.child("past-event-list"),
            coupleRef.child("future-event-list"),
            coupleRef.child("annual-event-list"))

        for(i: Int in 0 until 3)
        {
            val options = FirebaseRecyclerOptions.Builder<CB_Days>()
                .setQuery(queries[i].orderByChild("iOrderIdx"), CB_Days::class.java)
                .build()

            eventAdapters.add(CB_DaysAdapter(this@CB_DaysWidgetActivity, options))
        }

        _binding = DataBindingUtil.setContentView(this@CB_DaysWidgetActivity, R.layout.fragment_cb_days)
        binding.apply {
            lifecycleOwner      = this@CB_DaysWidgetActivity
            viewModel           = CB_ViewModel.Companion
            pastEventAdapter    = eventAdapters[0]
            futureEventAdapter  = eventAdapters[1]
            annualEventAdapter  = eventAdapters[2]

            pastEventLayoutManager = LinearLayoutManager(this@CB_DaysWidgetActivity).apply {
                reverseLayout = true
                stackFromEnd  = true
            }

            futureEventLayoutManager = LinearLayoutManager(this@CB_DaysWidgetActivity).apply {
                reverseLayout = true
                stackFromEnd  = true
            }

            annualEventLayoutManager = LinearLayoutManager(this@CB_DaysWidgetActivity).apply {
                reverseLayout = true
                stackFromEnd  = true
            }
        }
    }
}