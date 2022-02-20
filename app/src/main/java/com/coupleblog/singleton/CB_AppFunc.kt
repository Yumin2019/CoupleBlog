package com.coupleblog.singleton

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.database.Cursor
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.Editable
import android.text.Editable.Factory.getInstance
import android.text.format.DateFormat
import android.util.Base64.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.AnyRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.coupleblog.MainActivityBinding
import com.coupleblog.R
import com.coupleblog.model.CB_User
import com.coupleblog.base.CB_BaseActivity
import com.coupleblog.model.CB_FCMData
import com.coupleblog.model.CB_Notification
import com.coupleblog.util.CB_APIService
import com.coupleblog.util.CB_Response
import com.coupleblog.work.CB_NotificationWorker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/***
 *  ALL FUNCTIONS ARE FOR ACTIVITIES
 *  CONVENIENT, GENERAL FUNCTIONS
 */

class CB_AppFunc
{
    enum class STATUSBAR_TEXT_COLOR
    {
        BLACK,
        WHITE,
        NONE
    }

    enum class FCM_TYPE
    {
        NOTIFY,
        DAYS_WORKER
    }

    companion object
    {
        val strTag                           = "CB_AppFunc"
        val mainScope                        = CoroutineScope(Dispatchers.Main)
        val defaultScope                     = CoroutineScope(Dispatchers.Default)
        val networkScope                     = CoroutineScope(Dispatchers.IO)

        lateinit var application: Application
        var binding: MainActivityBinding? = null

        val apiService: CB_APIService by lazy {
            Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CB_APIService::class.java)
        }

        val PERMISSION_REQUEST               = 100

        var _curUser: CB_User? = null
        val curUser get() = _curUser!!

        var _coupleUser: CB_User? = CB_User()
        val coupleUser get() = _coupleUser!!

        var userInfoListener: ValueEventListener? = null
        var coupleUserInfoListener: ValueEventListener? = null
        var presenceListener: ValueEventListener? = null

        // Firebase Realtime Database
        fun getUid() = FirebaseAuth.getInstance().currentUser!!.uid
        fun getAuth() = Firebase.auth
        fun getDataBase() = Firebase.database.reference
        fun getDBInstance() = FirebaseDatabase.getInstance()
        fun getUsersRoot() = getDataBase().child("users")
        fun getUserPostsRoot() = getDataBase().child("user-posts")
        fun getCouplesRoot() = getDataBase().child("couples")
        fun getPostCommentsRoot() = getDataBase().child("post-comments")
        fun getMailBoxRoot() = getDataBase().child("user-mails")

        // Firebase Storage
        fun getStorage() = Firebase.storage
        fun getStroageRef() = Firebase.storage.reference
        fun getStorageRef(strPath: String) = Firebase.storage.getReference(strPath)

        suspend fun deleteFileFromStorage(strPath: String, strTag: String, strSuccessMsg: String, strFailMsg: String)
        {
            getStorageRef(strPath).delete()
                .addOnSuccessListener { Log.d(strTag, strSuccessMsg) }
                .addOnFailureListener { e -> Log.e(strTag, strFailMsg + "e: $e") }.await()
        }

        @SuppressLint("ConstantLocale")
        val isKorea = (Locale.getDefault().language == "ko")

        @SuppressLint("SimpleDateFormat")
        val strSaveDateFormat = SimpleDateFormat("yyyyMMddHHmm") // 202109102201

        @SuppressLint("SimpleDateFormat")
        val strOutputDateFormatKor = SimpleDateFormat("yyyy.mm.dd. HH:mm") // 2021. 9. 6. 23:43

        @SuppressLint("SimpleDateFormat")
        val strOutputDateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm") // 17 Oct 2020, 23:43

        @SuppressLint("SimpleDateFormat")
        val strTimeOutputFormat = SimpleDateFormat("hh:mm a") // 03:20 PM

        // Shared Preferences: Config
        fun getSharedPref(activity: CB_BaseActivity): SharedPreferences
        {
            return activity.getSharedPreferences("Config", 0) // PRIVATE
        }

        fun getSharedPref(context: Context): SharedPreferences
        {
            return context.getSharedPreferences("Config", 0) // PRIVATE
        }

        fun getColorStateList(colorResId: Int): ColorStateList
        {
            return ColorStateList.valueOf(ContextCompat.getColor(application, colorResId))
        }

        fun getColorValue(colorResId: Int): Int
        {
            return ContextCompat.getColor(application, colorResId)
        }

        fun drawableToBitmap(drawableId: Int): Bitmap
        {
            return BitmapFactory.decodeResource(application.resources, drawableId)
        }

        fun bitmapToDrawable(bitmap: Bitmap): BitmapDrawable
        {
            return BitmapDrawable(application.resources, bitmap)
        }

        fun getDrawable(drawableId: Int): Drawable
        {
            return AppCompatResources.getDrawable(application, drawableId)!!
        }

        val emailRegex = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$".toRegex()


