package com.coupleblog.fragment.profile

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.dialog.*
import com.coupleblog.model.GENDER
import com.coupleblog.base.CB_CameraBaseFragment
import com.coupleblog.fragment.EditProfileBinding
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel
import com.coupleblog.storage.UPLOAD_TYPE
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.*

class CB_EditProfileFragment : CB_CameraBaseFragment(UPLOAD_TYPE.PROFILE_IMAGE)
{
    // EDIT FIELD
    val NAME          = EDIT_FIELD_TYPE.NAME         .ordinal
    val REGION        = EDIT_FIELD_TYPE.REGION       .ordinal
    val INTRODUCTION  = EDIT_FIELD_TYPE.INTRODUCTION .ordinal
    val EDUCATION     = EDIT_FIELD_TYPE.EDUCATION    .ordinal
    val CAREER        = EDIT_FIELD_TYPE.CAREER       .ordinal
    val PHONE_NUMBER  = EDIT_FIELD_TYPE.PHONE_NUMBER .ordinal
    val FAVORITES     = EDIT_FIELD_TYPE.FAVORITES    .ordinal
    val DISLIKES      = EDIT_FIELD_TYPE.DISLIKES     .ordinal

    val IMAGE         = EDIT_FIELD_TYPE.IMAGE        .ordinal
    val EMAIL         = EDIT_FIELD_TYPE.EMAIL        .ordinal

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

    fun birthDateButton()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.DATE_PICKER))
            return

        CB_SingleSystemMgr.registerDialog(CB_SingleSystemMgr.DIALOG_TYPE.DATE_PICKER)

        MaterialDatePicker.Builder.datePicker().apply {
            // set title text
            setTitleText(R.string.str_date_of_birth)

            // enables only dates before now
            val constraints = CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now())
            setCalendarConstraints(constraints.build())
        }.

        build().apply {
            addOnPositiveButtonClickListener {
                // get the past date and save it
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = it }
                val strBirthDate = CB_AppFunc.calendarToDayString(calendar)

                with(CB_AppFunc)
                {
                    // update user's birthdate
                    curUser.strBirthDate = strBirthDate
                    getUsersRoot().child(getUid()).setValue(curUser)
                }
            }
            addOnDismissListener { CB_SingleSystemMgr.releaseDialog(CB_SingleSystemMgr.DIALOG_TYPE.DATE_PICKER) }
            show(this@CB_EditProfileFragment.requireActivity().supportFragmentManager, "birthDateButton")
        }
    }

    fun cameraButton()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.ITEM_LIST_DIALOG))
            return

        val listItem = arrayListOf(
            DialogItem(getString(R.string.str_camera), R.drawable.camera,
                callback =
                {
                    Log.i(strTag, "camera")
                    cameraLauncher.launch(imageUri)
                }),
            DialogItem(getString(R.string.str_gallery), R.drawable.image,
                callback =
                {
                    Log.i(strTag, "gallery")
                    galleryLauncher.launch("image/*")
                })
        )

        CB_ItemListDialog(requireActivity(), getString(R.string.str_change_profile_image), listItem, true)
    }

    fun imageButton(strImagePath: String)
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.IMAGE))
            return

        CB_ViewModel.strImagePath.value = strImagePath
        CB_ImageDialog(requireActivity())
    }

    fun genderButton()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.ITEM_LIST_DIALOG))
            return

        val listItem = arrayListOf(
            DialogItem(getString(R.string.str_unspecified), R.drawable.question,
                callback =
                {
                    CB_AppFunc.curUser.iGender = GENDER.NONE.ordinal
                    CB_AppFunc.getUsersRoot().child(CB_AppFunc.getUid()).setValue(CB_AppFunc.curUser)
                }),

            DialogItem(getString(R.string.str_male), R.drawable.male,
                callback =
                {
                    CB_AppFunc.curUser.iGender = GENDER.MALE.ordinal
                    CB_AppFunc.getUsersRoot().child(CB_AppFunc.getUid()).setValue(CB_AppFunc.curUser)
                }),

            DialogItem(getString(R.string.str_female), R.drawable.female,
                callback =
                {
                    CB_AppFunc.curUser.iGender = GENDER.FEMALE.ordinal
                    CB_AppFunc.getUsersRoot().child(CB_AppFunc.getUid()).setValue(CB_AppFunc.curUser)
                })
        )

        CB_ItemListDialog(requireActivity(), getString(R.string.str_gender), listItem, true)
    }

    fun showEditDialog(iType: Int)
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.EDIT_DIALOG))
            return

        val userInfo = CB_AppFunc.curUser
        val editFunc: ()->Unit = {
            // 각 필드마다 처리해야 하는 정보가 달라진다.
            val strText = CB_EditDialog.strText

            when(iType)
            {
                // 여기서 처리하는 값은 일반적인 String 처리.
                // 생년월일에 대한 처리나 성별에 대한 처리는 따로 한다.
                NAME         -> userInfo.strUserName      = strText
                REGION       -> userInfo.strRegion        = strText
                INTRODUCTION -> userInfo.strIntroduction  = strText
                EDUCATION    -> userInfo.strEducation     = strText
                CAREER       -> userInfo.strCareer        = strText
                PHONE_NUMBER -> userInfo.strPhoneNumber   = strText
                FAVORITES    -> userInfo.strFavorites     = strText
                DISLIKES     -> userInfo.strDislikes      = strText
                else -> { assert(false) { "wrong cases" } }
            }

            // update data to datebase
            CB_AppFunc.getUsersRoot().child(CB_AppFunc.getUid()).setValue(userInfo)
        }

        var iLines  = 1
        var strInit = ""
        when(iType)
        {
            NAME         ->
            {
                iLines = 1
                strInit = userInfo.strUserName.toString()
            }
            REGION       ->
            {
                iLines = 1
                strInit = userInfo.strRegion.toString()
            }
            INTRODUCTION ->
            {
                iLines = 15
                strInit = userInfo.strIntroduction.toString()
            }
            EDUCATION    ->
            {
                iLines = 5
                strInit = userInfo.strEducation.toString()
            }
            CAREER       ->
            {
                iLines = 5
                strInit = userInfo.strCareer.toString()
            }
            PHONE_NUMBER ->
            {
                iLines = 1
                strInit = userInfo.strPhoneNumber.toString()
            }
            FAVORITES    ->
            {
                iLines = 10
                strInit = userInfo.strFavorites.toString()
            }
            DISLIKES     ->
            {
                iLines = 10
                strInit = userInfo.strDislikes.toString()
            }
            else -> {}
        }

        CB_EditDialog(requireActivity(), iType, iLines, strInit, editFunc, false)
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