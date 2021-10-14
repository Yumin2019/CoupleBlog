package com.coupleblog.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.coupleblog.parent.CB_BaseFragment

class CB_MailBoxFragment: CB_BaseFragment("MailBoxFragment")
{
    private var _binding            : MailboxBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = MailboxBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_MailBoxFragment
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