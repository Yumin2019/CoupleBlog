package com.coupleblog.fragment.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.base.CB_BaseFragment
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.coupleblog.CB_MainActivity
import com.coupleblog.fragment.DeveloperBinding
import com.coupleblog.singleton.CB_ViewModel

class CB_DeveloperFragment : CB_BaseFragment()
{
    private var _binding            : DeveloperBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        CB_ViewModel.bAddButton.postValue(false)
        _binding = DeveloperBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment = this@CB_DeveloperFragment
            viewModel = CB_ViewModel
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

    fun instaLinkButton()
    {
        val strUrl = getString(R.string.str_insta_link)
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(strUrl)))
    }

    fun githubLinkButton()
    {
        val strUrl = getString(R.string.str_couple_blog_github_link)
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(strUrl)))
    }

    fun blogLinkButton()
    {
        val strUrl = getString(R.string.str_developer_blog_link)
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(strUrl)))
    }
}