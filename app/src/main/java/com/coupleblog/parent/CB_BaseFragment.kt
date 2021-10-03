package com.coupleblog.parent

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

abstract class CB_BaseFragment(val strTag: String) : Fragment()
{
    fun infoLog(strMsg: String) = Log.i(strTag, strMsg)
    abstract fun backPressButton()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        // backPress 처리를 Fragment에서 하도록 한다.
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                backPressButton()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
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
}