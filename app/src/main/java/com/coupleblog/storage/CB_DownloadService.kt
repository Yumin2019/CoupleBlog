package com.coupleblog.storage

import android.content.Intent
import android.content.IntentFilter
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.coupleblog.CB_MainActivity
import com.coupleblog.R
import com.coupleblog.parent.CB_BaseTaskService
import com.coupleblog.singleton.CB_AppFunc
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import java.io.File
import java.io.FileOutputStream

class CB_DownloadService: CB_BaseTaskService()
{
    companion object
    {
        const val TAG = "DownloadService"

        // ACTIONS
        const val ACTION_DOWNLOAD = "action_downloaded"
        const val DOWNLOAD_COMPLETE = "download_completed"
        const val DOWNLOAD_ERROR = "download_error"

        const val DOWNLOAD_PATH = "download_path"
        //const val BYTES_DOWNLOADED = "bytes_downloaded"

        val intentFilter: IntentFilter get()
        = IntentFilter().apply {
                addAction(DOWNLOAD_COMPLETE)
                addAction(DOWNLOAD_ERROR)
            }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    {
        Log.d(TAG, "onStartCommand:$intent:$startId")
        if(intent.action == ACTION_DOWNLOAD)
        {
            // get the path to download from the intent
            val strPath = intent.getStringExtra(DOWNLOAD_PATH)!!
            downloadFromPath(strPath)
        }

        return START_REDELIVER_INTENT
    }

    private fun downloadFromPath(strPath: String)
    {
        Log.d(TAG, "downloadFromPath:$strPath")

        // mark task started
        taskStarted()
        showProgressNotification(R.string.str_downloading, 0, 0)

        // download and get total bytes with getStream func
        CB_AppFunc.getStroageRef().child(strPath).getStream { (_, iTotalBytes), inputStream ->

            // make a file in download folder
            val file = File(getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"/$strPath")
            val outputStream = FileOutputStream(file)

            val buffer = ByteArray(1024)
            var iSize: Int = inputStream.read(buffer)
            var iBytesDownloaded: Long = 0

            while(iSize != -1)
            {
                // write data with buffer and update iBytesDownloaded
                iBytesDownloaded += iSize.toLong()
                outputStream.write(buffer)

                // update progress notification
                showProgressNotification(R.string.str_downloading, iBytesDownloaded, iTotalBytes)
                iSize = inputStream.read(buffer)
            }

            // all works were completed, close streams
            inputStream.close()
            outputStream.flush()
            outputStream.close()
        }.

        addOnSuccessListener { (_, _) ->
            Log.d(TAG, "downloadFromPath:SUCCESS")

            // send success broadcast
            broadcastDownloadFinished(true)
            showDownloadFinishedNotification(true)

            // mark task ended
            taskEnded()
        }.

        addOnFailureListener { exception ->
            exception.printStackTrace()
            Log.d(TAG, "downloadFromPath:FAIL")

            // send fail broadcast
            broadcastDownloadFinished(false)
            showDownloadFinishedNotification(false)

            // mark task ended
            taskEnded()
        }


    }

    /**
     * Broadcast finished download (success or failure).
     * @return true if a running receiver received the broadcast.
     */
    private fun broadcastDownloadFinished(bSuccess: Boolean): Boolean
    {
        val strAction = if(bSuccess) DOWNLOAD_COMPLETE else DOWNLOAD_ERROR
        return LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(strAction))
    }

    /**
     * Show a notification for a finished download.
     */
    private fun showDownloadFinishedNotification(bSuccess: Boolean)
    {
        dismissProgressNotification()

        val iCaptionStr = if(bSuccess) R.string.str_download_success else R.string.str_download_fail
        // CB_MainActivity : The component class that is to be used for the intent.
        val intent = Intent(this, CB_MainActivity::class.java)
        showFinishedNotification(iCaptionStr, intent, bSuccess)
    }

}