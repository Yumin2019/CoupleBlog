package com.coupleblog.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.parent.CB_BaseFragment

class CB_PostDetailFragment: CB_BaseFragment("PostDetail")
{
    companion object
    {
        const val ARGU_POST_KEY = "postKey"
    }

    private var _binding            : PostDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = PostDetailBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_PostDetailFragment
        }
        return binding.root
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }

    override fun backPressButton()
    {
        findNavController().navigate(R.id.action_CB_InfoFragment_to_CB_RegisterFragment)
    }

}