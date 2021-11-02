package com.coupleblog.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.parent.CB_BaseFragment
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_ViewModel

class CB_ProfileFragment: CB_BaseFragment("Profilefragment")
{
    private var _binding            : ProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = ProfileBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment = this@CB_ProfileFragment
            viewModel = CB_ViewModel.Companion
        }
        return binding.root
    }

    fun profileButton(isMyProfile: Boolean)
    {
        CB_ViewModel.isMyProfile.value = isMyProfile
        beginAction(R.id.action_CB_MainFragment_to_CB_ProfileInfoFragment, R.id.CB_ProfileInfoFragment)
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