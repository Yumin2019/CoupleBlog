package com.coupleblog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.coupleblog.fragment.CB_PostDetailFragment
import com.coupleblog.fragment.PAGE_TYPE
import com.coupleblog.parent.CB_BaseActivity
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel
import com.coupleblog.storage.CB_DownloadService
import com.coupleblog.storage.CB_UploadService
import com.google.android.material.snackbar.Snackbar

class CB_MainActivity : CB_BaseActivity("MainActivity", CB_SingleSystemMgr.ACTIVITY_TYPE.MAIN)
{
    private lateinit var binding    : MainActivityBinding
    private lateinit var broadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        binding   = DataBindingUtil.setContentView(this, R.layout.activity_cb_main)
        binding.apply {
            lifecycleOwner = this@CB_MainActivity
            activity       = this@CB_MainActivity
            viewModel      = CB_ViewModel.Companion

            setSupportActionBar(toolbar)
        }

        broadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent)
            {
                Log.d(strTag, "onReceive:$intent")
                when(intent.action)
                {
                    CB_DownloadService.DOWNLOAD_COMPLETE ->
                    {
                        CB_SingleSystemMgr.showToast(R.string.str_download_success)
                    }

                    CB_DownloadService.DOWNLOAD_ERROR ->
                    {
                        CB_SingleSystemMgr.showToast(R.string.str_download_fail)
                    }

                    CB_UploadService.UPLOAD_COMPLETE ->
                    {
                        CB_SingleSystemMgr.showToast(R.string.str_upload_success)
                    }

                    CB_UploadService.UPLOAD_ERROR ->
                    {
                        CB_SingleSystemMgr.showToast(R.string.str_upload_fail)
                    }
                }
            }
        }

        // Init
        CB_AppFunc.application = application
        CB_AppFunc.binding = binding
        findNavController(R.id.nav_host_fragment).setGraph(R.navigation.nav_graph)
    }

    override fun onDestroy()
    {
        super.onDestroy()
        CB_AppFunc.binding = null
    }

    // Add 버튼 함수 (MainFragment - MyPostLists 에서 사용한다)
    // 해당 상황이 맞는 경우에만 visible 처리가 되어 있다.
    fun floatingButton()
    {
        val navController = findNavController(R.id.nav_host_fragment)
        if(navController.currentDestination?.id != R.id.CB_MainFragment)
            return

        when(CB_ViewModel.iPageType.value)
        {
            PAGE_TYPE.MY_POSTS.ordinal ->
            {
                // we use NewPostFragment for editing and adding
                // so we pass empty string when we want to add new post
                val arguments = bundleOf(CB_PostDetailFragment.ARGU_POST_KEY to "")
                navController.navigate(R.id.action_CB_MainFragment_to_CB_NewPostFragment, arguments)
            }

            PAGE_TYPE.MAILBOX.ordinal ->
            {
                navController.navigate(R.id.action_CB_MainFragment_to_CB_NewMailFragment)
            }
            else -> {}
        }

    }
}