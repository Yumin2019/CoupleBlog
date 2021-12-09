package com.coupleblog.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.R
import com.coupleblog.dialog.CB_LoadingDialog
import com.coupleblog.model.CB_Couple
import com.coupleblog.model.CB_User
import com.coupleblog.base.CB_BaseFragment
import com.google.firebase.FirebaseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CB_RegisterFragment : CB_BaseFragment()
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

        binding.userNameEditText.apply {

            doAfterTextChanged { text ->

                if(text?.isEmpty() == true)
                {
                    binding.userNameTextInputLayout.error = getString(R.string.str_input_email)
                }
                else
                {
                    binding.userNameTextInputLayout.error = null
                }
            }

            // Next EditText
            setOnEditorActionListener { v, actionId, event ->

                CB_AppFunc.openIME(binding.emailEditText, requireActivity())
                true
            }
        }

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

            // Next EditText
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

                // password again쪽을 다시 처리한다.
                if(text.toString() != binding.passwordAgainEditText.text.toString())
                {
                    binding.passwordAgainTextInputLayout.error = getString(R.string.str_input_password)
                }
                else
                {
                    binding.passwordAgainTextInputLayout.error = null
                }
            }

            // Next EditText
            setOnEditorActionListener { v, actionId, event ->

                CB_AppFunc.openIME(binding.passwordAgainEditText, requireActivity())
                true
            }
        }

        binding.passwordAgainEditText.apply {

            doAfterTextChanged { text ->

                if(text.toString() != binding.passwordEditText.text.toString())
                {
                    binding.passwordAgainTextInputLayout.error = getString(R.string.str_password_again_error)
                }
                else
                {
                    binding.passwordAgainTextInputLayout.error = null
                }
            }

            // Done Sign Up
            setOnEditorActionListener { v, actionId, event ->

                CB_AppFunc.clearFocusing(requireActivity())
                signUpButton()
                true
            }
        }
    }

    fun signUpButton()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        infoLog("signInButton")
        val activity = requireActivity()
        val context = CB_AppFunc.application

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

                if(task.isSuccessful)
                {
                    CB_AppFunc.networkScope.launch {
                        try
                        {
                            // 회원가입 성공시 User 정보를 users 항목에 저장한다.
                            val uid = task.result?.user!!.uid
                            val user = CB_User(strUserName, strEmail, CB_AppFunc.getDateStringForSave(), "")

                            CB_AppFunc._curUser = user
                            CB_AppFunc.getUsersRoot().child(uid).setValue(user).await()

                            // Couple 정보도 couples 항목에 저장한다. (기본값)
                            CB_AppFunc.getCouplesRoot().child(uid).setValue(CB_Couple()).await()

                            launch(Dispatchers.Main)
                            {
                                // Login Fragment
                                dialog.cancel()
                                CB_SingleSystemMgr.showToast(R.string.str_registration_success)
                                backPressed()
                            }
                        }
                        catch(e: FirebaseException)
                        {
                            e.printStackTrace()
                            launch(Dispatchers.Main)
                            {
                                dialog.cancel()
                                CB_AppFunc.okDialog(activity, R.string.str_error,
                                    R.string.str_value_update_failed, R.drawable.error_icon, true)
                            }
                        }
                    }
                }
                else
                {
                    // 회원가입 실패
                    dialog.cancel()
                    CB_AppFunc.okDialog(activity, R.string.str_error,
                        R.string.str_sign_up_failed, R.drawable.error_icon, true)
                }
            }

    }

    fun personalInfoPolicyButton()
    {
        infoLog("personalInfoPolicyButton")
        beginAction(R.id.action_CB_RegisterFragment_to_CB_InfoFragment, R.id.CB_RegisterFragment)
    }

    override fun onResume()
    {
        super.onResume()

        with(binding)
        {
            userNameEditText.text?.clear()
            emailEditText.text?.clear()
            passwordEditText.text?.clear()
            passwordAgainEditText.text?.clear()

            userNameTextInputLayout.error = null
            emailTextInputLayout.error = null
            passwordTextInputLayout.error = null
            passwordAgainTextInputLayout.error = null
        }
    }

    override fun backPressed()
    {
        findNavController().popBackStack()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }
}