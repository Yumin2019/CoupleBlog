package com.coupleblog.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.dialog.CB_LoadingDialog
import com.coupleblog.model.CB_Post
import com.coupleblog.model.REACTION_TYPE
import com.coupleblog.parent.CB_BaseFragment
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.setIconImage
import com.google.firebase.FirebaseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/*  여러 위치에 업데이트
       val post = Post(userId, username, title, body)
       val postValues = post.toMap()

       val childUpdates = hashMapOf<String, Any?>(
               "/posts/$key" to postValues,             // null is deletion of data
               "/user-posts/$userId/$key" to postValues
       )

       database.updateChildren(childUpdates)
 */

class CB_NewPostFragment: CB_BaseFragment("NewPostFragment")
{
    private var _binding            : NewPostBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = NewPostBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_NewPostFragment
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.titleEditText.apply {

            doAfterTextChanged { text ->

                if(text?.isEmpty() == true)
                {
                    binding.titleTextInputLayout.error = getString(R.string.str_input_title)
                }
                else
                {
                    binding.titleTextInputLayout.error = null
                }
            }

            // Next EditText
            setOnEditorActionListener { v, actionId, event ->

                CB_AppFunc.openIME(binding.textEditText, requireActivity())
                true
            }
        }

        binding.textEditText.apply {

            // Done : Post Button
            setOnEditorActionListener { v, actionId, event ->

                CB_AppFunc.clearFocusing(requireActivity())
                postButton()
                true
            }
        }
    }

    fun postButton()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        infoLog("postButton")
        val activity = requireActivity()

        val strTitle = binding.titleEditText.text.toString()
        val strText = binding.textEditText.text.toString()
        // val imageView = binding.postImageView

        if(strTitle.isEmpty())
        {
            // 초기에 빈 경우를 막는다.
            return
        }
        else if(binding.titleTextInputLayout.error != null)
        {
            CB_AppFunc.okDialog(activity, getString(R.string.str_error),
                binding.titleTextInputLayout.error.toString(), R.drawable.error_icon, true)
            return
        }

        // we'll check if user really want to post
        CB_AppFunc.confirmDialog(activity, getString(R.string.str_information),
           getString(R.string.str_post_confirm), R.drawable.info_icon, true, getString(R.string.str_post),
        yesListener = { confirmDialog, which ->

            // 확인을 누른 경우, 처리한다.
            val dialog = CB_LoadingDialog(activity).apply { show() }

            CB_AppFunc.networkScope.launch {

                try
                {
                    // user-posts - userUid - postKey1 - CB_Post
                    val uidRootRef = CB_AppFunc.getUserPostsRoot().child(CB_AppFunc.getUid())
                    val postKey = uidRootRef.push().key

                    if(postKey == null)
                    {
                        // PostKey 가 없는 경우 dialog 처리
                        launch(Dispatchers.Main)
                        {
                            dialog.cancel()
                            CB_AppFunc.okDialog(activity, getString(R.string.str_error),
                                getString(R.string.str_post_failed), R.drawable.error_icon, true)
                        }
                        return@launch
                    }

                    // 해당 경로에 Post 데이터를 저장한다.
                    val strDate = CB_AppFunc.getDateStringForSave()
                    val tPost = CB_Post(CB_AppFunc.getUid(), strTitle, strText, REACTION_TYPE.NONE.ordinal, strDate, strDate)
                    uidRootRef.child(postKey).setValue(tPost).await()

                    launch(Dispatchers.Main)
                    {
                        // 저장을 완료한 이후에 다시 mainFragment 로 이동한다.
                        dialog.cancel()
                        findNavController().popBackStack()
                    }
                }
                catch (e: FirebaseException)
                {
                    // 문제가 생긴 경우 dialog 처리
                    e.printStackTrace()
                    launch(Dispatchers.Main)
                    {
                        dialog.cancel()
                        CB_AppFunc.okDialog(activity, getString(R.string.str_error),
                            getString(R.string.str_post_failed), R.drawable.error_icon, true)
                    }
                }
            }
        },
        getString(R.string.str_cancel), null)
    }

    override fun backPressed()
    {
        // 포스트 작성 중에 backPressed 이벤트가 들어오면 다시 확인해본다.
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.CONFIRM_DIALOG))
            return

        // 서버와 통신 중에 들어오는 경우 무시
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        with(binding)
        {
            val strTitle = titleEditText.text.toString()
            val strText = textEditText.text.toString()
            // val image = setIconImage()

            // 아무것도 작성한 것이 없다면 바로 메인화면 이동
            if(strTitle.isEmpty() && strText.isEmpty())
            {
                findNavController().popBackStack()
                return
            }
        }

        // 뭔가 적혀 있었으면 여부를 묻는다.
        CB_AppFunc.confirmDialog(requireActivity(), getString(R.string.str_warning),
            getString(R.string.str_post_discard), R.drawable.warning_icon, true,
            getString(R.string.str_discard),
            yesListener = { dialog, witch ->

                findNavController().popBackStack()

            }, getString(R.string.str_cancel), null)
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }
}