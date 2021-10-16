package com.coupleblog.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.dialog.CB_ItemListDialog
import com.coupleblog.dialog.DialogItem
import com.coupleblog.model.CB_Mail
import com.coupleblog.model.CB_User
import com.coupleblog.model.MAIL_TYPE
import com.coupleblog.parent.CB_BaseFragment
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

class CB_NewMailFragment: CB_BaseFragment("NewMailFragment")
{
    private var _binding            : NewMailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        CB_ViewModel.bAddButton.postValue(false)

        _binding = NewMailBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_NewMailFragment
            viewModel       = CB_ViewModel.Companion
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

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

        val strRecipient = binding.recipientEditText.text.toString()
        val strTitle = binding.titleEditText.text.toString()
        val strText  = binding.textEditText.text.toString()

        if(strTitle.isEmpty() || strRecipient.isEmpty())
        {
            // 초기에 빈 경우를 막는다.
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
            }, R.color.gray)
        )

        // if this user isn't a couple
        if(CB_AppFunc.curUser.strCoupleUid.isNullOrEmpty())
        {
            listItem.add(DialogItem(getString(R.string.str_mail_type_request_couple), R.drawable.ic_baseline_favorite_24,
                callback = {
                    // request couple
                    sendMail(MAIL_TYPE.REQUEST_COUPLE, strRecipient, strTitle, strText)
                }, R.color.red)
            )
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
        CB_AppFunc.getUsersRoot().orderByChild("strUserEmail").equalTo(strRecipientEmail).addListenerForSingleValueEvent(
            object: ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot)
                {
                    // check if strRecipient is valid
                    if(!snapshot.exists())
                    {
                        // if not found
                        CB_AppFunc.okDialog(requireActivity(), R.string.str_error, R.string.str_failed_to_find_user,
                            R.drawable.error_icon, true)
                        return
                    }

                    // if valid Recipient
                    val list = (snapshot.value as HashMap<*, *>).toList()
                    val strRecipientUid = list[0].first as String
                    val strRecipientCoupleUid = (list[0].second as HashMap<*, *>)["strCoupleUid"] as String?
                    val myUid = CB_AppFunc.getUid()

                    if(eType == MAIL_TYPE.REQUEST_COUPLE)
                    {
                        if(strRecipientUid == myUid)
                        {
                            // normal mail, release mail are okay
                            // but if you try to send request couple mail to you, it's an error
                            CB_AppFunc.okDialog(requireActivity(), R.string.str_error, R.string.str_tried_to_request_to_yourself,
                                R.drawable.error_icon, true)
                            return
                        }
                        else if(!strRecipientCoupleUid.isNullOrEmpty())
                        {
                            // this user is not a couple definitely
                            // but recipient can already be a couple
                            CB_AppFunc.okDialog(requireActivity(), R.string.str_error, R.string.str_user_is_couple_already,
                                R.drawable.error_icon, true)
                            return
                        }
                    }

                    // make Mail to send
                    val mail = CB_Mail(myUid, CB_AppFunc.getDateStringForSave(), eType.ordinal, strTitle, strText)

                    // save mail data at user-mails/uid/mailKey/mail data
                    CB_AppFunc.getMailBoxRoot().child(strRecipientUid).push().setValue(mail)
                    backPressed()
                }

                override fun onCancelled(error: DatabaseError)
                {
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
        findNavController().popBackStack()
    }
}