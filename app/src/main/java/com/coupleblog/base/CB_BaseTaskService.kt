package com.coupleblog.base

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.coupleblog.R

abstract class CB_BaseTaskService: Service()
{
    companion object
    {
        private const val TAG = "BaseTaskService"
        private const val CHANNEL_ID_DEFAULT = "default"

        // 한번에 컴파일 되는 파일들만 접근 가능 internal(abstract class 에서 많이 쓸 것으로 추측)
        internal const val PROGRESS_NOTIFICATION_ID = 0
        internal const val FINISHED_NOTIFICATION_ID = 1

    }

    private var iTasks = 0

    // initialization when it's used
    private val notiMgr by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    fun taskStarted() = changeNumberOfTasks(1)
    fun taskEnded() = changeNumberOfTasks(-1)

    // Synchronization when it's used
    @Synchronized
    private fun changeNumberOfTasks(iDelta: Int)
    {
        Log.d(TAG, "changeNumberOfTasks iTasks:$iTasks iDelta:$iDelta")
        iTasks += iDelta

        // If there are no tasks left, stop the service
        if (iTasks <= 0)
        {
            Log.d(TAG, "stopping")
            stopSelf()
        }
    }

    private fun createDefaultChannel()
    {
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val channel = NotificationChannel(CHANNEL_ID_DEFAULT,"Default", NotificationManager.IMPORTANCE_DEFAULT)
            notiMgr.createNotificationChannel(channel)
        }
    }

    /**
     *  Show notification with a progressbar
     */
    protected fun showProgressNotification(iResCaption: Int, lEndedUnits: Long, lTotalUnits: Long)
    {
        var iPercent = 0
        if(lTotalUnits > 0)
        {
            iPercent = (100 * lEndedUnits / lTotalUnits).toInt()
        }

        createDefaultChannel()
        val builder = NotificationCompat.Builder(this, CHANNEL_ID_DEFAULT)
            .setSmallIcon(R.drawable.notification_icon)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.couple_blog))
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(iResCaption))
            .setProgress(100, iPercent, false)
            .setGroup(getString(R.string.app_name))
            .setGroupSummary(true)

        notiMgr.notify(PROGRESS_NOTIFICATION_ID, builder.build())
    }

    /**
     * Show notification that the activity finished.
     */
    @SuppressLint("UnspecifiedImmutableFlag")
    protected fun showFinishedNotification(iResCaption: Int, intent: Intent, bSuccess: Boolean)
    {
        // make pending intent for notification
        val pendingIntent = PendingIntent.getActivity(this,
            /* request Code */ 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        createDefaultChannel()
        val builder = NotificationCompat.Builder(this, CHANNEL_ID_DEFAULT)
            .setSmallIcon(R.drawable.notification_icon)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.couple_blog))
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(iResCaption))
            .setAutoCancel(true) // when you click this notification, it'll be destroyed
            .setContentIntent(pendingIntent)
            .setGroup(getString(R.string.app_name))
            .setGroupSummary(true)

        notiMgr.notify(FINISHED_NOTIFICATION_ID, builder.build())
    }

    /***
     *  Dismiss the progress notification
     */
    protected fun dismissProgressNotification() = notiMgr.cancel(PROGRESS_NOTIFICATION_ID)

    override fun onBind(intent: Intent?): IBinder? = null
}