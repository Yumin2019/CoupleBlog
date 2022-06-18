package com.coupleblog.fragment.profile

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.base.CB_BaseFragment
import com.coupleblog.dialog.CB_ItemListDialog
import com.coupleblog.dialog.CB_ChangeDialog
import com.coupleblog.dialog.CB_WarnBehaviorDialog
import com.coupleblog.dialog.DialogItem
import com.coupleblog.fragment.ProfileBinding
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel
import kotlinx.coroutines.launch

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
            R.id.action_account ->
            {
                val listItem: ArrayList<DialogItem> = arrayListOf(
                    DialogItem(getString(R.string.str_delete_account), R.drawable.error_icon,
                    callback =
                    {
                        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.WARN_BEHAVIOR_DIALOG))
                            return@DialogItem

                        CB_WarnBehaviorDialog(requireActivity(), bCancelable = false, isAccount = true)
                    }))

                if(!CB_AppFunc.curUser.strCoupleKey.isNullOrEmpty())
                {
                    listItem.add(DialogItem(getString(R.string.str_break_up), R.drawable.broken_heart,
                        callback =
                        {
                            if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.WARN_BEHAVIOR_DIALOG))
                                return@DialogItem

                            CB_WarnBehaviorDialog(requireActivity(), bCancelable = false, isAccount = false)
                        })
                    )
                }

                // email & password
                listItem.add(DialogItem(getString(R.string.str_change_email), R.drawable.email_mark,
                    callback =
                    {
                        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.CHANGE_DIALOG))
                            return@DialogItem

                        CB_ChangeDialog(requireActivity(), bCancelable = false, isPassword = false)
                    }))

                listItem.add(DialogItem(getString(R.string.str_change_password), R.drawable.lock,
                    callback =
                    {
                        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.CHANGE_DIALOG))
                            return@DialogItem

                        CB_ChangeDialog(requireActivity(), bCancelable = false, isPassword = true)
                    }))

                CB_ItemListDialog(requireActivity(), getString(R.string.str_account), listItem, true)
            }

            R.id.action_developer ->
            {
                beginAction(R.id.action_CB_MainFragment_to_CB_DeveloperFragment, R.id.CB_MainFragment)
            }

            R.id.action_opensource ->
            {
                beginAction(R.id.action_CB_MainFragment_to_CB_OpensourceFragment, R.id.CB_MainFragment)
            }

            R.id.action_logout ->
            {
                CB_AppFunc.logout {
                    findNavController().popBackStack()
                }
            }

            else -> {super.onOptionsItemSelected(item)}
        }

        return true
    }

    fun profileButton(isMyProfile: Boolean)
    {
        val uid = if(isMyProfile) CB_AppFunc.getUid() else CB_AppFunc.curUser.strCoupleUid.toString()
        beginActionToProfileInfo(beginAction = {
            beginAction(R.id.action_CB_MainFragment_to_CB_ProfileInfoFragment,
                R.id.CB_MainFragment, bundleOf(CB_ProfileInfoFragment.ARGU_UID to uid))
        }, uid)
    }

    fun callButton()
    {
        val listItem: ArrayList<DialogItem> = arrayListOf(
            DialogItem(getString(R.string.str_call), R.drawable.call,
                callback =
                {
                }),

            DialogItem(getString(R.string.str_video_call), R.drawable.camera_on,
                callback =
                {
                }))

        CB_ItemListDialog(requireActivity(), getString(R.string.str_call), listItem, true)
    }

    fun daysButton()
    {
        beginAction(R.id.action_CB_MainFragment_to_CB_DaysFragment, R.id.CB_MainFragment)
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