package com.coupleblog.base

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.coupleblog.R
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr

open class CB_BaseActivity(val actFlag : CB_SingleSystemMgr.ACTIVITY_TYPE)
    : AppCompatActivity()
{
    val strTag: String = javaClass.simpleName
    fun infoLog(strMsg: String) = Log.i(strTag, strMsg)
    fun errorLog(strMsg: String) = Log.e(strTag, strMsg)
    fun debugLog(strMsg: String) = Log.d(strTag, strMsg)

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        Log.i(strTag, "onCreate")
        CB_SingleSystemMgr.registerActivity(actFlag)
        CB_AppFunc.setStatusBarTextWhite(window)
        window.statusBarColor = ContextCompat.getColor(this, R.color.purple)
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

    // if you want to add other processes in exitButton, override this func
    open fun exitButton()
    {
        finish()
        CB_AppFunc.topToBottomAnimation(this)
    }
}