package com.coupleblog.parent

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.coupleblog.CB_AppFunc
import com.coupleblog.CB_SingleSystemMgr

open class CB_BaseActivity(val strTag : String,
                           val actFlag : CB_SingleSystemMgr.ACTIVITY_TYPE)
    : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        Log.i(strTag, "onCreate")
        CB_SingleSystemMgr.registerActivity(actFlag)
        CB_AppFunc.setStatusBarTextWhite(window)
    }

    override fun onResume()
    {
        super.onResume()
        Log.i(strTag, "onResume")
    }

    override fun onPause()
    {
        super.onPause()
        Log.i(strTag, "onResume")
    }

    override fun onDestroy()
    {
        super.onDestroy()
        Log.i(strTag, "onDestroy")
        CB_SingleSystemMgr.releaseActivity(actFlag)
    }

    override fun onBackPressed() {}

    // if you want to add other processes in exitButton, override this func
    open fun exitButton()
    {
        finish()
        CB_AppFunc.topToBottomAnimation(this)
    }

    fun infoLog(msg: String) = Log.i(strTag, msg)
}