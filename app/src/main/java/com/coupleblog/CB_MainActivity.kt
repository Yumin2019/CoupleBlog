package com.coupleblog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.coupleblog.parent.CB_BaseActivity

class CB_MainActivity : CB_BaseActivity("MainActivity", CB_SingleSystemMgr.ACTIVITY_TYPE.MAIN)
{
    lateinit var binding    : MainActivityBinding
    lateinit var viewModel  : CB_MainViewModel

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(CB_MainViewModel::class.java)
        binding   = DataBindingUtil.setContentView(this, R.layout.activity_cb_main)
        binding.apply {
            lifecycleOwner = this@CB_MainActivity
            activity       = this@CB_MainActivity
            viewModel      = this@CB_MainActivity.viewModel
        }

    }
}