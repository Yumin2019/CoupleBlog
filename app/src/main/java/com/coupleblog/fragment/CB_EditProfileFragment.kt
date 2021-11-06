package com.coupleblog.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.dialog.CB_EditDialog
import com.coupleblog.dialog.EDIT_FIELD_TYPE
import com.coupleblog.parent.CB_BaseFragment
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
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

    fun showEditDialog(iType: Int, iLines: Int)
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.EDIT_DIALOG))
            return

        val editFunc =
        {
            // 각 필드마다 처리해야 하는 정보가 달라진다.
            val userInfo = CB_AppFunc.curUser
            val strText = CB_EditDialog.strText

            when(iType)
            {
                // 여기서 처리하는 값은 일반적인 String 처리.
                // 생년월일에 대한 처리나 성별에 대한 처리는 따로 한다.
                EDIT_FIELD_TYPE.NAME         .ordinal -> userInfo.strUserName = strText
               /* EDIT_FIELD_TYPE.IMAGE        .ordinal -> userInfo.strImage = strText 준비중
                EDIT_FIELD_TYPE.EMAIL        .ordinal -> userInfo.strUserName = strText*/
                EDIT_FIELD_TYPE.REGION       .ordinal -> userInfo.strRegion = strText
                EDIT_FIELD_TYPE.INTRODUCTION .ordinal -> userInfo.strIntroduction = strText
                EDIT_FIELD_TYPE.EDUCATION    .ordinal -> userInfo.strEducation = strText
                EDIT_FIELD_TYPE.CAREER       .ordinal -> userInfo.strCareer = strText
                EDIT_FIELD_TYPE.PHONE_NUMBER .ordinal -> userInfo.strPhoneNumber = strText
                EDIT_FIELD_TYPE.FAVORITES    .ordinal -> userInfo.strFavorites = strText
                EDIT_FIELD_TYPE.DISLIKES     .ordinal -> userInfo.strDislikes = strText
                else -> { assert(false) { "wrong cases" } }
            }

            // update data to datebase
            CB_AppFunc.getUsersRoot().child(CB_AppFunc.getUid()).setValue(userInfo)
        }

        //CB_EditDialog(requireActivity(), iType, iLines, editFunc, false)
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