package com.coupleblog

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.coupleblog.parent.CB_BaseActivity
import com.google.firebase.FirebaseApp

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

        // Init FirebaseApp
        //FirebaseApp.initializeApp(application)
        //setSupportActionBar(binding.toolbar)

        findNavController(R.id.nav_host_fragment).apply {

            setGraph(R.navigation.nav_graph)
            addOnDestinationChangedListener{ controller, destination, arguments ->

                // MainFragment 에 오는 경우에 floating 버튼을 켜준다.
                if (destination.id == R.id.CB_MainFragment)
                {
                    binding.addFloatingButton.isVisible = true
                }
                else
                {
                    binding.addFloatingButton.isGone = false
                }
            }
        }

    }

}