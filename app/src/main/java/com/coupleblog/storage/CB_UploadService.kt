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
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2

enum class UPLOAD_TYPE
{
    NONE,
    PROFILE_IMAGE,
    POST_IMAGE,
    EMAIL_IMAGE,

}

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
        const val UPLOAD_TYPE_KEY = "upload_type"
        const val DATABASE_KEY = "database_key"

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
            val uploadType = intent.getIntExtra(UPLOAD_TYPE_KEY, UPLOAD_TYPE.NONE.ordinal)
            val databaseKey = intent.getStringExtra(DATABASE_KEY).toString()

            // make sure we have permission to read the data
            contentResolver.takePersistableUriPermission(fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            uploadFromUri(fileUri, uploadType, databaseKey)
        }

        return START_REDELIVER_INTENT
    }

    private fun uploadFromUri(fileUri: Uri, uploadType: Int, databaseKey: String)
    {
        Log.d(TAG, "uploadFromUri:src:$fileUri")

        val storageRef: StorageReference
        val successFunc: ()->Unit
        when(uploadType)
        {
            UPLOAD_TYPE.PROFILE_IMAGE.ordinal ->
            {
                // profileImage : users - uid - user-info - profile.jpg
                val strUid = CB_AppFunc.getUid()
                val strPath = "users/$strUid/user-info/profile.jpg"
                storageRef = CB_AppFunc.getStorage().getReference(strPath)
                successFunc =
                    {
                        with(CB_AppFunc)
                        {
                            // update user's strImgPath
                            val childUpdates = hashMapOf<String, Any>("/user/" +
                                    "$strUid/strImgPath" to strPath)
                            getDataBase().updateChildren(childUpdates)
                        }
                    }
            }

            UPLOAD_TYPE.POST_IMAGE.ordinal ->
            {
                // profileImage : users - uid - user-posts - postKey1 - image.jpg
                val strUid = CB_AppFunc.getUid()
                val strPath = "users/$strUid/user-posts/$databaseKey/image.jpg"
                storageRef = CB_AppFunc.getStorage().getReference(strPath)
                successFunc =
                    {
                        with(CB_AppFunc)
                        {
                            // update post's strImgPath
                            val childUpdates = hashMapOf<String, Any>("/user-posts/" +
                                    "$strUid/$databaseKey/strImgPath" to strPath)
                            getDataBase().updateChildren(childUpdates)
                        }
                    }
            }

            UPLOAD_TYPE.EMAIL_IMAGE.ordinal ->
            {
                // profileImage : users - uid - user-mails - mailKey1 - image.jpg
                val strUid = CB_AppFunc.getUid()
                val strPath = "users/$strUid/user-mails/$databaseKey/image.jpg"
                storageRef = CB_AppFunc.getStorage().getReference(strPath)
                successFunc =
                {
                    with(CB_AppFunc)
                    {
                        // update mail's strImgPath
                        val childUpdates = hashMapOf<String, Any>("/user-mails/" +
                                "$strUid/$databaseKey/strImgPath" to strPath)
                        getDataBase().updateChildren(childUpdates)
                    }
                }

            }

            else ->
            {
                // if it's invalid value, stop and exit
                stopSelf()
                return
            }
        }

        // mark task started
        taskStarted()
        showProgressNotification(R.string.str_uploading, 0, 0)

        storageRef.putFile(fileUri).addOnProgressListener { (bytesTransferred, totalByteCount) ->
            showProgressNotification(R.string.str_uploading, bytesTransferred, totalByteCount)
        }.

        continueWith { task ->
            if(!task.isSuccessful)
                task.exception!!.printStackTrace()

            // Request the public download url
            return@continueWith storageRef.downloadUrl
        }.

        addOnSuccessListener {
            Log.d(TAG, "uploadFromUri: getDownloadUri success")
            successFunc.invoke()
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