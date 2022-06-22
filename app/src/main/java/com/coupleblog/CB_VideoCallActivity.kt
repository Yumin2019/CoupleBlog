package com.coupleblog

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.coupleblog.base.CB_BaseActivity
import com.coupleblog.dialog.CB_LoadingDialog
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_SingleSystemMgr.Companion.showToast
import com.coupleblog.singleton.CB_ViewModel

class CB_VideoCallActivity : CB_BaseActivity(CB_SingleSystemMgr.ACTIVITY_TYPE.VIDEO_CALL)
{
    private lateinit var binding    : VideoCallBinding
    private lateinit var dialog     : CB_LoadingDialog

    private var firstTime : Long = 0
    private var secondTime : Long = 0

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding   = DataBindingUtil.setContentView(this, R.layout.activity_video_call)
        binding.apply {
            lifecycleOwner = this@CB_VideoCallActivity
            activity       = this@CB_VideoCallActivity
            viewModel      = CB_ViewModel.Companion
        }

        dialog = CB_LoadingDialog(this@CB_VideoCallActivity).apply { show() }

        CB_AppFunc.postDelayedUI(3000, null, funcSecond = {

            val call = false
            CB_ViewModel.isConnected.value = call
            dialog.cancel()

            if(call)
            {

            }
            else
            {
                // tried to call your lover but not connected
                callOffButton(false)
            }
        })
    }

    fun switchCamera()
    {
        if(CB_ViewModel.isConnected.value == false)
            return

        val call = true
        if(call)
        {
            val flag = !CB_ViewModel.isFrontCamera.value!!
            CB_ViewModel.isFrontCamera.postValue(flag)
            if(flag)
            {
                showToast("Front")
            }
            else
            {
                showToast("Back")
            }
        }
    }

    fun cameraButton()
    {
        if(CB_ViewModel.isConnected.value == false)
            return

        val call = true
        if(call) {
            CB_ViewModel.isCameraEnabled.value = !CB_ViewModel.isCameraEnabled.value!!
            showToast("카메라 on/off")
        }
    }

    fun micButton()
    {
        if(CB_ViewModel.isConnected.value == false)
            return

        // on off

        val call = true
        if(call) {
            CB_ViewModel.isMicEnabled.value = !CB_ViewModel.isMicEnabled.value!!
            showToast("마이크 on/off")

        }
    }

    fun callOffButton(isSelected: Boolean)
    {
        // 닫는 처리를 해준다.
        if(isSelected)
            showToast(R.string.str_call_off)
        else
            showToast(R.string.str_failed_to_call)

        exitButton()
        CB_AppFunc.postDelayedUI(500, null, funcSecond = {
            CB_ViewModel.resetVideoCallActivityLiveData()
        })

        // 통신에 대한 처리 진행
    }

    override fun onBackPressed()
    {
        secondTime = System.currentTimeMillis()
        if(secondTime - firstTime < 2000)
        {
            callOffButton(true)
        }
        else
        {
            showToast(R.string.str_press_back_to_exit)
        }

        firstTime = secondTime
    }
}