        /*   SharedPreferences(Config) Data
             strUserId          : String        default : null
             strUserPassword    : String        default : null
             bSnsUser           : Boolean       default : false
             bAutoLogin         : Boolean       default : false

             using ex :
             val pref = CB_AppFunc.getSharedPref(this@Meetings_MainActivity)
             val strUserId           = pref.getString("strUserId", null)
             val strUserPassword     = pref.getString("strUserPassword", null)
             val bSnsUser            = pref.getBoolean("bSnsUser", false)
             val bAutoLogin          = pref.getBoolean("bAutoLogin", false)
         */

        fun cancelWorker(context: Context, strDaysKey: String)
        {
            // to remove all work of this type from the queue in order to prevent duplicates.
            WorkManager.getInstance(context).cancelAllWorkByTag(strDaysKey)
        }

        fun requestWorker(context: Context, strDaysKey: String, strTitle: String?, strBody: String?, strFcmToken: String?, strEventDate: String)
        {
            val inputData = Data.Builder().apply {
                putString("strTitle", strTitle)
                putString("strBody", strBody)
                putString("strFcmToken", strFcmToken)
            }.build()

            val eventDate = stringToDate(strEventDate)
            val curDate = getCalendarForSave().time
            val interval = eventDate.time - curDate.time
            Log.i(strTag, "time interval : $interval")

            if(interval <= 0)
                return

            // You can query and cancel work by tag
            // InputData to pass to NotificationWorker
            val notificationWork = OneTimeWorkRequest.Builder(CB_NotificationWorker::class.java)
                .setInitialDelay(interval, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(strDaysKey)
                .build()

            // We can use this form to determine what happens to the existing stack
            WorkManager.getInstance(context)
                .beginUniqueWork(strDaysKey, ExistingWorkPolicy.REPLACE, notificationWork)
                .enqueue()
        }

        fun cancelNotificationFCM(strCancelDaysKey: String, strFcmToken: String)
        {
            sendFCM("title", strBody = "body#$strCancelDaysKey#null#$strFcmToken#eventDate", strFcmToken, FCM_TYPE.DAYS_WORKER)
        }

        /*** 추가 데이터가 필요한 경우라면 strBody에 body#delete#add#fcmToken#strEventDate
         */
        fun sendFCM(strTitle: String, strBody: String, strFcmToken: String, eFCMType: FCM_TYPE = FCM_TYPE.NOTIFY)
        {
            Log.d(strTag, "sendNotification strTitle:$strTitle, strBody:$strBody, strFcmToken:$strFcmToken")
            val notification = CB_Notification(
                strFcmToken,
                CB_FCMData(strTitle, "${eFCMType.ordinal}#$strBody")
            )
            apiService.sendNotification(notification)
                .enqueue(object: Callback<CB_Response> {
                    override fun onResponse(
                        call: Call<CB_Response>,
                        response: Response<CB_Response>
                    ) {
                        when(response.code()) {
                            200 -> {
                                val body = response.body()!!
                                if (body.failure > 0) {
                                    Log.e(strTag, "onFailure : retrofit notification processing failed")
                                    for(error in body.results) {
                                        Log.e(strTag, "body.results id = ${error.message_id} error: ${error.error}")
                                    }
                                }

                            }

                            400 -> {
                                Log.e(strTag, "onFailure : json parsing error")
                            }

                            401 -> {
                                Log.e(strTag, "onFailure : sender auth error")
                            }

                            else -> {

                            }
                        }

                        // https://firebase.google.com/docs/cloud-messaging/http-server-ref#send-downstream
                        Log.d(strTag, "onResponse code:${response.code()}")
                        Log.d(strTag, "resposne body:${response.body()}")
                    }

                    override fun onFailure(call: Call<CB_Response>, t: Throwable)
                    {
                        Log.e(strTag, "onFailure : retrofit notification processing failed msg:" + t.message)
                    }
                })
        }

        fun getUserPresence()
        {
            // firebase provides online connection system in .info/connected ref
            val connectedRef = getDBInstance().getReference(".info/connected")
            val isOnlineRef = getUsersRoot().child(getUid()).child("online")
            val strLogoutDateRef = getUsersRoot().child(getUid()).child("strLogoutDate")
            presenceListener = object: ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot)
                {
                    val connected = snapshot.getValue(Boolean::class.java)!!
                    if (connected)
                    {
                        // set trigger func when it's unconnected
                        isOnlineRef.onDisconnect().setValue(false)
                        strLogoutDateRef.onDisconnect().setValue(getDateStringForSave())

                        isOnlineRef.setValue(true)
                        strLogoutDateRef.setValue("")
                    }
                    else
                    {
                        isOnlineRef.setValue(false)
                        strLogoutDateRef.setValue(getDateStringForSave())
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    Log.e("onlineStatus", "failed to load online status")
                }
            }

            connectedRef.addValueEventListener(presenceListener!!)
        }

