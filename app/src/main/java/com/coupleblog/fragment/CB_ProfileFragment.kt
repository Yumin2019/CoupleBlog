package com.coupleblog.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.coupleblog.parent.CB_BaseFragment
import com.coupleblog.singleton.CB_AppFunc

class CB_ProfileFragment: CB_BaseFragment("Profilefragment")
{
    private var _binding            : ProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = ProfileBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            userData = CB_AppFunc.curUser
            coupleUserData = CB_AppFunc.coupleUser
            isCouple = !CB_AppFunc.curUser.strCoupleUid.isNullOrEmpty()
        }
        return binding.root
    }

    fun myProfile()
    {

    }

    fun coupleProfile()
    {

    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }

    override fun backPressed()
    {
        finalBackPressed()
    }

}