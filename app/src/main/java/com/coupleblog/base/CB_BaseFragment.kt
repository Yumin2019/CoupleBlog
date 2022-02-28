package com.coupleblog.base

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
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import com.coupleblog.fragment.profile.CB_ProfileInfoFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

abstract class CB_BaseFragment() : Fragment()
{
    val strTag: String = javaClass.simpleName
    fun infoLog(strMsg: String) = Log.i(strTag, strMsg)
    fun errorLog(strMsg: String) = Log.e(strTag, strMsg)
    fun debugLog(strMsg: String) = Log.d(strTag, strMsg)

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

    fun beginAction(@IdRes actionId: Int, @IdRes currentId: Int, args: Bundle = bundleOf())
    {
        val navController = findNavController()
        if(navController.currentDestination?.id != currentId)
            return

        navController.navigate(actionId, args)
    }

    fun beginActionToProfileInfo(beginAction: ()->Unit, profileUid: String)
    {
        CB_AppFunc.getUsersRoot().child(profileUid).addListenerForSingleValueEvent(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    beginAction.invoke()
                }
                else
                {
                    CB_AppFunc.okDialog(requireActivity(), R.string.str_information, R.string.str_failed_to_find_user,
                        R.drawable.ic_baseline_account_circle_24, false)
                    Log.e(strTag, "beginActionToProfileInfo: snapshot is null")
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
                CB_AppFunc.okDialog(requireActivity(), R.string.str_information, R.string.str_failed_to_find_user,
                    R.drawable.ic_baseline_account_circle_24, false)
                Log.e(strTag, "beginActionToProfileInfo: $error")
            }
        })
    }
}