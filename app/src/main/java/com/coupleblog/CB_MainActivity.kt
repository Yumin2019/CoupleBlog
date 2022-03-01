package com.coupleblog

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.coupleblog.fragment.post.CB_PostDetailFragment
import com.coupleblog.fragment.PAGE_TYPE
import com.coupleblog.base.CB_BaseActivity
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel
import com.coupleblog.storage.CB_DownloadService
import com.coupleblog.storage.CB_UploadService
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

class CB_MainActivity : CB_BaseActivity(CB_SingleSystemMgr.ACTIVITY_TYPE.MAIN)
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

            if(!BuildConfig.DEBUG)
                adView.adUnitId = getString(R.string.str_admob_banner_id)

            setSupportActionBar(toolbar)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_DEFAULT)
            )
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
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        CB_AppFunc.application = application
        findNavController(R.id.nav_host_fragment).setGraph(R.navigation.nav_graph)
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

    override fun onPause()
    {
        binding.adView.pause()
        super.onPause()
    }

    override fun onResume()
    {
        binding.adView.resume()
        super.onResume()
    }

    override fun onDestroy()
    {
        binding.adView.destroy()
        super.onDestroy()
    }
}