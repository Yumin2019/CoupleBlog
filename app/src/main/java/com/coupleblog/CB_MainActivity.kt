package com.coupleblog

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.coupleblog.fragment.CB_PostDetailFragment
import com.coupleblog.parent.CB_BaseActivity
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel

class CB_MainActivity : CB_BaseActivity("MainActivity", CB_SingleSystemMgr.ACTIVITY_TYPE.MAIN)
{
    lateinit var binding    : MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        binding   = DataBindingUtil.setContentView(this, R.layout.activity_cb_main)
        binding.apply {
            lifecycleOwner = this@CB_MainActivity
            activity       = this@CB_MainActivity
            viewModel      = CB_ViewModel.Companion
        }

        // Init
        CB_AppFunc.application = application
        findNavController(R.id.nav_host_fragment).setGraph(R.navigation.nav_graph)
    }

    // Add 버튼 함수 (MainFragment - MyPostLists 에서 사용한다)
    // 해당 상황이 맞는 경우에만 visible 처리가 되어 있다.
    fun newPostButton()
    {
        val navController = findNavController(R.id.nav_host_fragment)
        if(navController.currentDestination?.id != R.id.CB_MainFragment)
            return

        // we use NewPostFragment for editing and adding
        // so we pass empty string when we want to add new post
        val arguments = bundleOf(CB_PostDetailFragment.ARGU_POST_KEY to "")
        navController.navigate(R.id.action_CB_MainFragment_to_CB_NewPostFragment, arguments)
    }
}