package com.coupleblog.fragment.days

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.coupleblog.base.CB_BaseFragment

class CB_NewDaysFragment : CB_BaseFragment()
{
    private var _binding            : NewDaysBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = NewDaysBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_NewDaysFragment
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