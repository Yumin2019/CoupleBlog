package com.coupleblog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.coupleblog.base.CB_BaseActivity
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import java.net.MalformedURLException
import java.net.URL
import org.jitsi.meet.sdk.*

class CB_VideoCallActivity : CB_BaseActivity(CB_SingleSystemMgr.ACTIVITY_TYPE.VIDEO_CALL)
{
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onBroadcastReceived(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)
        val strCoupleKey = CB_AppFunc.getSharedPref(this).getString("strCoupleKey", "")
        val strCoupleFcmToken = CB_AppFunc.getSharedPref(this).getString("strCoupleFcmToken", "")
        if(strCoupleKey.isNullOrEmpty() || strCoupleFcmToken.isNullOrEmpty()) {
            exitButton()
        }

        // Initialize default options for Jitsi Meet conferences.
        val serverURL: URL = try {
            // When using JaaS, replace "https://meet.jit.si" with the proper serverURL
            URL("https://meet.jit.si")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            throw RuntimeException("Invalid server URL!")
        }
        val defaultOptions = JitsiMeetConferenceOptions.Builder()
            .setServerURL(serverURL)
            // Different features flags can be set
            //.setFeatureFlag("toolbox.enabled", false)
            //.setFeatureFlag("filmstrip.enabled", false)
            .setFeatureFlag("welcomepage.enabled", false)
            .build()
        JitsiMeet.setDefaultConferenceOptions(defaultOptions)

        registerForBroadcastMessages()

        val options = JitsiMeetConferenceOptions.Builder()
            .setRoom(strCoupleKey)
            // Settings for audio and video
            //.setAudioMuted(true)
            //.setVideoMuted(true)
            .build()
        // Launch the new activity with the given options. The launch() method takes care
        // of creating the required Intent and passing the options.
        JitsiMeetActivity.launch(this, options)

        CB_AppFunc.sendFCM(
            "onVideoCallStart",
            getString(R.string.str_video_call_notification_msg),
            strCoupleFcmToken!!,
            CB_AppFunc.FCM_TYPE.CALL_EVENT
        )
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }


    private fun registerForBroadcastMessages() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BroadcastEvent.Type.CONFERENCE_TERMINATED.action)

        /* This registers for every possible event sent from JitsiMeetSDK
           If only some of the events are needed, the for loop can be replaced
           with individual statements:
           ex:  intentFilter.addAction(BroadcastEvent.Type.AUDIO_MUTED_CHANGED.action);
                ... other events
         */
        for (type in BroadcastEvent.Type.values()) {
            intentFilter.addAction(type.action)
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter)
    }

    // Example for handling different JitsiMeetSDK events
    private fun onBroadcastReceived(intent: Intent?) {
        if (intent != null) {
            val event = BroadcastEvent(intent)
            when (event.type) {
                BroadcastEvent.Type.CONFERENCE_JOINED -> Log.i("Conference Joined with url%s",
                    event.getData().get("url").toString()
                )
                BroadcastEvent.Type.PARTICIPANT_JOINED -> Log.i("Participant joined%s",
                    event.getData().get("name").toString()
                )
                BroadcastEvent.Type.CONFERENCE_TERMINATED -> exitButton()
                else -> Log.i("Received event: %s", event.type.toString())
            }
        }
    }

    // Example for sending actions to JitsiMeetSDK
    private fun hangUp() {
        val hangupBroadcastIntent: Intent = BroadcastIntentHelper.buildHangUpIntent()
        LocalBroadcastManager.getInstance(this.applicationContext).sendBroadcast(hangupBroadcastIntent)
    }
}