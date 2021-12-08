package com.coupleblog.fragment

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.dialog.CB_ItemListDialog
import com.coupleblog.dialog.CB_LoadingDialog
import com.coupleblog.dialog.DialogItem
import com.coupleblog.model.CB_Mail
import com.coupleblog.model.MAIL_TYPE
import com.coupleblog.base.CB_CameraBaseFragment
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel
import com.coupleblog.storage.CB_UploadService
import com.coupleblog.storage.UPLOAD_TYPE
import com.google.firebase.FirebaseException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CB_NewMailFragment: CB_CameraBaseFragment("NewMailFragment", UPLOAD_TYPE.EMAIL_IMAGE, bDeferred = true)
{
    companion object
    {
        var strRecipientUid = ""
    }

    private var _binding            : NewMailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = NewMailBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_NewMailFragment
            viewModel       = CB_ViewModel.Companion
        }
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        // before upload, invoke this
        funDeferred = {
            CB_ViewModel.mailImage.postValue(imageBitmap)
        }

        strRecipientUid = ""
    }

    override fun beginActionToEdtior()
    {
        beginAction(R.id.action_CB_NewMailFragment_to_CB_PhotoEditorFragment, R.id.CB_NewMailFragment)
    }

    override fun onResume()
    {
        super.onResume()
        CB_ViewModel.bAddButton.postValue(false)
    }

     override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        inflater.inflate(R.menu.menu_new_mail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.action_recipients ->
            {
                // 수신인 지정 기능이다.
                // 자신과 커플(있는 경우)의 이메일을 설정할 수 있는 dialog를 제공한다.
                val itemList = ArrayList<DialogItem>()
                itemList.add(DialogItem(CB_AppFunc.curUser.strUserName!! + getString(R.string.str_me), R.drawable.haha_icon,
                callback = {   CB_ViewModel.strRecipient.postValue(CB_AppFunc.curUser.strUserEmail!!) }))

                // if a couple
                if(!CB_AppFunc.curUser.strCoupleUid.isNullOrEmpty())
                {
                    itemList.add(DialogItem(CB_AppFunc.coupleUser.strUserName!!, R.drawable.ic_baseline_favorite_24,
                        callback = { CB_ViewModel.strRecipient.postValue(CB_AppFunc.coupleUser.strUserEmail!!) }, R.color.red))
                }

                CB_ItemListDialog(requireActivity(), CB_AppFunc.getString(R.string.str_recipients), itemList, true)
            }

            R.id.action_add_image  ->
            {
                val listItem = arrayListOf(
                    DialogItem(getString(R.string.str_no_image), R.drawable.trash_can,
                        callback =
                        {
                            Log.i(strTag, "no image")
                            CB_ViewModel.mailImage.postValue(null)
                        }),
                    DialogItem(getString(R.string.str_camera), R.drawable.camera,
                        callback =
                        {
                            Log.i(strTag, "camera")
                            cameraActivity()
                           // cameraLauncher.launch(imageUri)
                        }),
                    DialogItem(getString(R.string.str_gallery), R.drawable.image,
                        callback =
                        {
                            galleryActivity()
                            Log.i(strTag, "gallery")
                            //galleryLauncher.launch("image/*")
                        })
                )

                CB_ItemListDialog(requireActivity(), getString(R.string.str_add_image), listItem, true)
            }
            else -> { super.onOptionsItemSelected(item) }
        }

        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        CB_ViewModel.resetNewMailFragmentLiveData()

        binding.recipientEditText.apply {

            doAfterTextChanged { text ->

                binding.recipientTextInputLayout.error =
                    when
                    {
                        text?.isEmpty() == true                       -> getString(R.string.str_input_recipient)
                        text?.matches(CB_AppFunc.emailRegex) == false -> getString(R.string.str_invalid_format_error)
                        else                                          -> null
                    }
            }

            // Next EditText
            setOnEditorActionListener { _, _, _ ->

                CB_AppFunc.openIME(binding.titleEditText, requireActivity())
                true
            }
        }

        binding.titleEditText.apply {

            doAfterTextChanged { text ->

                binding.titleTextInputLayout.error =
                if(text?.isEmpty() == true)   getString(R.string.str_input_title)
                else                          null

            }

            // Next EditText
            setOnEditorActionListener { _, _, _ ->

                CB_AppFunc.openIME(binding.textEditText, requireActivity())
                true
            }
        }
    }

    fun sendButton()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.ITEM_LIST_DIALOG))
            return

        val strRecipient = CB_ViewModel.strRecipient.value.toString()
        val strTitle = CB_ViewModel.strMailTitle.value.toString()
        val strText  = CB_ViewModel.strMailBody.value.toString()

        if(strTitle.isEmpty())
        {
            // 초기에 빈 경우를 막는다.
            CB_SingleSystemMgr.showToast(R.string.str_input_title)
            return
        }
        else if(strRecipient.isEmpty())
        {
            CB_SingleSystemMgr.showToast(R.string.str_input_recipient)
            return
        }
        else if(binding.recipientTextInputLayout.error != null)
        {
            CB_AppFunc.okDialog(requireActivity(), getString(R.string.str_error),
                binding.recipientTextInputLayout.error.toString(), R.drawable.error_icon, true)
            return
        }
        else if(binding.titleTextInputLayout.error != null)
        {
            CB_AppFunc.okDialog(requireActivity(), getString(R.string.str_error),
                binding.titleTextInputLayout.error.toString(), R.drawable.error_icon, true)
            return
        }


        // we'll check any type of mail user wants
        val listItem = ArrayList<DialogItem>()
        listItem.add(DialogItem(getString(R.string.str_mail_type_normal), R.drawable.ic_baseline_mail_24,
            callback = {
                // normal mail
                sendMail(MAIL_TYPE.NORMAL, strRecipient, strTitle, strText)
            }, R.color.grey)
        )

        // if this user isn't a couple
        if(CB_AppFunc.curUser.strCoupleUid.isNullOrEmpty())
        {
            listItem.add(DialogItem(getString(R.string.str_request_couple), R.drawable.ic_baseline_favorite_24,
                callback = {
                    // request couple
                    sendMail(MAIL_TYPE.REQUEST_COUPLE, strRecipient, strTitle, strText)
                }, R.color.red)
            )
        }

        if(listItem.size == 1)
        {
            // if this user hasn't any options.
            sendMail(MAIL_TYPE.NORMAL, strRecipient, strTitle, strText)
            return
        }

        // if use is an admin
        /* if(CB_AppFunc.coupleUser.strCoupleUid.isNullOrEmpty())
         {
             listItem.add(DialogItem(getString(R.string.str_mail_type_release_note), R.drawable.ic_baseline_event_note_24,
                 callback = {
                     // release note

                 }, R.color.white))
         }*/

        CB_ItemListDialog(requireActivity(), getString(R.string.str_choose_mail_type), listItem, true)
    }

    fun sendMail(eType: MAIL_TYPE, strRecipientEmail: String, strTitle: String, strText: String)
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        val dialog = CB_LoadingDialog(requireActivity()).apply { show() }

        CB_AppFunc.getUsersRoot().orderByChild("strUserEmail").equalTo(strRecipientEmail).addListenerForSingleValueEvent(
            object: ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot)
                {
                    CB_AppFunc.networkScope.launch {

                        try
                        {
                            // check if strRecipient is valid
                            if(!snapshot.exists())
                            {
                                launch(Dispatchers.Main)
                                {
                                    // if not found
                                    dialog.cancel()
                                    CB_AppFunc.okDialog(requireActivity(), R.string.str_error, R.string.str_failed_to_find_user,
                                        R.drawable.error_icon, true)
                                }
                                return@launch
                            }

                            // if valid Recipient
                            val list = (snapshot.value as HashMap<*, *>).toList()
                            strRecipientUid = list[0].first as String
                            val strRecipientCoupleUid = (list[0].second as HashMap<*, *>)["strCoupleUid"] as String?
                            val myUid = CB_AppFunc.getUid()

                            if(eType == MAIL_TYPE.REQUEST_COUPLE)
                            {
                                if(strRecipientUid == myUid)
                                {
                                    launch(Dispatchers.Main)
                                    {
                                        // normal mail, release mail are okay
                                        // but if you try to send request couple mail to you, it's an error
                                        dialog.cancel()
                                        CB_AppFunc.okDialog(requireActivity(), R.string.str_error, R.string.str_tried_to_request_to_yourself,
                                            R.drawable.error_icon, true)
                                    }
                                    return@launch
                                }
                                else if(!strRecipientCoupleUid.isNullOrEmpty())
                                {
                                    launch(Dispatchers.Main)
                                    {
                                        // this user is not a couple definitely
                                        // but recipient's already a couple
                                        dialog.cancel()
                                        CB_AppFunc.okDialog(requireActivity(), R.string.str_error, R.string.str_user_is_couple_already,
                                            R.drawable.error_icon, true)
                                    }
                                    return@launch
                                }
                            }

                            // make Mail to send
                            val uriRoot = CB_AppFunc.getMailBoxRoot().child(strRecipientUid)
                            val mailKey = uriRoot.push().key
                            if(mailKey == null)
                            {
                                // if key is null
                                launch(Dispatchers.Main)
                                {
                                    dialog.cancel()
                                    CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                                        R.string.str_mail_send_failed, R.drawable.error_icon, true)
                                }
                                return@launch
                            }
                            // 사전 예외 처리 END

                            val mail = CB_Mail(myUid, CB_AppFunc.getDateStringForSave(), "", eType.ordinal, strTitle, strText)
                            if(CB_ViewModel.mailImage.value == null)
                            {
                                // save mail data at user-mails/uid/mailKey/mail data
                                uriRoot.child(mailKey).setValue(mail).await()
                                Log.d(strTag, "sent a mail to recipient uid:$strRecipientUid")

                                launch(Dispatchers.Main)
                                {
                                    dialog.cancel()
                                    findNavController().popBackStack()
                                    CB_SingleSystemMgr.showToast(R.string.str_send_mail_success)
                                }
                            }
                            else
                            {
                                // it has an image
                                saveBitmapAndUpload(mailKey)

                                // it's called after upload
                                CB_UploadService.funSuccess =
                                    {
                                        CB_AppFunc.networkScope.launch {
                                            mail.strImgPath = CB_UploadService.strPath
                                            uriRoot.child(mailKey).setValue(mail).await()
                                            Log.d(strTag, "sent a mail to recipient uid:$strRecipientUid")
                                            Log.d(CB_AppFunc.strTag, "update img to users/$strRecipientUid/" +
                                                    "user-mails/$mailKey/strImgPath")

                                            launch(Dispatchers.Main)
                                            {
                                                dialog.cancel()
                                                findNavController().popBackStack()
                                                CB_SingleSystemMgr.showToast(R.string.str_send_mail_success)
                                            }
                                        }
                                    }

                                CB_UploadService.funFailure =
                                    {
                                        launch(Dispatchers.Main)
                                        {
                                            dialog.cancel()
                                            Log.d(strTag, getString(R.string.str_mail_send_failed) + " uid:$strRecipientUid")
                                            CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                                                R.string.str_mail_send_failed, R.drawable.error_icon, true)
                                        }
                                    }
                            }
                        }
                        catch (e: FirebaseException)
                        {
                            e.printStackTrace()
                            launch(Dispatchers.Main)
                            {
                                dialog.cancel()
                                CB_AppFunc.okDialog(requireActivity(), R.string.str_error, R.string.str_mail_send_failed,
                                    R.drawable.error_icon, true)
                            }
                            return@launch
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    dialog.cancel()
                    CB_AppFunc.okDialog(requireActivity(), R.string.str_error, R.string.str_failed_to_find_user,
                        R.drawable.error_icon, true)
                }
            })
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }

    override fun backPressed()
    {
        // 메일 작성 중에 backPressed 이벤트가 들어오면 다시 확인해본다.
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.CONFIRM_DIALOG))
            return

        // 서버와 통신 중에 들어오는 경우 무시
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        val strRecipient = CB_ViewModel.strRecipient.value!!
        val strTitle = CB_ViewModel.strMailTitle.value!!
        val strText = CB_ViewModel.strMailBody.value!!

        // when addition, no changes
        if (strRecipient.isEmpty() && strTitle.isEmpty() && strText.isEmpty() && (CB_ViewModel.postImage.value == null))
        {
            findNavController().popBackStack()
            return
        }

        // 변경사항이 있으면 여부를 묻는다.
        CB_AppFunc.confirmDialog(requireActivity(), R.string.str_warning,
            R.string.str_discard_msg, R.drawable.warning_icon, true,
            R.string.str_discard,
            yesListener = { _, _ ->
                findNavController().popBackStack()
            }, R.string.str_cancel, null)
    }
}