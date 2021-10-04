package com.coupleblog.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.coupleblog.CB_AppFunc
import com.coupleblog.CB_SingleSystemMgr
import com.coupleblog.R
import com.coupleblog.dialog.CB_LoadingDialog
import com.coupleblog.model.CB_Couple
import com.coupleblog.model.CB_User
import com.coupleblog.parent.CB_BaseFragment

class CB_RegisterFragment : CB_BaseFragment("RegisterFragment")
{

    private var _binding            : RegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = RegisterBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_RegisterFragment
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.userNameEditText.doAfterTextChanged { text ->

            if(text?.isEmpty() == true)
            {
                binding.userNameTextInputLayout.error = requireContext().getString(R.string.str_input_email)
            }
            else
            {
                binding.userNameTextInputLayout.error = null
            }
        }

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
                binding.passwordTextInputLayout.error =
                    requireContext().getString(R.string.str_input_password)
            }
            else
            {
                binding.passwordTextInputLayout.error = null
            }

            // password again쪽을 다시 처리한다.
            if(text.toString() != binding.passwordAgainEditText.text.toString())
            {
                binding.passwordAgainTextInputLayout.error =
                    requireContext().getString(R.string.str_input_password)
            }
            else
            {
                binding.passwordAgainTextInputLayout.error = null
            }
        }

        binding.passwordAgainEditText.doAfterTextChanged { text ->

            if(text.toString() != binding.passwordEditText.text.toString())
            {
                binding.passwordAgainTextInputLayout.error =
                    requireContext().getString(R.string.str_password_again_error)
            }
            else
            {
                binding.passwordAgainTextInputLayout.error = null
            }
        }
    }

    fun signUpButton()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        infoLog("signInButton")
        val activity = requireActivity()
        val context = requireContext()

        val strUserName = binding.userNameEditText.text.toString()
        val strEmail = binding.emailEditText.text.toString()
        val strPassword = binding.passwordEditText.text.toString()
        val strPasswordAgain = binding.passwordAgainEditText.text.toString()

        if(strUserName.isEmpty() || strEmail.isEmpty() || strPassword.isEmpty() || strPasswordAgain.isEmpty())
        {
            // 초기에 빈 경우를 막는다.
            return
        }
        else if(binding.userNameTextInputLayout.error != null)
        {
            CB_AppFunc.okDialog(activity, context.getString(R.string.str_error),
                binding.userNameTextInputLayout.error.toString(), R.drawable.error_icon, true)
            return
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
        else if(binding.passwordAgainTextInputLayout.error != null)
        {
            CB_AppFunc.okDialog(activity, context.getString(R.string.str_error),
                binding.passwordAgainTextInputLayout.error.toString(), R.drawable.error_icon, true)
            return
        }

        // password, email are not empty
        val dialog = CB_LoadingDialog(activity).apply { show() }

        CB_AppFunc.getAuth().createUserWithEmailAndPassword(strEmail, strPassword)
            .addOnCompleteListener { task ->

                dialog.cancel()

                if(task.isSuccessful)
                {
                    // 회원가입 성공시 User 정보를 users 항목에 저장한다.
                    val uid = task.result?.user!!.uid
                    val user = CB_User(strUserName, strEmail, CB_AppFunc.getDateStringForSave(), "")

                    CB_AppFunc._curUser = user
                    CB_AppFunc.getUsersRoot().child(uid).setValue(user)

                    // Couple 정보도 couples 항목에 저장한다. (기본값)
                    CB_AppFunc.getCouplesRoot().child(uid).setValue(CB_Couple())

                    findNavController().navigate(R.id.action_CB_RegisterFragment_to_CB_LoginFragment)
                }
                else
                {
                    // 회원가입 실패
                    CB_AppFunc.okDialog(activity, context.getString(R.string.str_error),
                        context.getString(R.string.str_sign_up_failed), R.drawable.error_icon, true)
                }
            }

    }

    fun personalInfoPolicyButton()
    {
        infoLog("personalInfoPolicyButton")
        findNavController().navigate(R.id.action_CB_RegisterFragment_to_CB_InfoFragment)
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }

    override fun backPressButton()
    {
        findNavController().navigate(R.id.action_CB_RegisterFragment_to_CB_LoginFragment)
    }

}