package com.coupleblog

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast

class CB_SingleSystemMgr
{
    // enum class for single system
    enum class ACTIVITY_TYPE
    {
        MAIN,
        ACTIVITY_TYPE_END
    }

    enum class DIALOG_TYPE
    {
        DATE_PICKER,
        // CUSTOM DIALOG
        LOADING_DIALOG,

        // MATERIAL DIALOG CUSTOM
        OK_DIALOG,
        CONFIRM_DIALOG,
        DIALOG_TYPE_END
    }

    companion object
    {
        private var isActivity = BooleanArray(ACTIVITY_TYPE.ACTIVITY_TYPE_END.ordinal)
        private var isDialog = BooleanArray(DIALOG_TYPE.DIALOG_TYPE_END.ordinal)
        var toast : Toast? = null

        private fun isToastMessage() = (toast != null)
        fun isActivity(type : ACTIVITY_TYPE) = isActivity[type.ordinal]
        fun isDialog(type : DIALOG_TYPE) = isDialog[type.ordinal]

        @SuppressLint("ShowToast")
        fun showToast(activity: Activity, text : CharSequence)
        {
            if (isToastMessage())
                toast?.cancel()

            toast = Toast.makeText(activity.applicationContext, text, Toast.LENGTH_SHORT)
            toast!!.show()
        }

        @SuppressLint("ShowToast")
        fun showToast(activity : Activity, resId : Int)
        {
            if (isToastMessage())
                toast?.cancel()

            toast = Toast.makeText(activity.applicationContext, resId, Toast.LENGTH_SHORT)
            toast!!.show()
        }

        fun registerActivity(type : ACTIVITY_TYPE)
        {
            if(type == ACTIVITY_TYPE.ACTIVITY_TYPE_END)
                return

            if(isActivity[type.ordinal])
                assert(false)

            isActivity[type.ordinal] = true
        }

        fun releaseActivity(type : ACTIVITY_TYPE)
        {
            if(type == ACTIVITY_TYPE.ACTIVITY_TYPE_END)
                return

            if(!isActivity[type.ordinal])
                assert(false)

            isActivity[type.ordinal] = false
        }

        fun registerDialog(type : DIALOG_TYPE)
        {
            if(isDialog[type.ordinal])
                assert(false)

            isDialog[type.ordinal] = true
        }

        fun releaseDialog(type : DIALOG_TYPE)
        {
            if(!isDialog[type.ordinal])
                assert(false)

            isDialog[type.ordinal] = false
        }
    }
}