package com.coupleblog.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import com.coupleblog.R
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr

enum class EDIT_FIELD_TYPE
{
    NAME,
    IMAGE,
    EMAIL,
    GENDER,
    DATE_OF_BIRTH,
    REGION,
    INTRODUCTION,
    EDUCATION,
    CAREER,
    PHONE_NUMBER,
    FAVORITES,
    DISLIKES,

    RESET_PASSWORD_EMAIL
}

class CB_EditDialog(context: Activity, iType: Int, iLines: Int, strInit: String,
                    editFunc: ()->Unit,  bCancelable: Boolean) : Dialog(context)
{
    companion object
    {
        var strText = ""
    }

    init
    {
        val binding: EditDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
            R.layout.dialog_cb_edit, null, false)
        setContentView(binding.root)
        strText = ""

        binding.apply {
            iFieldType = iType
            iMaxLines = iLines

            editText.apply {
                setText(strInit)
                if (iLines == 1)
                    isSingleLine = true
            }

            cancelButton.setOnClickListener { cancel() }
            editButton.setOnClickListener {

                strText = editText.text.toString()
                if(strText.isEmpty())
                {
                    CB_SingleSystemMgr.showToast(R.string.str_input_letter)
                    return@setOnClickListener
                }

                editFunc.invoke()
                cancel()
            }

            CB_AppFunc.openIME(editText, context)
        }

        window!!.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        setCanceledOnTouchOutside(bCancelable)
        CB_SingleSystemMgr.registerDialog(CB_SingleSystemMgr.DIALOG_TYPE.EDIT_DIALOG)
        show()
    }

    override fun dismiss()
    {
        super.dismiss()
        CB_SingleSystemMgr.releaseDialog(CB_SingleSystemMgr.DIALOG_TYPE.EDIT_DIALOG)
    }
}