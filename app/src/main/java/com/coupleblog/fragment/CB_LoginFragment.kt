package com.coupleblog.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.coupleblog.CB_AppFunc
import com.coupleblog.CB_SingleSystemMgr
import com.coupleblog.R
import com.coupleblog.dialog.CB_LoadingDialog
import com.coupleblog.parent.CB_BaseFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class CB_LoginFragment : CB_BaseFragment("LoginFragment") {

    private var _binding            : LoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_cb_login, container, false)
        binding.apply {
            lifecycleOwner  = this@CB_LoginFragment
            fragment        = this@CB_LoginFragment
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.emailEditText.doAfterTextChanged { text ->

            if(text?.isEmpty() == true)
            {
                binding.emailTextInputLayout.error = requireContext().getString(R.string.str_input_email)
            }
            else
            {
                binding.emailTextInputLayout.error = null
            }
        }

        binding.passwordEditText.doAfterTextChanged { text ->

            if(text?.isEmpty() == true)
            {
                binding.passwordTextInputLayout.error = requireContext().getString(R.string.str_input_password)
            }
            else
            {
                binding.passwordTextInputLayout.error = null
            }

        }
    }

    fun signInButton()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        infoLog("signInButton")
        val activity = requireActivity()
        val context = requireContext()

        if(binding.emailTextInputLayout.error != null)
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

        val strEmail = binding.emailEditText.text.toString()
        val strPassword = binding.passwordEditText.text.toString()

        CB_AppFunc.getAuth().signInWithEmailAndPassword(strEmail, strPassword)
            .addOnCompleteListener { task ->

                dialog.cancel()

                if(task.isSuccessful)
                {
                    // 로그인 성공
                    findNavController().navigate(R.id.action_CB_LoginFragment_to_CB_MainFragment)
                }
                else
                {
                    // 로그인 실패
                    CB_AppFunc.okDialog(activity, context.getString(R.string.str_error),
                        context.getString(R.string.str_sign_in_failed), R.drawable.error_icon, true)
                }
            }

    }

    fun registerButton()
    {
        infoLog("registerButton")
        findNavController().navigate(R.id.action_CB_LoginFragment_to_CB_RegisterFragment)
    }

    override fun onStart()
    {
        super.onStart()

        // if it's possible to login by auto

        CB_AppFunc.getAuth().currentUser?.let {
            findNavController().navigate(R.id.action_CB_LoginFragment_to_CB_MainFragment)
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
            finishAffinity(requireActivity())
        }
        else
        {
            CB_SingleSystemMgr.showToast(requireContext(), requireContext().getString(R.string.str_press_back_to_exit))
        }

        firstTime = secondTime
    }
}