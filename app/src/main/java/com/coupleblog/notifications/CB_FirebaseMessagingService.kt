package com.coupleblog.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.coupleblog.CB_MainActivity
import com.coupleblog.R
import com.coupleblog.singleton.CB_AppFunc
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CB_FirebaseMessagingService : FirebaseMessagingService() {

    fun notify(strTitle: String, strContent: String)
    {
        val intent = Intent(this, CB_MainActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.default_notification_channel_id)
        val channelName = getString(R.string.default_notification_channel_name)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.couple_blog))
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(strTitle)
            .setContentText(strContent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setChannelId(channelId)
            .setGroup(getString(R.string.app_name))
            .setGroupSummary(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.enableLights(false)
            channel.enableVibration(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "onMessageReceived from : ${remoteMessage.from}")

        if (remoteMessage.data.isNullOrEmpty())
        {
            Log.e(TAG, "remoteMessage.data is null")
            return
        }

        val strTitle        = remoteMessage.data["title"] ?: ""
        val strBody         = remoteMessage.data["body"] ?: ""
        val arrText         = strBody.split('#')
        Log.d(TAG, "onMessageReceived: title: $strTitle body:$strBody")

        when(arrText[0].toInt())
        {
            CB_AppFunc.FCM_TYPE.NOTIFY.ordinal ->
            {
                // notify
                notify(strTitle, arrText[1])
            }

            CB_AppFunc.FCM_TYPE.DAYS_WORKER.ordinal ->
            {
                val strDeleteItem = arrText[2]
                val strAddItem = arrText[3]

                if(strDeleteItem != "null")
                {
                    CB_AppFunc.cancelWorker(applicationContext, strDeleteItem)
                }

                if(strAddItem != "null")
                {
                    val strFcmToken = arrText[4]
                    val strEventDate = arrText[5]
                    CB_AppFunc.requestWorker(applicationContext, strAddItem, strTitle, arrText[1], strFcmToken, strEventDate)
                }

            }
        }
    }

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        if(CB_AppFunc._curUser != null) {
            Log.d(TAG, "save fcm token to server${CB_AppFunc.getUid()}/strFcmToken = $token")
            CB_AppFunc.getUsersRoot().child(CB_AppFunc.getUid()).child("strFcmToken").setValue(token)
        }
    }

    companion object {

        private const val TAG = "MyFirebaseMsgService"
    }
}
