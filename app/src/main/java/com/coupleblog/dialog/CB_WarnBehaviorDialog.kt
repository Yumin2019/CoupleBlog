package com.coupleblog.dialog
import kotlinx.coroutines.Dispatchers

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
import kotlinx.coroutines.launch

class CB_WarnBehaviorDialog(val activity: Activity, val bCancelable: Boolean, val isAccount: Boolean) : Dialog(activity)
{
    val TAG = javaClass.simpleName
    var binding: WarnBehaviorBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
        R.layout.dialog_cb_warn_behavior, null, false)

    init
    {
        binding.apply {
            dialog = this@CB_WarnBehaviorDialog
            isAccount = this@CB_WarnBehaviorDialog.isAccount
            passwordEditText.apply {

                doAfterTextChanged { text ->
                    if(text?.isEmpty() == true)
                    {
                        passwordLayout.error = getString(R.string.str_input_current_password)
                    }
                    else
                    {
                        passwordLayout.error = null
                    }
                }

                setOnEditorActionListener { v, actionId, event ->
                    okButton()
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
        CB_SingleSystemMgr.registerDialog(CB_SingleSystemMgr.DIALOG_TYPE.CHANGE_DIALOG)
        show()
    }

    override fun dismiss()
    {
        super.dismiss()
        CB_SingleSystemMgr.releaseDialog(CB_SingleSystemMgr.DIALOG_TYPE.CHANGE_DIALOG)
    }

    fun okButton()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        CB_AppFunc.clearFocusing(activity)
        Log.i(TAG, "okButton")
        val strPassword = binding.passwordEditText.text.toString()

        if(strPassword.isEmpty())
        {
            CB_SingleSystemMgr.showToast(R.string.str_input_current_password)
            return
        }
        else if(FirebaseAuth.getInstance().currentUser == null)
        {
            CB_SingleSystemMgr.showToast(R.string.str_login_again)
            return
        }

        val curUser = FirebaseAuth.getInstance().currentUser!!
        val credential = EmailAuthProvider.getCredential(curUser.email!!, strPassword)
        val dialog = CB_LoadingDialog(activity).apply { show() }

        curUser.reauthenticate(credential).addOnCompleteListener { task ->

            if(task.isSuccessful)
            {
                if(isAccount)
                {
                    CB_AppFunc.networkScope.launch {

                        val bSuccess = CB_AppFunc.tryDeleteAccount()
                        if(bSuccess)
                        {
                            launch(Dispatchers.Main)
                            {
                                dialog.cancel()
                                cancel()

                                CB_AppFunc.okDialog(activity, R.string.str_delete_account, R.string.str_deleted_account,
                                    R.drawable.error_icon, false,
                                    listener =
                                    { _, _ ->
                                        CB_AppFunc.restartApp(activity)
                                    })
                            }
                        }
                        else
                        {
                            launch(Dispatchers.Main)
                            {
                                dialog.cancel()
                                CB_AppFunc.okDialog(activity, context.getString(R.string.str_error),
                                    getString(R.string.str_failed_to_delete_account), R.drawable.error_icon, true)
                            }
                        }
                    }
                }
                else
                {
                    CB_AppFunc.networkScope.launch {

                        val bSuccess = CB_AppFunc.tryBreakingUp()
                        if(bSuccess)
                        {
                            launch(Dispatchers.Main)
                            {
                                dialog.cancel()
                                cancel()

                                CB_AppFunc.okDialog(activity, R.string.str_break_up, R.string.str_deleted_couple,
                                    R.drawable.broken_heart, false,
                                listener =
                                { _, _ ->
                                    CB_AppFunc.restartApp(activity)
                                })
                            }
                        }
                        else
                        {
                            launch(Dispatchers.Main)
                            {
                                dialog.cancel()
                                CB_AppFunc.okDialog(activity, context.getString(R.string.str_error),
                                    getString(R.string.str_failed_to_delete_couple), R.drawable.error_icon, true)
                            }
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
}