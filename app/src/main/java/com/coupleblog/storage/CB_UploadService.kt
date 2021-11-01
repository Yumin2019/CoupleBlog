package com.coupleblog.storage

import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.coupleblog.CB_MainActivity
import com.coupleblog.R
import com.coupleblog.parent.CB_BaseTaskService
import com.coupleblog.singleton.CB_AppFunc
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2

class CB_UploadService: CB_BaseTaskService()
{
    companion object
    {
        const val TAG = "UploadService"

        // ACTIONS
        const val ACTION_UPLOAD = "action_upload"
        const val UPLOAD_COMPLETE = "upload_completed"
        const val UPLOAD_ERROR = "upload_error"

        const val FILE_URI = "file_uri"

        val intentFilter: IntentFilter get()
        = IntentFilter().apply {
                addAction(UPLOAD_COMPLETE)
                addAction(UPLOAD_ERROR)
            }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    {
        Log.d(TAG, "onStartCommand:$intent:$startId")
        if(intent.action == ACTION_UPLOAD)
        {
            val fileUri = intent.getParcelableExtra<Uri>(FILE_URI)!!

            // make sure we have permission to read the data
            contentResolver.takePersistableUriPermission(fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            uploadFromUri(fileUri)
        }

        return START_REDELIVER_INTENT
    }

    // 테스트
    private fun uploadFromUri(fileUri: Uri)
    {
        Log.d(TAG, "uploadFromUri:src:$fileUri")

        // mark task started
        taskStarted()
        showProgressNotification(R.string.str_uploading, 0, 0)

        // profileImage : users - uid - user-info - profile.png
        val imageRef = CB_AppFunc.getStorage().getReference("users/${CB_AppFunc.getUid()}/user-info/profile.png")
        imageRef.putFile(fileUri).addOnProgressListener { (bytesTransferred, totalByteCount) ->
            showProgressNotification(R.string.str_uploading, bytesTransferred, totalByteCount)
        }.

        continueWith { task ->

            if(!task.isSuccessful)
                throw task.exception!!

            Log.d(TAG, "uploadFromUri: upload success")

            // Request the public download url
            return@continueWith imageRef.downloadUrl
        }.

        addOnSuccessListener {
            Log.d(TAG, "uploadFromUri: getDownloadUri success")

            broadcastUploadFinished(true)
            showUploadFinishedNotification(true)
            taskEnded()
        }.

        addOnFailureListener { exception ->
            exception.printStackTrace()
            Log.d(TAG, "uploadFromUri:onFailure")

            broadcastUploadFinished(false)
            showUploadFinishedNotification(false)
            taskEnded()
        }
    }

    /**
     * Broadcast finished upload (success or failure).
     * @return true if a running receiver received the broadcast.
     */
    private fun broadcastUploadFinished(bSuccess: Boolean): Boolean
    {
        val strAction = if(bSuccess) UPLOAD_COMPLETE else UPLOAD_ERROR
        return LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(strAction))
    }

    /**
     * Show a notification for a finished upload.
     */
    private fun showUploadFinishedNotification(bSuccess: Boolean)
    {
        dismissProgressNotification()

        val intent = Intent(this, CB_MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val iCaptionStr = if(bSuccess) R.string.str_download_success else R.string.str_download_fail
        showFinishedNotification(iCaptionStr, intent, bSuccess)
    }

}