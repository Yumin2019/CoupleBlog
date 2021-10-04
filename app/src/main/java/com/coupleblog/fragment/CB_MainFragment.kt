package com.coupleblog.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.coupleblog.CB_AppFunc
import com.coupleblog.CB_SingleSystemMgr
import com.coupleblog.R
import com.coupleblog.parent.CB_BaseFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class CB_MainFragment : CB_BaseFragment("MainFragment") {

    private var _binding            : MainBinding? = null
    private val binding get() = _binding!!

    private lateinit var pagerAdapter: FragmentStateAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = MainBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_MainFragment
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        // 각 부분별 처리를 할 Fragment 를 가지고 adapter 를 생성한다.
        pagerAdapter = object : FragmentStateAdapter(parentFragmentManager, viewLifecycleOwner.lifecycle) {
            private val fragments = arrayOf<Fragment>()

            override fun createFragment(position: Int) = fragments[position]
            override fun getItemCount() = fragments.size
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }

    var firstTime : Long = 0
    var secondTime : Long = 0

    override fun backPressButton()
    {
        secondTime = System.currentTimeMillis()
        if(secondTime - firstTime < 2000)
        {
            ActivityCompat.finishAffinity(requireActivity())
        }
        else
        {
            CB_SingleSystemMgr.showToast(requireContext(), requireContext().getString(R.string.str_press_back_to_exit))
        }

        firstTime = secondTime
    }
}