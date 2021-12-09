package com.coupleblog.fragment

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.base.CB_BaseFragment
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_ViewModel

class CB_ProfileFragment: CB_BaseFragment()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId)
        {
            R.id.action_logout ->
            {
                // 로그아웃을 진행한다.
                CB_AppFunc.logout {
                    // 프레그먼트를 종료시킨다.
                    findNavController().popBackStack()
                }

            }

            else -> {super.onOptionsItemSelected(item)}
        }

        return true
    }

    fun profileButton(isMyProfile: Boolean)
    {
        val uid = if(isMyProfile) CB_AppFunc.getUid() else CB_AppFunc.curUser.strCoupleUid
        beginAction(R.id.action_CB_MainFragment_to_CB_ProfileInfoFragment,
            R.id.CB_MainFragment, bundleOf(CB_ProfileInfoFragment.ARGU_UID to uid))
    }

    fun calendarButton()
    {

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