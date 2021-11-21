package com.coupleblog.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.R
import com.coupleblog.dialog.CB_LoadingDialog
import com.coupleblog.parent.CB_BaseFragment
import com.coupleblog.singleton.CB_ViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch


class CB_LoginFragment : CB_BaseFragment("LoginFragment")
{
    private var _binding            : LoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = LoginBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_LoginFragment
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.emailEditText.apply {

            doAfterTextChanged { text ->

                if(text?.isEmpty() == true)
                {
                    binding.emailTextInputLayout.error = getString(R.string.str_input_email)
                }
                else
                {
                    binding.emailTextInputLayout.error = null
                }
            }

            // Next editText
            setOnEditorActionListener { v, actionId, event ->

                CB_AppFunc.openIME(binding.passwordEditText, requireActivity())
                true
            }
        }

        binding.passwordEditText.apply {

            doAfterTextChanged { text ->

                if(text?.isEmpty() == true)
                {
                    binding.passwordTextInputLayout.error = getString(R.string.str_input_password)
                }
                else
                {
                    binding.passwordTextInputLayout.error = null
                }

            }

            // Done : 로그인 시도
            setOnEditorActionListener { v, actionId, event ->

                CB_AppFunc.clearFocusing(requireActivity())
                signInButton()
                true
            }

        }
    }

    override fun onResume()
    {
        super.onResume()

        with(binding)
        {
            emailEditText.text?.clear()
            passwordEditText.text?.clear()

            emailTextInputLayout.error = null
            passwordTextInputLayout.error = null
        }
    }

    fun signInButton()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        infoLog("signInButton")
        val activity = requireActivity()
        val context = CB_AppFunc.application

        val strEmail = binding.emailEditText.text.toString()
        val strPassword = binding.passwordEditText.text.toString()

        if(strEmail.isEmpty())
        {
            CB_SingleSystemMgr.showToast(R.string.str_input_email)
            return
        }
        else if(strPassword.isEmpty())
        {
            CB_SingleSystemMgr.showToast(R.string.str_input_password)
        }
        else if(binding.emailTextInputLayout.error != null)
        {
            CB_AppFunc.okDialog(activity, context.getString(R.string.str_error),
                binding.emailTextInputLayout.error.toString(), R.drawable.error_icon, true)
            return
        }
        else if(binding.passwordTextInputLayout.error != null)
        {
            CB_AppFunc.okDialog(activity, context.getString(R.string.str_error),
                binding.passwordTextInputLayout.error.toString(), R.drawable.error_icon, true)
            return
        }

        // password, email are not empty
        val dialog = CB_LoadingDialog(activity).apply { show() }

        CB_AppFunc.getAuth().signInWithEmailAndPassword(strEmail, strPassword)
            .addOnCompleteListener { task ->

                dialog.cancel()
                if(task.isSuccessful)
                {
                    // 로그인 성공
                    CB_AppFunc.getUserInfo(activity,
                    funcSuccess =
                    {
                        beginAction(R.id.action_CB_LoginFragment_to_CB_MainFragment, R.id.CB_LoginFragment)
                    }, null)
                }
                else
                {
                    // 로그인 실패
                    CB_AppFunc.okDialog(activity, R.string.str_error,
                        R.string.str_sign_in_failed, R.drawable.error_icon, true)
                }
            }
    }

    fun registerButton()
    {
        infoLog("registerButton")
        beginAction(R.id.action_CB_LoginFragment_to_CB_RegisterFragment, R.id.CB_LoginFragment)
    }

    override fun onStart()
    {
        super.onStart()

        // if it's possible to login by auto
        CB_AppFunc.getAuth().currentUser?.let {

            val dialog = CB_LoadingDialog(requireActivity()).apply { show() }
            CB_AppFunc.getUserInfo(requireActivity(),
            funcSuccess =
            {
                CB_AppFunc.mainScope.launch {
                    // 유저 정보를 제대로 가져온 경우에만 이동한다
                    dialog.cancel()
                    CB_SingleSystemMgr.showToast(R.string.str_auto_login_success)
                    beginAction(R.id.action_CB_LoginFragment_to_CB_MainFragment, R.id.CB_LoginFragment)
                }
            }, null)
        }
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