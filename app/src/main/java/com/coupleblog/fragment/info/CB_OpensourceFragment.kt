package com.coupleblog.fragment.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.coupleblog.CB_MainActivity
import com.coupleblog.R
import com.coupleblog.base.CB_BaseFragment
import com.coupleblog.fragment.InfoBinding
import com.coupleblog.singleton.CB_ViewModel

class CB_OpensourceFragment : CB_BaseFragment()
{
    private var _binding            : InfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        CB_ViewModel.bAddButton.postValue(false)
        _binding = InfoBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            strTitle = getString(R.string.str_opensource_license)
            strText = getString(R.string.str_opensource_license_text)
        }
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        (requireActivity() as CB_MainActivity).supportActionBar?.hide()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
        (requireActivity() as CB_MainActivity).supportActionBar?.show()
    }

    override fun backPressed()
    {
        findNavController().popBackStack()
    }
}