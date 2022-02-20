package com.coupleblog.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import com.coupleblog.R
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_AppFunc.Companion.getString
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class CB_PasswordChangeDialog(context: Activity, bCancelable: Boolean) : Dialog(context)
{
    init
    {
        val binding: PasswordChangeBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
            R.layout.dialog_cb_password_change, null, false)
        binding.apply {
            cancelButton.setOnClickListener { cancel() }

            changeButton.setOnClickListener {

                if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
                    return@setOnClickListener

                CB_AppFunc.clearFocusing(context)
                Log.i("PasswordChangeDialog", "changeButton")
                val strCurrentPassword = currentPasswordEditText.text.toString()
                val strNewPassword = newPasswordEditText.text.toString()
                val strNewPasswordAgain = newPasswordAgainEditText.text.toString()

                if(strCurrentPassword.isEmpty())
                {
                    CB_SingleSystemMgr.showToast(R.string.str_input_current_password)
                    return@setOnClickListener
                }
                else if(strNewPassword.isEmpty())
                {
                    CB_SingleSystemMgr.showToast(R.string.str_input_password)
                    return@setOnClickListener
                }
                else if(strNewPasswordAgain.isEmpty())
                {
                    CB_SingleSystemMgr.showToast(R.string.str_input_password_again)
                    return@setOnClickListener
                }
                else if(newPasswordAgainLayout.error != null)
                {
                    CB_AppFunc.okDialog(context, context.getString(R.string.str_error),
                        newPasswordAgainLayout.error.toString(), R.drawable.error_icon, true)
                    return@setOnClickListener
                }
                else if(FirebaseAuth.getInstance().currentUser == null)
                {
                    CB_SingleSystemMgr.showToast(R.string.str_login_again)
                    return@setOnClickListener
                }

                val curUser = FirebaseAuth.getInstance().currentUser!!
                val credential = EmailAuthProvider.getCredential(curUser.email!!, strCurrentPassword)
                val dialog = CB_LoadingDialog(context).apply { show() }

                curUser.reauthenticate(credential).addOnCompleteListener { task ->

                    if(task.isSuccessful)
                    {
                        curUser.updatePassword(strNewPassword).addOnCompleteListener { task ->

                            dialog.cancel()
                            if(task.isSuccessful)
                            {
                                CB_SingleSystemMgr.showToast(R.string.str_password_change_success)
                                cancel()
                            }
                            else
                            {
                                Log.e("PasswordChangeDialog", "error : ${task.exception}")
                                CB_AppFunc.okDialog(context, context.getString(R.string.str_error),
                                    getString(R.string.str_password_change_failed), R.drawable.error_icon, true)
                            }
                        }
                    }
                    else
                    {
                        dialog.cancel()
                        Log.e("PasswordChangeDialog", "error : ${task.exception}")
                        CB_AppFunc.okDialog(context, context.getString(R.string.str_error),
                            getString(R.string.str_invalid_auth), R.drawable.error_icon, true)
                    }
                }
            }

            currentPasswordEditText.apply {

                doAfterTextChanged { text ->

                    if(text?.isEmpty() == true)
                    {
                        currentPasswordLayout.error = getString(R.string.str_input_current_password)
                    }
                    else
                    {
                        currentPasswordLayout.error = null
                    }
                }

                // Next editText
                setOnEditorActionListener { v, actionId, event ->
                    CB_AppFunc.openIME(newPasswordEditText, context)
                    true
                }
            }

            newPasswordEditText.apply {

                doAfterTextChanged { text ->

                    if(text?.isEmpty() == true)
                    {
                        newPasswordLayout.error = getString(R.string.str_input_password)
                    }
                    else
                    {
                        newPasswordLayout.error = null
                    }
                }

                // Next editText
                setOnEditorActionListener { v, actionId, event ->
                    CB_AppFunc.openIME(newPasswordAgainEditText, context)
                    true
                }
            }

            newPasswordAgainEditText.apply {

                doAfterTextChanged { text ->

                    if(text?.isEmpty() == true)
                    {
                        newPasswordAgainLayout.error = getString(R.string.str_input_password)
                    }
                    else if(newPasswordEditText.text.toString() != text.toString())
                    {
                        newPasswordAgainLayout.error = getString(R.string.str_password_again_error)
                    }
                    else
                    {
                        newPasswordAgainLayout.error = null
                    }
                }

                // Done : 로그인 시도
                setOnEditorActionListener { v, actionId, event ->
                    CB_AppFunc.clearFocusing(context)
                    changeButton.performClick()
                    true
                }
            }
        }

        setContentView(binding.root)
        window!!.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        setCanceledOnTouchOutside(bCancelable)
        CB_SingleSystemMgr.registerDialog(CB_SingleSystemMgr.DIALOG_TYPE.PASSWORD_CHANGE)
        show()
    }

    override fun dismiss()
    {
        super.dismiss()
        CB_SingleSystemMgr.releaseDialog(CB_SingleSystemMgr.DIALOG_TYPE.PASSWORD_CHANGE)
    }

}