        fun getUserInfo(context: Activity, funcSuccess: (()->Unit)?, funcFailure: (() -> Unit)?)
        {
            // 유저 정보를 받아오는 함수이다. (갱신)
            // 로그인 인증 상황 및 유저 데이터 변경시 호출한다.
            getUsersRoot().child(getUid()).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    _curUser = snapshot.getValue<CB_User>()
                    if(_curUser == null)
                    {
                        Log.e(strTag, "User Info load failed")
                        okDialog(context, R.string.str_error,
                            R.string.str_user_info_load_failed, R.drawable.error_icon, true)
                        funcFailure?.invoke()
                    }
                    else
                    {
                        // 나의 정보가 존재하는 경우. 내부적으로 커플 정보가 있으면 listener 추가
                        addEventListenerToUserInfo()

                        if(curUser.strCoupleUid.isNullOrEmpty())
                        {
                            funcSuccess?.invoke()
                            _coupleUser = CB_User()
                        }
                        else
                        {
                            // 커플 정보가 있는 경우
                            getUsersRoot().child(curUser.strCoupleUid!!).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot)
                                {
                                    _coupleUser = snapshot.getValue<CB_User>()
                                    if(_coupleUser == null)
                                    {
                                        Log.e(strTag, "User Info load failed")
                                        okDialog(context, R.string.str_error,
                                            R.string.str_user_info_load_failed, R.drawable.error_icon, true)
                                        funcFailure?.invoke()
                                        return
                                    }

                                    funcSuccess?.invoke()
                                }

                                override fun onCancelled(error: DatabaseError)
                                {
                                    funcFailure?.invoke()
                                    Log.e(strTag, "couple info load failed")
                                }
                            })

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    Log.e(strTag, "User Info load cancelled", error.toException())
                    okDialog(context, R.string.str_error,
                        R.string.str_user_info_load_failed, R.drawable.error_icon, true)
                    funcFailure?.invoke()
                }
            })
        }

        fun logout(funcSecond: (() -> Unit)?)
        {
            // 간헐적으로 로그아웃 처리랑 UI 처리가 맞물려서 문제가 생기는 경우가 있어서 0.5초 딜레이
            postDelayedUI(500, funcFirst = { cleanUpListener()
                getAuth().signOut() }, funcSecond)
        }

        /* 리스너의 장점
           1. 커플이 아닌 경우든 커플인 경우든, 항상 최신의 정보를 보장한다.
         */
        fun addEventListenerToUserInfo()
        {
            // 이전 메모리가 남아 있는 경우인데 정리가 제대로 되지 않은 것이다.
            if(userInfoListener != null)
                return

            userInfoListener = object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot)
                {
                    _curUser = snapshot.getValue<CB_User>()!!
                    CB_ViewModel.curUser.postValue(curUser)

                    // 커플이 된 경우를 확인하여 추가한다.
                    addEventListenerToCoupleUserInfo()
                }

                override fun onCancelled(error: DatabaseError)
                {
                    Log.e(strTag, "User Info load cancelled", error.toException())
                }
            }

            getUsersRoot().child(getUid()).addValueEventListener(userInfoListener!!)
        }

        fun addEventListenerToCoupleUserInfo()
        {
            // 커플이 아닌 유저의 경우에는 listener 처리를 하지 않는다.
            if (curUser.strCoupleUid.isNullOrEmpty())
            {
                if(coupleUserInfoListener != null && !coupleUser.strCoupleUid.isNullOrEmpty())
                {
                    // 만약 이전 데이터가 남아있다면... 정리한다.
                    cleanUpCoupleUserListener()
                }
                return
            }

            // user 업데이트를 할 때마다 없애고 재등록하는 과정을 거치고 있는데, 그럴 필요가 없다.
            if(coupleUserInfoListener != null && !coupleUser.strUserName.isNullOrEmpty())
                return

            cleanUpCoupleUserListener()
            coupleUserInfoListener = object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot)
                {
                    _coupleUser = snapshot.getValue<CB_User>()!!
                    CB_ViewModel.coupleUser.postValue(coupleUser)
                }

                override fun onCancelled(error: DatabaseError)
                {
                    Log.e(strTag, "User Info load cancelled", error.toException())
                }
            }

            getUsersRoot().child(curUser.strCoupleUid!!).addValueEventListener(coupleUserInfoListener!!)
        }

        // 로그아웃시 호출한다.
        fun cleanUpListener()
        {
            presenceListener?.let { getDBInstance().getReference(".info/connected").removeEventListener(it) }
            presenceListener = null

            userInfoListener?.let { getUsersRoot().child(getUid()).removeEventListener(it) }
            userInfoListener = null

            cleanUpCoupleUserListener()
        }

        fun cleanUpCoupleUserListener()
        {
            coupleUserInfoListener?.let {  getUsersRoot().child(curUser.strCoupleUid!!).removeEventListener(it) }
            coupleUserInfoListener = null
            _coupleUser = CB_User() // default value
        }

        fun getResourceName(context: Context, @AnyRes iRes: Int): String
        {
            return context.resources.getResourceName(iRes)
        }

        fun getResIdentifier(context: Context, strRes: String, strResType: String): Int
        {
            val iRes = context.resources.getIdentifier(strRes.split("/")[1], strResType, context.packageName)
            if(iRes == 0) Log.e(strTag, "getResWithName: iRes value is invalid")
            return iRes
        }

        fun getDrawableIdentifier(context: Context, strDrawableRes: String): Int
        {
            val iRes = context.resources.getIdentifier(strDrawableRes.split("/")[1], "drawable", context.packageName)
            if(iRes == 0) Log.e(strTag, "getResWithName: iRes value is invalid")
            return iRes
        }

        // Software 할당 방식으로 비트맵을 설정한다.
        // java.lang.IllegalArgumentException: Software rendering doesn't support hardware bitmaps
        // https://developer.android.com/reference/android/graphics/ImageDecoder#setMutableRequired(boolean)
        fun getBitmapFromUriSoftware(imageUri: Uri): Bitmap = let {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            {
                return@let ImageDecoder.decodeBitmap(ImageDecoder.createSource(application.contentResolver, imageUri))
                { decoder: ImageDecoder, _: ImageDecoder.ImageInfo?, _: ImageDecoder.Source? ->
                    decoder.isMutableRequired = true
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            }

            BitmapDrawable(application.resources,
                MediaStore.Images.Media.getBitmap(application.contentResolver, imageUri)).bitmap
        }

        fun getBitmapFromUri(imageUri: Uri): Bitmap?
        {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(application.contentResolver, imageUri))
            }
            else
            {
                MediaStore.Images.Media.getBitmap(application.contentResolver, imageUri)
            }
        }

        fun getPathFromURI(context: Context, ContentUri: Uri): String?
        {
            var res: String? = null
            val cursor: Cursor? = context.contentResolver.query(ContentUri, null, null, null, null)
            if (cursor != null)
            {
                cursor.moveToFirst()
                try {  res = cursor.getString(cursor.getColumnIndexOrThrow("_data")) }
                catch (e: Exception) { e.printStackTrace() }
                cursor.close()
            }
            return res
        }

        fun saveBitmapToFileCache(bitmap: Bitmap, strFilePath: String)
        {
            try
            {
                val outputStream = FileOutputStream(File(strFilePath))
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }

        private fun rotateBitmap(bitmap: Bitmap, fAngle: Float): Bitmap
        = Matrix().let { matrix ->
            matrix.postRotate(fAngle)
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        fun changeImageOrientation(imageBitmap: Bitmap, exifInterface: ExifInterface): Bitmap?
        {
            try
            {
                var bitmap = imageBitmap
                val iOrientation = exifInterface
                    .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

                when (iOrientation)
                {
                    ExifInterface.ORIENTATION_ROTATE_90 ->
                        bitmap = rotateBitmap(imageBitmap, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 ->
                        bitmap = rotateBitmap(imageBitmap, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 ->
                        bitmap = rotateBitmap(imageBitmap, 270f)
                    else -> {}
                }

                return bitmap
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                return null
            }
        }

        // if default value is null, View.GONE
        fun setImageWithGlide(strStoragePath: String?, imageView: ImageView, iDefaultRes: Int?)
        {
            // load image from storage
            Log.d("bind:image_path", "strPath : $strStoragePath")
            imageView.visibility = View.VISIBLE

            if(strStoragePath.isNullOrEmpty())
            {
                if(iDefaultRes != null) imageView.setImageResource(iDefaultRes)
                else imageView.visibility = View.GONE
                return
            }

            val storageRef = getStorage().getReference(strStoragePath)
            GlideApp.with(application)
                .load(storageRef)
                .into(imageView)
        }

        fun requestPermission(activity: Activity, arrPermission: Array<String>)
        {
            ActivityCompat.requestPermissions(activity, arrPermission, PERMISSION_REQUEST)
        }

        fun postDelayedDefault(lDelayTime: Long, funcFirst : (() -> Unit)?, funcSecond : (() -> Unit)?)
        {
            defaultScope.launch {
                funcFirst?.invoke()
                delay(lDelayTime)
                funcSecond?.invoke()
            }
        }

        fun postDelayedUI(lDelayTime: Long, funcFirst : (() -> Unit)?, funcSecond : (() -> Unit)?)
        {
            mainScope.launch {
                funcFirst?.invoke()
                delay(lDelayTime)
                funcSecond?.invoke()
            }
        }

        fun postDelayedDefaultToUI(lDelayTime: Long, funcDefault : (() -> Unit)?, funcUI : (() -> Unit)?)
        {
            defaultScope.launch {

                funcDefault?.invoke()
                delay(lDelayTime)

                launch(Dispatchers.Main){
                    funcUI?.invoke()
                }.join()
            }
        }

        fun postDelayedUIToDefault(lDelayTime: Long, funcUI : (() -> Unit)?, funcDefault : (() -> Unit)?)
        {
            mainScope.launch {

                funcUI?.invoke()
                delay(lDelayTime)

                launch(Dispatchers.Default){
                    funcDefault?.invoke()
                }.join()
            }
        }

        fun checkPermission(strPermission: String): Boolean
        {
            return (ContextCompat.checkSelfPermission(application, strPermission)
                    == PackageManager.PERMISSION_GRANTED)
        }

        fun checkPermission(permissions: Array<String>): Boolean
        {
            for (i in permissions.indices)
            {
                if (ContextCompat.checkSelfPermission(application, permissions[i])
                    != PackageManager.PERMISSION_GRANTED)
                    return false
            }
            return true
        }

        fun requestPermission(activity: Activity, strPermission: String)
        {
            ActivityCompat.requestPermissions(activity, arrayOf(strPermission), PERMISSION_REQUEST)
        }

        val stickerItemSize: Int by lazy { convertDpToPixel(50) }
        val stickerOptions: BitmapFactory.Options by lazy {
            BitmapFactory.Options().apply {
                // First decode with inJustDecodeBounds=true to check dimensions
                inJustDecodeBounds = true
                BitmapFactory.decodeResource(application.resources, R.drawable.android,this)

                // Calculate inSampleSize
                inSampleSize = calculateInSampleSize(this, stickerItemSize, stickerItemSize)

                // Decode bitmap with inSampleSize set
                inJustDecodeBounds = false
            }
        }

        val stickerList: IntArray by lazy {
            intArrayOf(
                R.drawable.android,
                R.drawable.arch_1,
                R.drawable.arch_2,
                R.drawable.arch_3,
                R.drawable.arch_4,
                R.drawable.ballon_1,
                R.drawable.ballon_2,
                R.drawable.bear_1,
                R.drawable.bear_2,
                R.drawable.bed_1,
                R.drawable.bed_2,
                R.drawable.bed_3,
                R.drawable.bed_4,
                R.drawable.bed_5,
                R.drawable.bed_6,
                R.drawable.bed_7,
                R.drawable.bikini_1,
                R.drawable.bikini_2,
                R.drawable.bird_1,
                R.drawable.bird_2,
                R.drawable.bird_3,
                R.drawable.bird_4,
                R.drawable.bird_5,
                R.drawable.bird_7,
                R.drawable.bouquet_1,
                R.drawable.bouquet_2,
                R.drawable.bouquet_3,
                R.drawable.bouquet_4,
                R.drawable.bride_1,
                R.drawable.bride_2,
                R.drawable.bride_3,
                R.drawable.cake_1,
                R.drawable.cake_2,
                R.drawable.cake_3,
                R.drawable.cake_4,
                R.drawable.cake_5,
                R.drawable.cake_6,
                R.drawable.camera_1,
                R.drawable.camera_2,
                R.drawable.candle,
                R.drawable.christmas_1,
                R.drawable.christmas_2,
                R.drawable.christmas_3,
                R.drawable.christmas_4,
                R.drawable.clover,
                R.drawable.condom_1,
                R.drawable.condom_2,
                R.drawable.abstinence,
                R.drawable.costume_1,
                R.drawable.costume_2,
                R.drawable.costume_3,
                R.drawable.costume_4,
                R.drawable.costume_5,
                R.drawable.costume_6,
                R.drawable.costume_7,
                R.drawable.couple_1,
                R.drawable.couple_2,
                R.drawable.couple_3,
                R.drawable.couple_5,
                R.drawable.couple_6,
                R.drawable.couple_7,
                R.drawable.couple_8,
                R.drawable.couple_blog,
                R.drawable.diamond_1,
                R.drawable.diamond_2,
                R.drawable.diamond_3,
                R.drawable.dinner_1,
                R.drawable.dinner_2,
                R.drawable.dinner_3,
                R.drawable.dress_1,
                R.drawable.dress_2,
                R.drawable.dress_3,
                R.drawable.dress_4,
                R.drawable.dress_5,
                R.drawable.dress_6,
                R.drawable.dress_7,
                R.drawable.drink_1,
                R.drawable.drink_2,
                R.drawable.drink_3,
                R.drawable.family,
                R.drawable.flower_1,
                R.drawable.flower_10,
                R.drawable.flower_11,
                R.drawable.flower_2,
                R.drawable.flower_3,
                R.drawable.flower_4,
                R.drawable.flower_5,
                R.drawable.flower_6,
                R.drawable.flower_7,
                R.drawable.flower_8,
                R.drawable.flower_9,
                R.drawable.gender,
                R.drawable.gift_1,
                R.drawable.gift_10,
                R.drawable.gift_11,
                R.drawable.gift_2,
                R.drawable.gift_3,
                R.drawable.gift_4,
                R.drawable.gift_5,
                R.drawable.gift_6,
                R.drawable.gift_7,
                R.drawable.gift_8,
                R.drawable.gift_9,
                R.drawable.girl,
                R.drawable.groom_1,
                R.drawable.groom_2,
                R.drawable.groom_3,
                R.drawable.hand,
                R.drawable.heart_1,
                R.drawable.heart_2,
                R.drawable.heart_3,
                R.drawable.heart_4,
                R.drawable.heart_5,
                R.drawable.heart_6,
                R.drawable.heart_7,
                R.drawable.heart_8,
                R.drawable.heels_1,
                R.drawable.heels_2,
                R.drawable.house_1,
                R.drawable.house_2,
                R.drawable.ice_cream_1,
                R.drawable.ice_cream_2,
                R.drawable.just_married,
                R.drawable.key_1,
                R.drawable.key_2,
                R.drawable.key_3,
                R.drawable.kiss_1,
                R.drawable.kiss_2,
                R.drawable.kiss_3,
                R.drawable.letter_1,
                R.drawable.letter_2,
                R.drawable.letter_3,
                R.drawable.lipstick,
                R.drawable.money_1,
                R.drawable.money_2,
                R.drawable.music_1,
                R.drawable.music_2,
                R.drawable.necklace_1,
                R.drawable.necklace_2,
                R.drawable.necklace_3,
                R.drawable.necklace_4,
                R.drawable.necklace_5,
                R.drawable.necklace_6,
                R.drawable.pregnant,
                R.drawable.ring_1,
                R.drawable.ring_2,
                R.drawable.ring_3,
                R.drawable.ring_4,
                R.drawable.ring_5,
                R.drawable.ring_6,
                R.drawable.ring_7,
                R.drawable.ring_8,
                R.drawable.ring_9,
                R.drawable.rose_1,
                R.drawable.rose_2,
                R.drawable.rose_3,
                R.drawable.suit,
                R.drawable.sweet_1,
                R.drawable.sweet_2,
                R.drawable.sweet_3,
                R.drawable.sweet_4,
                R.drawable.sweet_5,
                R.drawable.ticket,
                R.drawable.time,
                R.drawable.travel_1,
                R.drawable.travel_2,
                R.drawable.travel_3,
                R.drawable.video_1,
                R.drawable.video_2,
                R.drawable.video_3,
                R.drawable.video_4,
                R.drawable.wine_1,
                R.drawable.wine_2,
                R.drawable.wine_3,
                R.drawable.wine_4,
                R.drawable.wine_5
            )
        }

        fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int
        {
            // Raw height and width of image
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1
            if (height > reqHeight || width > reqWidth)
            {
                val halfHeight = height / 2
                val halfWidth = width / 2

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth)
                    inSampleSize *= 2
            }
            return inSampleSize
        }

     /*   fun requestPermissionAll(activity: Activity)
        {
            ActivityCompat.requestPermissions(activity, arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.VIBRATE,
                ), PERMISSION_REQUEST)
        }

        fun getPermissionString(): Array<String>
        {
            return arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.VIBRATE
            )
        }





        fun checkEssentialPermission(context: Context): Boolean
        {
            return (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.MODIFY_AUDIO_SETTINGS)
                    == PackageManager.PERMISSION_GRANTED)
        }

        fun checkPermissionAll(context: Context): Boolean
        {
            return (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.MODIFY_AUDIO_SETTINGS)
                    == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.VIBRATE)
                    == PackageManager.PERMISSION_GRANTED
                    )
        }

        fun getPathFromURI(context: Context, ContentUri: Uri): String?
        {
            var res: String? = null
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            val cursor: Cursor? = context.contentResolver
                .query(ContentUri, proj, null, null, null)
            if (cursor != null)
            {
                cursor.moveToFirst()
                res = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                cursor.close()
            }
            return res
        }

        fun setBitmapFromPath(fullPath: String, imageView: ImageView)
        {
            val imgFile = File(fullPath)
            if (imgFile.exists())
            {
                val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                imageView.setImageBitmap(myBitmap)
            }
        }

        fun RotateBitmap(bitmap: Bitmap, fAngle: Float): Bitmap?
        = Matrix().let { matrix ->

            matrix.postRotate(fAngle)
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        fun saveBitmapToFileCache(bitmap: Bitmap, strFilePath: String)
        {
            try
            {
                val outputStream = FileOutputStream(File(strFilePath))
                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream)
                outputStream.close()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }*/

        fun getString(iStrRes: Int): String { return application.getString(iStrRes) }

        fun stringToEditable(str: String): Editable = getInstance().newEditable(str)

        // 저장된 데이터를 파싱해서 calendar 반환
        fun stringToCalendar(strDate: String?): Calendar
        = let {

            if (strDate == null || strDate.isEmpty())
            {
                Log.e(strTag, "strDate can't be parsed")
                return@let getCurCalendar()
            }

            val date = strSaveDateFormat.parse(strDate)
            val calendar = getCurCalendar()
            calendar.time = date!!
            calendar
        }

        // get gmt offset, return value is minutes
        fun getGMTOffset(): Int
        {
            val calendar = Calendar.getInstance(Locale.getDefault())
            return (calendar[Calendar.ZONE_OFFSET] + calendar[Calendar.DST_OFFSET]) / (60 * 1000)
        }

        /*** UTC 기준의 시간을 로컬 시간으로 변환하여 Calendar 로 전달.
         *   실행 기기의 gmtOffset 사용
         *   @param strDate 기준이 되는 시간 string
         */
        fun convertUtcToLocale(strDate: String): Calendar
        = Calendar.getInstance().apply {

            // UTC 기준으로 calendar 생성
            time = stringToDate(strDate)

            // UTC 기준을 Locale 기준으로 교체한다.
            add(Calendar.MINUTE, getGMTOffset())
        }

        /*** UTC 기준의 시간을 로컬 시간으로 변환하여 Calendar 로 전달.
         *   현재 시간을 사용
         *   @param iGmtOffset 기준이 되는 gmt offset
         */
        fun convertUtcToLocale(iGmtOffset: Int): Calendar = convertUtcToLocale(getDateStringForSave(), iGmtOffset)

        /*** UTC 기준의 시간을 로컬 시간으로 변환하여 Calendar 로 전달.
         *   @param strDate 기준이 되는 시간 string
         *   @param iGmtOffset 기준이 되는 gmt offset
         */
        fun convertUtcToLocale(strDate: String, iGmtOffset: Int): Calendar
                = Calendar.getInstance().apply {

            time = stringToDate(strDate)
            add(Calendar.MINUTE, iGmtOffset)
        }

        fun stringToDate(strDate: String?): Date
        = let {

            if (strDate == null || strDate.isEmpty())
            {
                Log.e(strTag, "strDate can't be parsed")
                return@let Date()
            }

            return@let strSaveDateFormat.parse(strDate)!!
        }

        fun String?.toDate(): Date
        {
            if (this@toDate.isNullOrEmpty())
            {
                Log.e(strTag, "strDate can't be parsed")
                return Date()
            }

            return strSaveDateFormat.parse(this@toDate)!!
        }

        fun String?.toCalendar(): Calendar
        {
            if(this@toCalendar.isNullOrEmpty())
            {
                Log.e(strTag, "stringToCalendar can't be parsed")
                return getCurCalendar()
            }

            val date = strSaveDateFormat.parse(this@toCalendar)
            val calendar = getCurCalendar()
            calendar.time = date!!
            return calendar
        }

        // 저장을 위한 dateString 얻기
        fun getUniqueSuffix(): String = calendarToUniqueSuffix(getCalendarForSave())
        fun getDateStringForSave(): String = calendarToSaveString(getCalendarForSave())
        fun getDayStringForSave(): String = calendarToDayString(getCalendarForSave())
        fun getCalendarForSave(): Calendar
        = getCurCalendar().apply {

                // 로컬 기준의 시간을 영국 기준으로 변경하여 저장한다.
                add(Calendar.MINUTE, -getGMTOffset())
            }

        // gmt offset을 고려한다.
        fun calendarToUniqueSuffix(calendar: Calendar) = DateFormat.format("yyyyMMddHHmmss", calendar.time).toString()
        fun calendarToSaveString(calendar: Calendar) = DateFormat.format("yyyyMMddHHmm", calendar.time).toString()
        fun calendarToDayString(calendar: Calendar) = DateFormat.format("yyyyMMdd", calendar.time).toString() + "0000"
        fun getCurCalendar(): Calendar = Calendar.getInstance()

        /*** 출력용 string 을 얻는다.
         *  yyyy.MM.dd. HH:mm
         *  @param calendar 캘린더
         */
        fun getDateStringForOutput(calendar: Calendar): String
        {
            return if(isKorea)
            {
                // 한국식 표기
                DateFormat.format("yyyy.MM.dd. HH:mm", calendar.time).toString()
            }
            else
            {
                // 영국식 표기(나머지)
                DateFormat.format("dd MMM yyyy, HH:mm", calendar.time).toString()
            }
        }

        /*** 시간 없이 출력용 string 을 얻는다.
         *  yyyy. MM. dd
         *  @param calendar 캘린더
         */
        fun getDateStringWithoutTime(calendar: Calendar): String
        {
            return if(isKorea)
            {
                // 한국식 표기
                DateFormat.format("yyyy. MM. dd", calendar.time).toString()
            }
            else
            {
                // 영국식 표기(나머지)
                DateFormat.format("dd MMM yyyy", calendar.time).toString()
            }
        }

        /*** 연도, 시간 없이 출력용 day string 을 구한다.
         *  MM. dd
         *  @param calendar 캘린더
         */
        fun getDayStringForOutput(calendar: Calendar): String
        {
            return if(isKorea)
            {
                DateFormat.format("MM. dd", calendar.time).toString()
            }
            else
            {
                DateFormat.format("dd MMM", calendar.time).toString()
            }
        }

        @RequiresApi(Build.VERSION_CODES.M)
        fun changeStatusBarColor(activity: Activity, colorResId: Int)
        {
            activity.window.statusBarColor = activity.getColor(colorResId)
        }

        fun changeStatusBarColorTransparent(activity: Activity)
        {
            activity.window.statusBarColor = Color.TRANSPARENT
        }

        // without data
        fun <T> toActivity(activity: CB_BaseActivity, cls: Class<T>, callback: ((intent: Intent) -> Unit)?)
        {
            val intent = Intent(activity, cls)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            callback?.invoke(intent)
            activity.startActivity(intent)
            activity.finish()
            topToBottomAnimation(activity)
        }

        fun rightToLeftAnimation(activity: Activity)
        {
            activity.overridePendingTransition(
                R.anim.anim_slide_in_right,
                R.anim.anim_slide_out_left
            )
        }

        fun bottomToTopAnimation(activity: Activity)
        {
            activity.overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_stop)
        }

        fun topToBottomAnimation(activity: Activity)
        {
            activity.overridePendingTransition(R.anim.anim_stop, R.anim.anim_slide_out_bottom)
        }

        fun stopAnimation(activity: Activity)
        {
            activity.overridePendingTransition(R.anim.anim_stop, R.anim.anim_stop)
        }

        fun convertDpToPixel(dp: Int): Int
        {
            return dp * (application.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
        }

        fun clearFocusing(activity: Activity)
        {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
            activity.currentFocus?.clearFocus()
        }

        fun openIME(editText: EditText, context: Context)
        {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            editText.requestFocus()
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }

        // replace string to resId
        fun okDialog(context: Activity, @StringRes iResTitle: Int, @StringRes iResMessage:Int,
                     @DrawableRes iconId: Int?, bCancelable: Boolean, listener: DialogInterface.OnClickListener? = null)
        {
            okDialog(context, getString(iResTitle), getString(iResMessage), iconId, bCancelable, listener)
        }

        fun okDialog(context: Activity, str_title: String?, str_message: String?,
                     @DrawableRes iconId: Int?, bCancelable: Boolean, listener: DialogInterface.OnClickListener? = null)
        {
            if (CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.OK_DIALOG))
                return

            CB_SingleSystemMgr.registerDialog(CB_SingleSystemMgr.DIALOG_TYPE.OK_DIALOG)

            val dialog = MaterialAlertDialogBuilder(context)
                .setTitle(str_title)
                .setMessage(str_message)
                .setCancelable(bCancelable)
                .setPositiveButton(getString(R.string.str_ok), listener)
                .setOnDismissListener {
                    CB_SingleSystemMgr.releaseDialog(CB_SingleSystemMgr.DIALOG_TYPE.OK_DIALOG)
                }

            iconId?.let { dialog.setIcon(iconId) }
            dialog.show()
        }

        // replace string to resId
        fun confirmDialog(context: Context, @StringRes iResTitle: Int, @StringRes iResMessage: Int, @DrawableRes iconId: Int?,
                          bCancelable: Boolean, @StringRes iYesText: Int, yesListener: DialogInterface.OnClickListener?,
                          @StringRes iNoText: Int, noListener: DialogInterface.OnClickListener?)
        {
            confirmDialog(context, getString(iResTitle), getString(iResMessage), iconId, bCancelable,
            getString(iYesText), yesListener, getString(iNoText), noListener)
        }

        fun confirmDialog(context: Context, str_title: String?, str_message: String, @DrawableRes iconId: Int?,
                          bCancelable: Boolean, yesText: String, yesListener: DialogInterface.OnClickListener?,
                          noText: String, noListener: DialogInterface.OnClickListener?)
        {
            if (CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.CONFIRM_DIALOG))
                return

            CB_SingleSystemMgr.registerDialog(CB_SingleSystemMgr.DIALOG_TYPE.CONFIRM_DIALOG)

            val dialog = MaterialAlertDialogBuilder(context)
                .setTitle(str_title)
                .setMessage(str_message)
                .setCancelable(bCancelable)
                .setPositiveButton(yesText, yesListener)
                .setNegativeButton(noText, noListener)
                .setOnDismissListener {
                    CB_SingleSystemMgr.releaseDialog(CB_SingleSystemMgr.DIALOG_TYPE.CONFIRM_DIALOG)
                }

            iconId?.let { dialog.setIcon(iconId) }
            dialog.show()
        }

        fun confirmDialog(context: Context, str_title: String?, str_message: String, strImgStoragePath: String?,
                          @DrawableRes iDefaultIcon: Int, bCancelable: Boolean, yesText: String, yesListener: DialogInterface.OnClickListener?,
                          noText: String, noListener: DialogInterface.OnClickListener?)
        {
            if (CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.CONFIRM_DIALOG))
                return

            CB_SingleSystemMgr.registerDialog(CB_SingleSystemMgr.DIALOG_TYPE.CONFIRM_DIALOG)

            val dialog = MaterialAlertDialogBuilder(context)
                .setTitle(str_title)
                .setMessage(str_message)
                .setCancelable(bCancelable)
                .setPositiveButton(yesText, yesListener)
                .setNegativeButton(noText, noListener)
                .setOnDismissListener {
                    CB_SingleSystemMgr.releaseDialog(CB_SingleSystemMgr.DIALOG_TYPE.CONFIRM_DIALOG)
                }

            if(strImgStoragePath.isNullOrEmpty())
            {
                dialog.setIcon(iDefaultIcon)
                dialog.show()
            }
            else
            {
                // getImage from storage
                val imageRef = getStorageRef(strImgStoragePath)
                GlideApp.with(application)
                    .asBitmap()
                    .load(imageRef)
                    .into(object: CustomTarget<Bitmap>()
                    {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?)
                        {
                            dialog.setIcon(bitmapToDrawable(resource))
                            dialog.show()
                        }
                        override fun onLoadCleared(placeholder: Drawable?){}
                    })
            }
        }

        @SuppressLint("SourceLockedOrientationActivity")
        fun setOrientationPortrait(activity: Activity) {
            try {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            } catch (e: IllegalStateException) {
            }
        }

        fun setStatusBarTextColor(activity: Activity, statusBarTextColor: STATUSBAR_TEXT_COLOR)
        {
            when (statusBarTextColor)
            {
                STATUSBAR_TEXT_COLOR.BLACK ->
                {
                    setStatusBarTextBlack(activity.window)
                }

                STATUSBAR_TEXT_COLOR.WHITE ->
                {
                    setStatusBarTextWhite(activity.window)
                }

                STATUSBAR_TEXT_COLOR.NONE ->
                {
                }
            }
        }

        fun setStatusBarTextWhite(window: Window)
        {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT

            setStatusBarTextBlack(window)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                var flags: Int = window.decorView.systemUiVisibility
                flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                flags = (flags xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                window.decorView.systemUiVisibility = flags
            }
        }

        fun setStatusBarTextBlack(window: Window)
        {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                var flags: Int = window.decorView.systemUiVisibility
                flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                flags = (flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                window.decorView.systemUiVisibility = flags
            }
        }
    }
}