package com.coupleblog.parent

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.R
import android.view.MotionEvent
import android.view.View.OnTouchListener


abstract class CB_BaseFragment(val strTag: String) : Fragment()
{
    fun infoLog(strMsg: String) = Log.i(strTag, strMsg)

    abstract fun backPressed()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        // backPress 처리를 Fragment에서 하도록 한다.
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                backPressed()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        infoLog("onCreate")
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        view.setOnTouchListener { v, event ->

            if (event.action == MotionEvent.ACTION_DOWN)
            {
                CB_AppFunc.clearFocusing(requireActivity())
            }
            true
        }
    }

    override fun onStart()
    {
        super.onStart()
        infoLog("onStart")
    }

    override fun onResume()
    {
        super.onResume()
        infoLog("onResume")
    }

    override fun onStop()
    {
        super.onStop()
        infoLog("onStop")
    }

    override fun onDestroy()
    {
        super.onDestroy()
        infoLog("onDestroy")
    }

    var firstTime : Long = 0
    var secondTime : Long = 0

    fun finalBackPressed()
    {
        secondTime = System.currentTimeMillis()
        if(secondTime - firstTime < 2000)
        {
            ActivityCompat.finishAffinity(requireActivity())
        }
        else
        {
            CB_SingleSystemMgr.showToast(R.string.str_press_back_to_exit)
        }

        firstTime = secondTime
    }

    fun beginAction(actionId: Int, currentId: Int, args: Bundle? = null)
    {
        val navController = findNavController()
        if(navController.currentDestination?.id != currentId)
            return

        navController.navigate(actionId, args)
    }
}