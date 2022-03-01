package com.coupleblog.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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

class CB_ChangeDialog(val activity: Activity, val bCancelable: Boolean, val isPassword: Boolean) : Dialog(activity)
{
    val TAG = javaClass.simpleName
    var binding: ChangeBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
        R.layout.dialog_cb_change, null, false)

    init
    {
        binding.apply {
            dialog = this@CB_ChangeDialog
            isPassword = this@CB_ChangeDialog.isPassword
        }

        initInputLayout()
        setContentView(binding.root)

        window!!.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        setCanceledOnTouchOutside(bCancelable)
        CB_SingleSystemMgr.registerDialog(CB_SingleSystemMgr.DIALOG_TYPE.CHANGE_DIALOG)
        show()
    }

    override fun dismiss()
    {
        super.dismiss()
        CB_SingleSystemMgr.releaseDialog(CB_SingleSystemMgr.DIALOG_TYPE.CHANGE_DIALOG)
    }

    fun changeButton()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        CB_AppFunc.clearFocusing(activity)
        Log.i(TAG, "changeButton")
        val strCurrentPassword = binding.currentPasswordEditText.text.toString()
        val strNewPassword = binding.newPasswordEditText.text.toString()
        val strNewPasswordAgain = binding.newPasswordAgainEditText.text.toString()

        if(strCurrentPassword.isEmpty())
        {
            CB_SingleSystemMgr.showToast(R.string.str_input_current_password)
            return
        }
        else if(strNewPassword.isEmpty())
        {
            if(isPassword) CB_SingleSystemMgr.showToast(R.string.str_input_password)
            else           CB_SingleSystemMgr.showToast(R.string.str_input_email)
            return
        }
        else if(strNewPasswordAgain.isEmpty())
        {
            if(isPassword) CB_SingleSystemMgr.showToast(R.string.str_input_password_again)
            else           CB_SingleSystemMgr.showToast(R.string.str_input_email_again)
            return
        }
        else if(binding.newPasswordAgainLayout.error != null)
        {
            CB_AppFunc.okDialog(activity, context.getString(R.string.str_error),
                binding.newPasswordAgainLayout.error.toString(), R.drawable.error_icon, true)
            return
        }
        else if(FirebaseAuth.getInstance().currentUser == null)
        {
            CB_SingleSystemMgr.showToast(R.string.str_login_again)
            return
        }

        val curUser = FirebaseAuth.getInstance().currentUser!!
        val credential = EmailAuthProvider.getCredential(curUser.email!!, strCurrentPassword)
        val dialog = CB_LoadingDialog(activity).apply { show() }

        curUser.reauthenticate(credential).addOnCompleteListener { task ->

            if(task.isSuccessful)
            {
                if(isPassword)
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
                            Log.e(TAG, "error : ${task.exception}")
                            CB_AppFunc.okDialog(activity, context.getString(R.string.str_error),
                                getString(R.string.str_password_change_failed), R.drawable.error_icon, true)
                        }
                    }
                }
                else
                {
                    curUser.updateEmail(strNewPassword).addOnCompleteListener { task ->

                        dialog.cancel()
                        if(task.isSuccessful)
                        {
                            CB_SingleSystemMgr.showToast(R.string.str_email_change_success)
                            cancel()

                            with(CB_AppFunc)
                            {
                                this.curUser.strUserEmail = strNewPassword
                                getUsersRoot().child(getUid()).setValue(this.curUser)
                            }
                        }
                        else
                        {
                            Log.e(TAG, "error : ${task.exception}")
                            CB_AppFunc.okDialog(activity, context.getString(R.string.str_error),
                                getString(R.string.str_email_change_failed), R.drawable.error_icon, true)
                        }
                    }
                }

            }
            else
            {
                dialog.cancel()
                Log.e(TAG, "error : ${task.exception}")
                CB_AppFunc.okDialog(activity, context.getString(R.string.str_error),
                    getString(R.string.str_invalid_auth), R.drawable.error_icon, true)
            }
        }
    }

    private fun initInputLayout()
    {
        with(binding)
        {
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
                        newPasswordLayout.error = if(isPassword) getString(R.string.str_input_password)
                                                  else           getString(R.string.str_input_email)
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
                        newPasswordAgainLayout.error = if(isPassword) getString(R.string.str_input_password)
                                                       else           getString(R.string.str_input_email)
                    }
                    else if(newPasswordEditText.text.toString() != text.toString())
                    {
                        newPasswordAgainLayout.error = if(isPassword) getString(R.string.str_password_again_error)
                                                       else           getString(R.string.str_email_again_error)
                    }
                    else
                    {
                        newPasswordAgainLayout.error = null
                    }
                }

                // Done : 로그인 시도
                setOnEditorActionListener { v, actionId, event ->
                    CB_AppFunc.clearFocusing(activity)
                    changeButton()
                    true
                }
            }
        }
    }

}