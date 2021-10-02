package com.coupleblog.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import com.coupleblog.CB_SingleSystemMgr
import com.coupleblog.R

class CB_LoadingDialog(context: Context, callbackFunc: ((dialog: Dialog) -> Unit)? = null) : Dialog(context)
{
    init
    {
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.dialog_cb_loading)
        window!!.apply{
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        CB_SingleSystemMgr.registerDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG)
        callbackFunc?.invoke(this)
    }

    override fun onBackPressed() {}
    override fun dismiss()
    {
        super.dismiss()
        CB_SingleSystemMgr.releaseDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG)
    }
}