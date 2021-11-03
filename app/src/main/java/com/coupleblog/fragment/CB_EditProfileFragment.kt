package com.coupleblog.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.coupleblog.parent.CB_BaseFragment
import com.coupleblog.singleton.CB_ViewModel

class CB_EditProfileFragment : CB_BaseFragment("EditProfile") {

    private var _binding            : EditProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = EditProfileBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_EditProfileFragment
            viewModel       = CB_ViewModel.Companion
        }
        return binding.root
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }

    override fun backPressed()
    {
        findNavController().popBackStack()
    }
}