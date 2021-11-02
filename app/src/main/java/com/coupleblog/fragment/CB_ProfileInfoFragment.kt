package com.coupleblog.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.coupleblog.parent.CB_BaseFragment
import com.coupleblog.singleton.CB_ViewModel

class CB_ProfileInfoFragment: CB_BaseFragment("ProfileInfo")
{
    companion object
    {

    }

    private var _binding            : ProfileInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = ProfileInfoBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment = this@CB_ProfileInfoFragment
            viewModel = CB_ViewModel.Companion
        }
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }

    fun menuButton()
    {
        // if it's not my profile, it'd be not visible
    }

    override fun backPressed()
    {
        findNavController().popBackStack()
    }
}