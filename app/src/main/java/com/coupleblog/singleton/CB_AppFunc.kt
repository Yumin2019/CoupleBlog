package com.coupleblog.singleton

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.database.Cursor
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.*
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
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.coupleblog.MainActivityBinding
import com.coupleblog.R
import com.coupleblog.base.CB_BaseActivity
import com.coupleblog.model.*
import com.coupleblog.util.CB_APIService
import com.coupleblog.util.CB_Response
import com.coupleblog.work.CB_NotificationWorker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
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
import kotlin.math.absoluteValue

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
        DAYS_WORKER,
        CALL_EVENT,
    }

    companion object
    {
        val strTag                           = "CB_AppFunc"
        val mainScope                        = CoroutineScope(Dispatchers.Main)
        val defaultScope                     = CoroutineScope(Dispatchers.Default)
        val networkScope                     = CoroutineScope(Dispatchers.IO)

        lateinit var application: Context
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

        var isNetworkActive = false
        private val networkCallBack = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isNetworkActive = true
            }

            override fun onLost(network: Network) {
                isNetworkActive = false
            }
        }

         fun registerNetworkCallback(context: Context) {
            val connectivityManager = getSystemService(context, ConnectivityManager::class.java)
            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()
            connectivityManager?.registerNetworkCallback(networkRequest, networkCallBack)
        }

        fun releaseNetworkCallback(context: Context) {
            val connectivityManager = getSystemService(context, ConnectivityManager::class.java)
            connectivityManager?.unregisterNetworkCallback(networkCallBack)
        }

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

        fun deleteFileFromStorage(strPath: String, strTag: String, strSuccessMsg: String, strFailMsg: String)
        {
            getStorageRef(strPath).delete()
                .addOnSuccessListener { Log.d(strTag, strSuccessMsg) }
                .addOnFailureListener { e -> Log.e(strTag, strFailMsg + "e: $e") }
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

        fun Context.isAppInForeground(): Boolean
        {
            val application = this.applicationContext
            val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningProcessList = activityManager.runningAppProcesses

            if (runningProcessList != null) {
                val myApp = runningProcessList.find { it.processName == application.packageName }
                ActivityManager.getMyMemoryState(myApp)
                return myApp?.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            }

            return false
        }

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

        /*** ?????? ???????????? ????????? ???????????? strBody??? body#delete#add#fcmToken#strEventDate
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
            // ?????? ????????? ???????????? ????????????. (??????)
            // ????????? ?????? ?????? ??? ?????? ????????? ????????? ????????????.
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
                        // ?????? ????????? ???????????? ??????. ??????????????? ?????? ????????? ????????? listener ??????
                        addEventListenerToUserInfo()

                        if(curUser.strCoupleUid.isNullOrEmpty())
                        {
                            funcSuccess?.invoke()
                            _coupleUser = CB_User()
                        }
                        else
                        {
                            // ?????? ????????? ?????? ??????
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
            // ??????????????? ???????????? ????????? UI ????????? ???????????? ????????? ????????? ????????? ????????? 0.5??? ?????????
            postDelayedUI(500, funcFirst = { cleanUpListener()
                getAuth().signOut() }, funcSecond)
        }

        /* ???????????? ??????
           1. ????????? ?????? ????????? ????????? ?????????, ?????? ????????? ????????? ????????????.
         */
        fun addEventListenerToUserInfo()
        {
            // ?????? ???????????? ?????? ?????? ???????????? ????????? ????????? ?????? ?????? ?????????.
            if(userInfoListener != null)
                return

            userInfoListener = object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot)
                {
                    _curUser = snapshot.getValue<CB_User>()
                    if(_curUser != null)
                    {
                        CB_ViewModel.curUser.postValue(curUser)

                        // ????????? ??? ????????? ???????????? ????????????.
                        addEventListenerToCoupleUserInfo()
                    }
                    else
                    {
                        _curUser = CB_User()
                    }
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
            // ????????? ?????? ????????? ???????????? listener ????????? ?????? ?????????.
            if (curUser.strCoupleUid.isNullOrEmpty())
            {
                if(coupleUserInfoListener != null && !coupleUser.strCoupleUid.isNullOrEmpty())
                {
                    // ?????? ?????? ???????????? ???????????????... ????????????.
                    cleanUpCoupleUserListener()
                }
                return
            }

            // user ??????????????? ??? ????????? ????????? ??????????????? ????????? ????????? ?????????, ?????? ????????? ??????.
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

        // ??????????????? ????????????.
        fun cleanUpListener()
        {
            presenceListener?.let { getDBInstance().getReference(".info/connected").removeEventListener(it) }
            presenceListener = null

            userInfoListener?.let { getUsersRoot().child(getUid()).removeEventListener(it) }
            userInfoListener = null

            cleanUpCoupleUserListener(false)
        }

        fun cleanUpCoupleUserListener(isClearData: Boolean = true)
        {
            coupleUserInfoListener?.let {  getUsersRoot().child(curUser.strCoupleUid!!).removeEventListener(it) }
            coupleUserInfoListener = null

            if(isClearData)
            {
                _coupleUser = CB_User() // default value
            }
        }

        fun loadDaysItem(nodeRef: DatabaseReference, strPath: String, list: ArrayList<CB_Days>, keyList: ArrayList<String>)
                = nodeRef.child(strPath).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children)
                {
                    val item = child.getValue<CB_Days>()
                    item?.let { list.add(it) }
                    child.key?.let { keyList.add(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(strTag, error.toString())
            }
        })

        fun getPostItems(nodeRef: DatabaseReference, strPath: String, list: ArrayList<CB_Post>, keyList: ArrayList<String>)
                = nodeRef.child(strPath).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children)
                {
                    val item = child.getValue<CB_Post>()
                    item?.let { list.add(it) }
                    child.key?.let { keyList.add(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(strTag, error.toString())
            }
        })

        fun getMailItems(nodeRef: DatabaseReference, strPath: String, list: ArrayList<CB_Mail>, keyList: ArrayList<String>)
                = nodeRef.child(strPath).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children)
                {
                    val item = child.getValue<CB_Mail>()
                    item?.let { list.add(it) }
                    child.key?.let { keyList.add(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(strTag, error.toString())
            }
        })

        suspend fun tryDeleteAccount() = networkScope.async {
            Log.i(strTag, "tryDeleteAccount userUid: ${getUid()}")

            // if couple, delete couple info
            if(!curUser.strCoupleKey.isNullOrEmpty())
            {
                val isDeleted = tryBreakingUp()
                if(!isDeleted)
                    return@async false
            }

            val userPostsRef = getUserPostsRoot().child(getUid())
            val postList: ArrayList<CB_Post> = arrayListOf()
            val keyList: ArrayList<String> = arrayListOf()
            getPostItems(getUserPostsRoot(), getUid(), postList, keyList)
            delay(2000)

            for(i in 0 until postList.size)
            {
                val postRef = userPostsRef.child(keyList[i])
                val commentRef = getPostCommentsRoot().child(keyList[i])
                val strImagePath = postList[i].strImgPath
                if(!strImagePath.isNullOrEmpty())
                {
                    // find upper folder for post
                    deleteFileFromStorage(strImagePath, strTag,
                        "post image deleted path:$strImagePath",
                        "post image delete failed path:$strImagePath")
                }

                postRef.setValue(null)
                commentRef.setValue(null)
                Log.d(strTag, "deleted post, postKey: ${keyList[i]}")
            }

            val userMailRef = getMailBoxRoot().child(getUid())
            val mailList: ArrayList<CB_Mail> = arrayListOf()
            keyList.clear()
            getMailItems(getMailBoxRoot(), getUid(), mailList, keyList)
            delay(2000)

            for(i in 0 until mailList.size)
            {
                val mailRef = userMailRef.child(keyList[i])
                val strImagePath = mailList[i].strImgPath
                if(!strImagePath.isNullOrEmpty())
                {
                    // find upper folder for post
                    deleteFileFromStorage(strImagePath, strTag,
                        "mail image deleted path:$strImagePath",
                        "mail image delete failed path:$strImagePath")
                }

                mailRef.setValue(null)
                Log.d(strTag, "deleted mail, mailKey: ${keyList[i]}")
            }

            val strImgPath = curUser.strImgPath
            if(!strImgPath.isNullOrEmpty())
            {
                // find upper folder for post
                deleteFileFromStorage(strImgPath, strTag,
                    "user image deleted path:$strImgPath",
                    "user image delete failed path:$strImgPath")
            }

            cleanUpListener()
            getUsersRoot().child(getUid()).setValue(null)
            FirebaseAuth.getInstance().currentUser?.delete()?.addOnCompleteListener {
                if(it.isSuccessful) {
                    Log.i(strTag, "account deleted")
                } else {
                    Log.e(strTag, "account not deleted error: ${it.exception} " +
                            "uid : ${FirebaseAuth.getInstance().currentUser?.uid}, retry")
                    FirebaseAuth.getInstance().currentUser?.delete()
                }
            }

            return@async true
        }.await()

        suspend fun tryBreakingUp() = networkScope.async {
            Log.i(strTag, "tryBreakingUp")
            if(curUser.strCoupleKey.isNullOrEmpty() || curUser.strCoupleKey.isNullOrEmpty()) {
                Log.e(strTag, "tryBreakingUp: User's not a couple")
                return@async false
            }

            val strUid = getUid()
            val strCoupleUid = curUser.strCoupleUid!!
            val strCoupleKey = curUser.strCoupleKey!!

            // root - couples ????????? ????????????. daysItem ?????? ??? ?????? ??????
            val daysList: ArrayList<CB_Days> = arrayListOf()
            val daysKeyList: ArrayList<String> = arrayListOf()
            val coupleRef = getCouplesRoot().child(strCoupleKey)

            loadDaysItem(coupleRef, "future-event-list", daysList, daysKeyList)
            loadDaysItem(coupleRef, "annual-event-list", daysList, daysKeyList)
            delay(2000)

            for(i in 0 until daysList.size) {
                cancelWorker(application, daysKeyList[i])
                cancelNotificationFCM(daysKeyList[i], coupleUser.strFcmToken!!)
            }

            Log.i(strTag, "cancel all of event")
            coupleRef.setValue(null)

            // root - users couple ????????? ????????????.
            cleanUpCoupleUserListener(false)

            sendFCM(coupleUser.strUserName!!, getString(R.string.str_widget_couple_error) + "\n" +
                    getString(R.string.str_login_again), curUser.strFcmToken!!)

            sendFCM(curUser.strUserName!!, getString(R.string.str_widget_couple_error) + "\n" +
                    getString(R.string.str_login_again), coupleUser.strFcmToken!!)

            curUser.strCoupleUid = ""
            curUser.strCoupleKey = ""

            getUsersRoot().child(strUid).setValue(curUser)
            getUsersRoot().child(strCoupleUid).updateChildren(mapOf("strCoupleUid" to "", "strCoupleKey" to ""))
            Log.i(strTag, "clear couple info")

            getSharedPref(application).edit().apply {
                putString("strCoupleKey", "")
                putString("strCoupleFcmToken", "")
            }.apply()

            return@async true
        }.await()

        fun getTimeDiffMilliseconds(eventCalendar: Calendar): Int
        {
            val curDate = getCurCalendar().time
            val eventDate = eventCalendar.time
            return ((curDate.time - eventDate.time).absoluteValue).toInt()
        }

        fun getTimeDiffMinutes(eventCalendar: Calendar): Int = (getTimeDiffMilliseconds(eventCalendar) / (60 * 1000))
        fun getTimeDiffHours(eventCalendar: Calendar): Int = (getTimeDiffMinutes(eventCalendar) / 60)
        fun getTimeDiffDays(eventCalendar: Calendar): Int = (getTimeDiffHours(eventCalendar) / 24)
        fun getTimeDiffMonths(eventCalendar: Calendar): Int = (getTimeDiffDays(eventCalendar) / 30)
        fun getTimeDiffYears(eventCalendar: Calendar): Int = (getTimeDiffMonths(eventCalendar) / 12)

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

        // Software ?????? ???????????? ???????????? ????????????.
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

        fun getString(iStrRes: Int): String { return application.getString(iStrRes) }

        fun stringToEditable(str: String): Editable = getInstance().newEditable(str)

        // ????????? ???????????? ???????????? calendar ??????
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

        /*** UTC ????????? ????????? ?????? ???????????? ???????????? Calendar ??? ??????.
         *   ?????? ????????? gmtOffset ??????
         *   @param strDate ????????? ?????? ?????? string
         */
        fun convertUtcToLocale(strDate: String): Calendar
        = Calendar.getInstance().apply {

            // UTC ???????????? calendar ??????
            time = stringToDate(strDate)

            // UTC ????????? Locale ???????????? ????????????.
            add(Calendar.MINUTE, getGMTOffset())
        }

        /*** UTC ????????? ????????? ?????? ???????????? ???????????? Calendar ??? ??????.
         *   ?????? ????????? ??????
         *   @param iGmtOffset ????????? ?????? gmt offset
         */
        fun convertUtcToLocale(iGmtOffset: Int): Calendar = convertUtcToLocale(getDateStringForSave(), iGmtOffset)

        /*** UTC ????????? ????????? ?????? ???????????? ???????????? Calendar ??? ??????.
         *   @param strDate ????????? ?????? ?????? string
         *   @param iGmtOffset ????????? ?????? gmt offset
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

        // ????????? ?????? dateString ??????
        fun getUniqueSuffix(): String = calendarToUniqueSuffix(getCalendarForSave())
        fun getDateStringForSave(): String = calendarToSaveString(getCalendarForSave())
        fun getDayStringForSave(): String = calendarToDayString(getCalendarForSave())
        fun getCalendarForSave(): Calendar
        = getCurCalendar().apply {

                // ?????? ????????? ????????? ?????? ???????????? ???????????? ????????????.
                add(Calendar.MINUTE, -getGMTOffset())
            }

        // gmt offset??? ????????????.
        fun calendarToUniqueSuffix(calendar: Calendar) = DateFormat.format("yyyyMMddHHmmss", calendar.time).toString()
        fun calendarToSaveString(calendar: Calendar) = DateFormat.format("yyyyMMddHHmm", calendar.time).toString()
        fun calendarToDayString(calendar: Calendar) = DateFormat.format("yyyyMMdd", calendar.time).toString() + "0000"
        fun getCurCalendar(): Calendar = Calendar.getInstance()

        /*** ????????? string ??? ?????????.
         *  yyyy.MM.dd. HH:mm
         *  @param calendar ?????????
         */
        fun getDateStringForOutput(calendar: Calendar): String
        {
            return if(isKorea)
            {
                // ????????? ??????
                DateFormat.format("yyyy.MM.dd. HH:mm", calendar.time).toString()
            }
            else
            {
                // ????????? ??????(?????????)
                DateFormat.format("dd MMM yyyy, HH:mm", calendar.time).toString()
            }
        }

        /*** ?????? ?????? ????????? string ??? ?????????.
         *  yyyy. MM. dd
         *  @param calendar ?????????
         */
        fun getDateStringWithoutTime(calendar: Calendar): String
        {
            return if(isKorea)
            {
                // ????????? ??????
                DateFormat.format("yyyy. MM. dd", calendar.time).toString()
            }
            else
            {
                // ????????? ??????(?????????)
                DateFormat.format("dd MMM yyyy", calendar.time).toString()
            }
        }

        /*** ??????, ?????? ?????? ????????? day string ??? ?????????.
         *  MM. dd
         *  @param calendar ?????????
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
        
        fun restartApp(activity: Activity)
        {
            val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
            val mainIntent = Intent.makeRestartActivityTask(intent!!.component)
            activity.startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
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