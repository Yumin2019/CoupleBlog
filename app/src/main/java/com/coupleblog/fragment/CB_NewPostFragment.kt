package com.coupleblog.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.coupleblog.parent.CB_BaseFragment

class CB_NewPostFragment: CB_BaseFragment("NewPostFragment")
{
    private var _binding            : NewPostBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = NewPostBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_NewPostFragment
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

    }

    fun postButton()
    {
        // 작업중
    }

    override fun backPressed()
    {
        findNavController().popBackStack()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }
}