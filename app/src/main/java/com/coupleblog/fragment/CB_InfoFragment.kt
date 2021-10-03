package com.coupleblog.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coupleblog.R
import com.coupleblog.parent.CB_BaseFragment


class CB_InfoFragment : CB_BaseFragment("InfoFragment") {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cb_info, container, false)
    }

    override fun backPressButton() {
        TODO("Not yet implemented")
    }
}