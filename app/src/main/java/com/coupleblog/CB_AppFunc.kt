package com.coupleblog

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.Editable
import android.text.Editable.Factory.getInstance
import android.text.format.DateFormat
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.coupleblog.parent.CB_BaseActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/***
 *  ALL FUNCTIONS ARE FOR ACTIVITIES
 *  CONVENIENT, GENERAL FUNCTIONS
 */

class CB_AppFunc
{
    enum class PERMISSION
    {
        CAMERA,
        RECORD_AUDIO,
        MODIFY_AUDIO_SETTINGS,
        WRITE_EXTERNAL_STORAGE,
        READ_EXTERNAL_STORAGE,
        VIBRATE,
        END
    }

    enum class STATUSBAR_TEXT_COLOR
    {
        BLACK,
        WHITE,
        NONE
    }

    companion object
    {
        val strTag                           = "CB_AppFunc"
        val mainScope                        = CoroutineScope(Dispatchers.Main)
        val defaultScope                     = CoroutineScope(Dispatchers.Default)
        val PERMISSION_REQUEST               = 100

        fun getUid() = FirebaseAuth.getInstance().currentUser!!.uid
        fun getAuth() = Firebase.auth
        fun getDataBase() = Firebase.database.reference

        // Shared Preferences: Config
        fun getSharedPref(activity: CB_BaseActivity): SharedPreferences
        {
            return activity.getSharedPreferences("Config", 0) // PRIVATE
        }

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

        @SuppressLint("SimpleDateFormat")
        val hashTimeStampFormat = SimpleDateFormat("yyyyMMddHHmmssSSS")

        @SuppressLint("SimpleDateFormat")
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")

        @SuppressLint("SimpleDateFormat")
        val timeStampFormat = SimpleDateFormat("yyyyMMdd_HHmmss")

        @SuppressLint("SimpleDateFormat")
        val dayFormat = SimpleDateFormat("20yy.MM.dd(E)")

        @SuppressLint("SimpleDateFormat")
        val dayFormatMonthDay = SimpleDateFormat("MM.dd(E)")

        @SuppressLint("SimpleDateFormat")
        val dayFormatDay = SimpleDateFormat("dd(E)")

        @SuppressLint("SimpleDateFormat")
        val timeFormat = SimpleDateFormat("a hh:mm")



        fun checkNullObject(obj: Any?, strTag: String, strMessage: String)
        {
            if (obj != null)
                return

            Log.e(strTag, strMessage)
            assert(false)
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

        fun checkPermission(context: Context, strPermission: String): Boolean
        {
            return (ContextCompat.checkSelfPermission(context, strPermission)
                    == PackageManager.PERMISSION_GRANTED)
        }

        fun checkPermission(context: Context, arrPerString: Array<String>): Boolean
        {
            for (i in arrPerString.indices)
            {
                if (ContextCompat.checkSelfPermission(context, arrPerString[i])
                    != PackageManager.PERMISSION_GRANTED)
                    return false
            }

            return true
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

        fun stringToEditable(str: String): Editable = getInstance().newEditable(str)

        // Date 값을 주면 format에 맞춰서 String을 반환.
        fun getDayString(date: Date): String = dayFormat.format(date)
        fun getTimeString(date: Date): String = timeFormat.format(date)
        fun getDayString(strDate: String): String = dayFormat.format(stringToDate(strDate)) // ~년 ~월 ~일(월) 15:00
        fun getTimeString(strDate: String): String = timeFormat.format(stringToDate(strDate))

        fun getDateStringWithoutYear(date: Date): String
        {
            return dayFormatMonthDay.format(date) + " " + getTimeString(date) // ~월 ~일(월) 15:00
        }

        fun getDateStringWithoutMonth(date: Date): String
        {
            return dayFormatDay.format(date) + " " + getTimeString(date) // ~일(월) 15:00
        }

        fun getDateString(date: Date): String
        {
            return getDayString(date) + " " + getTimeString(date)
        }

        // String 값을 Date값으로 변환
        fun stringToDate(strDate: String)
        = let {
                  val date = dateFormat.parse(strDate)
                  if(date == null)
                  {
                      Log.e(strTag, "strDate can't be parsed")
                      assert(false)
                      Date()
                  }
                  else
                  {
                      val calendar = Calendar.getInstance()
                      calendar.time = date
                      getDate(calendar)
                  }
            }

        fun dateToString(date: Date) = DateFormat.format("yyyyMMddHHmmss", date).toString()

        fun getCurDate() = Date(Calendar.getInstance().timeInMillis)
        fun getAfter30MinsDate() = Date(Calendar.getInstance().apply { add(Calendar.MINUTE, 30) }.timeInMillis)
        fun getNextHourDate() = Date(Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) }.timeInMillis)
        fun getCurDateString() = dateToString(getCurDate())
        fun getAfter30MinsDateString() = dateToString(getAfter30MinsDate())
        fun getNextHourDateString() = dateToString(getNextHourDate())

        fun getDate(calendar: Calendar) = Date(calendar.timeInMillis)

        fun dateToCalendar(date: Date): Calendar
        {
            val calendar = Calendar.getInstance()
            calendar.time = date
            return calendar
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
            activity.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)
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

        fun convertDpToPixel(dp: Float, context: Context): Float
        {
            return dp * (context.resources.displayMetrics.densityDpi.toFloat()
                    / DisplayMetrics.DENSITY_DEFAULT.toFloat())
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

        fun okDialog(context: Context, str_title: String?, str_message: String?,
                     iconId: Int?, bCancelable: Boolean, listener: DialogInterface.OnClickListener? = null)
        {
            if (CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.OK_DIALOG))
                return

            CB_SingleSystemMgr.registerDialog(CB_SingleSystemMgr.DIALOG_TYPE.OK_DIALOG)

            val dialog = MaterialAlertDialogBuilder(context)
                .setTitle(str_title)
                .setMessage(str_message)
                .setCancelable(bCancelable)
                .setPositiveButton("확인", listener)
                .setOnDismissListener {
                    CB_SingleSystemMgr.releaseDialog(CB_SingleSystemMgr.DIALOG_TYPE.OK_DIALOG)
                }

            iconId?.let { dialog.setIcon(iconId) }
            dialog.show()
        }

        fun confirmDialog(context: Context, str_title: String?, str_message: String, iconId: Int?,
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