package com.coupleblog.fragment.post

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.coupleblog.R
import com.coupleblog.dialog.CB_ItemListDialog
import com.coupleblog.dialog.CB_LoadingDialog
import com.coupleblog.dialog.DialogItem
import com.coupleblog.model.CB_Post
import com.coupleblog.model.REACTION_TYPE
import com.coupleblog.base.CB_CameraBaseFragment
import com.coupleblog.fragment.NewPostBinding
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel
import com.coupleblog.singleton.GlideApp
import com.coupleblog.storage.CB_UploadService
import com.coupleblog.storage.UPLOAD_TYPE
import com.google.firebase.FirebaseException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CB_NewPostFragment: CB_CameraBaseFragment(UPLOAD_TYPE.POST_IMAGE, bDeferred = true)
{
    companion object
    {
        var prevImgPath: String? = null
    }

    private var _binding            : NewPostBinding? = null
    private val binding get() = _binding!!

    // used for editing
    var editPostKey = ""
    var postData: CB_Post? = null
    var bImageChanged = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = NewPostBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_NewPostFragment
            viewModel       = CB_ViewModel.Companion
        }
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        // before upload, invoke this
        funDeferred = {
            CB_ViewModel.postImage.postValue(imageBitmap)
            bImageChanged = true
        }
    }

    override fun onResume()
    {
        super.onResume()
        CB_ViewModel.bAddButton.postValue(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        prevImgPath = null
        editPostKey = requireArguments().getString(CB_PostDetailFragment.ARGU_POST_KEY)!!
        if(editPostKey.isNotEmpty())
        {
            // if edit state, load post data with postKey
            CB_AppFunc.getUserPostsRoot().child(CB_AppFunc.getUid()).child(editPostKey).addListenerForSingleValueEvent(object :
            ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot)
                {
                    postData = snapshot.getValue<CB_Post>()
                    if(postData == null)
                    {
                        Log.e(strTag, "post data load cancelled")
                        CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                            R.string.str_post_data_load_failed, R.drawable.error_icon, false,
                        listener = { _, _ ->

                            // 에러상황이면 취소 못하게 하고 그냥 확인 누르면 PostDetailFragment로 보낸다.
                            // 즉, 이후에 postData값은 null일 수 없다.
                            findNavController().popBackStack()
                        })
                        return
                    }

                    CB_ViewModel.apply {
                        strPostTitle.postValue(postData!!.strTitle)
                        strPostBody.postValue(postData!!.strBody)

                        prevImgPath = postData!!.strImgPath
                        if(!prevImgPath.isNullOrEmpty())
                        {
                            // this post has an image, load from storage
                            CB_AppFunc.networkScope.launch {
                                val imageRef = CB_AppFunc.getStorageRef(prevImgPath!!)
                                GlideApp.with(requireActivity())
                                    .asBitmap()
                                    .load(imageRef)
                                    .into(object: CustomTarget<Bitmap>()
                                    {
                                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?)
                                        {
                                            postImage.postValue(resource)
                                        }
                                        override fun onLoadCleared(placeholder: Drawable?){}
                                    })
                            }
                        }
                        else
                        {
                            postImage.postValue(null)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    Log.e(strTag, "post data load cancelled", error.toException())
                    CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                        R.string.str_post_data_load_failed, R.drawable.error_icon, true)
                }
            })
        }
        else
        {
            // init "" at live data
            CB_ViewModel.resetNewPostFragmentLiveData()
        }

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
            setOnEditorActionListener { _, _, _ ->

                CB_AppFunc.openIME(binding.textEditText, requireActivity())
                true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        inflater.inflate(R.menu.menu_new_post, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId)
        {
            R.id.action_add_image  ->
            {
                val listItem = arrayListOf(
                    DialogItem(getString(R.string.str_no_image), R.drawable.trash_can,
                        callback =
                        {
                            Log.i(strTag, "no image")
                            CB_ViewModel.postImage.postValue(null)
                            bImageChanged = true
                        }),
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

                CB_ItemListDialog(requireActivity(), getString(R.string.str_add_image), listItem, true)
            }

            else -> {super.onOptionsItemSelected(item)}
        }

        return true
    }

    fun postButton()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        infoLog("postButton")
        val activity = requireActivity()

        val strTitle = CB_ViewModel.strPostTitle.value!!
        val strText = CB_ViewModel.strPostBody.value!!

        if(strTitle.isEmpty())
        {
            // 초기에 빈 경우를 막는다
            CB_SingleSystemMgr.showToast(R.string.str_input_title)
            return
        }
        else if(binding.titleTextInputLayout.error != null)
        {
            CB_AppFunc.okDialog(activity, getString(R.string.str_error),
                binding.titleTextInputLayout.error.toString(), R.drawable.error_icon, true)
            return
        }

        // we'll check if user really want to post
        CB_AppFunc.confirmDialog(activity, R.string.str_information,
           R.string.str_post_confirm, R.drawable.info_icon, true, R.string.str_post,
        yesListener = { _, _ ->

            // 확인을 누른 경우, 처리한다.
            val dialog = CB_LoadingDialog(activity).apply { show() }

            CB_AppFunc.networkScope.launch {

                try
                {
                    if(editPostKey.isNotEmpty())
                    {
                        // it has no image
                        if(CB_ViewModel.postImage.value == null)
                        {
                            // if edit state, modify post and save
                            postData!!.apply {
                                this.strTitle       = strTitle
                                strBody             = strText
                                strImgPath          = ""
                                strRecentEditDate   = CB_AppFunc.getDateStringForSave()
                            }

                            // delete previous post image
                            if(!prevImgPath.isNullOrEmpty())
                            {
                                CB_AppFunc.deleteFileFromStorage(prevImgPath!!, strTag,
                                    "deleted previous post img path:$prevImgPath",
                                    "delete previous post img Failed path:$prevImgPath")
                                prevImgPath = null
                            }

                            CB_AppFunc.getUserPostsRoot().child(CB_AppFunc.getUid())
                                .child(editPostKey).setValue(postData).await()
                            Log.d(strTag, "post updated $editPostKey")


                            launch(Dispatchers.Main)
                            {
                                // 저장을 완료한 이후에 다시 PostDetailFragment 로 이동한다.
                                dialog.cancel()
                                CB_SingleSystemMgr.showToast(R.string.str_post_edited)
                                findNavController().popBackStack()
                            }
                        }
                        else
                        {
                            // it has an image
                            saveBitmapAndUpload(editPostKey)

                            // it's called after upload
                            CB_UploadService.funSuccess =
                                {
                                    CB_AppFunc.networkScope.launch {

                                        postData!!.apply {
                                            this.strTitle = strTitle
                                            strBody = strText
                                            strImgPath = CB_UploadService.strPath
                                            strRecentEditDate = CB_AppFunc.getDateStringForSave()
                                        }

                                        CB_AppFunc.getUserPostsRoot().child(CB_AppFunc.getUid())
                                            .child(editPostKey).setValue(postData).await()
                                        Log.d(strTag, "post updated $editPostKey")

                                        launch(Dispatchers.Main)
                                        {
                                            // 저장을 완료한 이후에 다시 PostDetailFragment 로 이동한다.
                                            dialog.cancel()
                                            CB_SingleSystemMgr.showToast(R.string.str_post_edited)
                                            findNavController().popBackStack()
                                        }
                                    }
                                }

                            CB_UploadService.funFailure =
                                {
                                    launch(Dispatchers.Main)
                                    {
                                        dialog.cancel()
                                        CB_AppFunc.okDialog(activity, R.string.str_error,
                                            R.string.str_post_failed, R.drawable.error_icon, true)
                                    }
                                }
                        }
                    }
                    else
                    {
                        // if add state
                        // user-posts - userUid - postKey1 - CB_Post
                        val uidRootRef = CB_AppFunc.getUserPostsRoot().child(CB_AppFunc.getUid())
                        val postKey = uidRootRef.push().key

                        if(postKey == null)
                        {
                            // PostKey 가 없는 경우 dialog 처리
                            launch(Dispatchers.Main)
                            {
                                dialog.cancel()
                                CB_AppFunc.okDialog(activity, R.string.str_error,
                                    R.string.str_post_failed, R.drawable.error_icon, true)
                            }
                            return@launch
                        }

                        val strDate = CB_AppFunc.getDateStringForSave()
                        val tPost = CB_Post(CB_AppFunc.getUid(), strTitle, strText,
                            REACTION_TYPE.NONE.ordinal, strDate, strDate, "")
                        if(CB_ViewModel.postImage.value == null)
                        {
                            // 해당 경로에 Post 데이터를 저장한다.
                            uidRootRef.child(postKey).setValue(tPost).await()
                            Log.d(strTag, "postKey:$postKey setValue")

                            launch(Dispatchers.Main)
                            {
                                // 저장을 완료한 이후에 다시 mainFragment 로 이동한다.
                                dialog.cancel()
                                CB_SingleSystemMgr.showToast(R.string.str_post_posted)
                                findNavController().popBackStack()
                            }
                        }
                        else
                        {
                            // it has an image
                            saveBitmapAndUpload(postKey)

                            // it's called after upload
                            CB_UploadService.funSuccess =
                                {
                                    CB_AppFunc.networkScope.launch {
                                        tPost.strImgPath = CB_UploadService.strPath
                                        uidRootRef.child(postKey).setValue(tPost).await()
                                        Log.d(strTag, "postKey:$postKey setValue")

                                        launch(Dispatchers.Main)
                                        {
                                            // 저장을 완료한 이후에 다시 mainFragment 로 이동한다.
                                            dialog.cancel()
                                            CB_SingleSystemMgr.showToast(R.string.str_post_posted)
                                            findNavController().popBackStack()
                                        }
                                    }
                                }

                            CB_UploadService.funFailure =
                                {
                                    launch(Dispatchers.Main)
                                    {
                                        dialog.cancel()
                                        CB_AppFunc.okDialog(activity, R.string.str_error,
                                            R.string.str_post_failed, R.drawable.error_icon, true)
                                    }
                                }
                        }
                    }
                }
                catch (e: FirebaseException)
                {
                    // 문제가 생긴 경우 dialog 처리
                    e.printStackTrace()
                    launch(Dispatchers.Main)
                    {
                        dialog.cancel()
                        CB_AppFunc.okDialog(activity, R.string.str_error,
                            R.string.str_post_failed, R.drawable.error_icon, true)
                    }
                }
            }
        },
        R.string.str_cancel, null)
    }

    override fun backPressed()
    {
        // 포스트 작성 중에 backPressed 이벤트가 들어오면 다시 확인해본다.
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.CONFIRM_DIALOG))
            return

        // 서버와 통신 중에 들어오는 경우 무시
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        val strTitle = CB_ViewModel.strPostTitle.value!!
        val strText = CB_ViewModel.strPostBody.value!!

        if(editPostKey.isNotEmpty())
        {
            // when editing, no changes
            if(strTitle == postData!!.strTitle && strText == postData!!.strBody && !bImageChanged)
            {
                findNavController().popBackStack()
                return
            }
        }
        else
        {
            // when addition, no changes
            if (strTitle.isEmpty() && strText.isEmpty() && (CB_ViewModel.postImage.value == null))
            {
                findNavController().popBackStack()
                return
            }
        }

        // 변경사항이 있으면 여부를 묻는다.
        CB_AppFunc.confirmDialog(requireActivity(), R.string.str_warning,
            R.string.str_discard_msg, R.drawable.warning_icon, true,
            R.string.str_discard,
            yesListener = { _, _ ->
                findNavController().popBackStack()
            }, R.string.str_cancel, null)
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }
}