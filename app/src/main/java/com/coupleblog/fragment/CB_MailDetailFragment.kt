package com.coupleblog.fragment


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.dialog.CB_ItemListDialog
import com.coupleblog.dialog.CB_LoadingDialog
import com.coupleblog.dialog.DialogItem
import com.coupleblog.model.CB_Couple
import com.coupleblog.model.CB_Mail
import com.coupleblog.model.CB_User
import com.coupleblog.parent.CB_BaseFragment
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class CB_MailDetailFragment : CB_BaseFragment("MailDetail")
{
    companion object
    {
        const val ARGU_MAIL_KEY = "strMailKey"
    }

    private lateinit var mailKey: String
    private lateinit var mailRef: DatabaseReference

    private var _binding            : MailDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        CB_ViewModel.bAddButton.value = false

        _binding = MailDetailBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_MailDetailFragment
            viewModel       = CB_ViewModel.Companion
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        // get postKey from arguments
        with(requireArguments())
        {
            // mailKey
            mailKey = getString(ARGU_MAIL_KEY) ?: throw IllegalArgumentException("must pass mailKey")
        }

        // load mail data
        mailRef = CB_AppFunc.getMailBoxRoot().child(CB_AppFunc.getUid()).child(mailKey)
        mailRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot)
            {
                val mail = snapshot.getValue<CB_Mail>()
                mail?.let {
                    // update mail data
                    CB_ViewModel.tMail.value = mail

                    if(!mail.bRead!!)
                    {
                        // if it's an unread mail
                        mail.bRead = true
                        mailRef.setValue(mail)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
                errorLog("mail load failed")
                CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                    R.string.str_mail_load_failed, R.drawable.error_icon, true)
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

    fun deleteMail()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.CONFIRM_DIALOG))
            return

        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        // check if user really want to delete this mail
        CB_AppFunc.confirmDialog(requireActivity(), R.string.str_warning,
            R.string.str_delete_msg, R.drawable.warning_icon, true,
            R.string.str_delete, yesListener = { _, _ ->

                val dialog = CB_LoadingDialog(requireActivity()).apply { show() }

                CB_AppFunc.networkScope.launch {

                    try
                    {
                        mailRef.setValue(null)
                        launch(Dispatchers.Main)
                        {
                            dialog.cancel()
                            CB_SingleSystemMgr.showToast(R.string.str_mail_deleted)
                            backPressed()
                        }
                    }
                    catch (e: FirebaseException)
                    {
                        e.printStackTrace()
                        launch(Dispatchers.Main)
                        {
                            dialog.cancel()
                            CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                                R.string.str_failed_to_delete_mail, R.drawable.error_icon, true)
                        }
                    }
                }

            }, R.string.str_cancel, null)
    }

    fun heartButton()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        // change heart icon state in this mail
        CB_AppFunc.networkScope.launch {

            try
            {
                val prevMail = CB_ViewModel.tMail.value!!
                launch(Dispatchers.Main)
                {
                    // local data
                    prevMail.bHeartIcon = !(prevMail.bHeartIcon!!)
                    CB_ViewModel.tMail.value = prevMail

                    // server data
                    mailRef.setValue(prevMail)
                }
            }
            catch (e: FirebaseException)
            {
                e.printStackTrace()
                launch(Dispatchers.Main)
                {
                    CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                        R.string.str_failed_to_change_heart_state, R.drawable.error_icon, true)
                }
            }
        }
    }

    fun menuButton()
    {
        // 받은 메일. 삭제.
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.ITEM_LIST_DIALOG))
            return

        val listItem = arrayListOf(
            DialogItem(getString(R.string.str_delete_post), R.drawable.ic_baseline_delete_forever_24,
                callback = { deleteMail() }),

          /*  답장(나중에 구현)
            DialogItem(getString(R.string.str_reply_mail), R.drawable.ic_baseline_reply_24,
                callback = { }),*/

            DialogItem(getString(R.string.str_heart), R.drawable.ic_baseline_favorite_24,
                callback = { heartButton() }, R.color.red),

            // ADD ICON IF UPDATED
        )

        CB_ItemListDialog(requireActivity(), getString(R.string.str_mail_menu), listItem, true)
    }

    fun coupleRequestButton()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.CONFIRM_DIALOG))
            return

        // dialog 를 출력하여 해당 유저와 커플이 되길 원하는지 판단한다.
        // 1. 자신한테 요청 메세지를 보낼 수는 없다. V
        // 2. 이미 커플인 상태라면 해당 버튼을 누를 수 없다. (gone) V
        // 3. 커플이면 요청 메일을 보낼 수 없다. V
        val mailData = CB_ViewModel.tMail.value!!

        // sender 정보를 토대로 유저 정보를 찾아온다.
        CB_AppFunc.getUsersRoot().child(mailData.strSenderUid!!).addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot)
            {
                val senderUserInfo = snapshot.getValue<CB_User>()
                if(senderUserInfo == null)
                {
                    CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                        R.string.str_failed_to_find_user, R.drawable.error_icon, false)
                    return
                }

                if(!senderUserInfo.strCoupleUid.isNullOrEmpty())
                {
                    // 4. 내가 커플이 아닌데, 보낸 상대가 이미 커플이라면 나는 요청을 수락하거나 거절할 수 없다.
                    CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                        R.string.str_user_is_couple_already, R.drawable.error_icon, false)
                    return
                }

                // 다른 사람이 보낸 요청이고 두 사람 모두 커플이 아닌 경우에 dialog 를 출력하여 여부를 확인한다.
                CB_AppFunc.confirmDialog(requireActivity(), getString(R.string.str_request_couple),
                    getString(R.string.str_request_couple_msg) + senderUserInfo.strUserName,
                            R.drawable.haha_icon, /*  유저 이미지*/ true,
                    getString(R.string.str_yes),
                    yesListener = { _, _ ->

                        val prevUser = CB_AppFunc.curUser
                        val coupleUid = mailData.strSenderUid!!
                        val myUid = CB_AppFunc.getUid()

                        // 유저 정보 수정 내꺼
                        prevUser.strCoupleUid = coupleUid
                        CB_AppFunc.getUsersRoot().child(myUid).setValue(prevUser)
                        CB_AppFunc.getCouplesRoot().child(myUid).setValue(CB_Couple(coupleUid))

                        CB_AppFunc.getUsersRoot().child(coupleUid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {

                                    // 커플꺼
                                    CB_AppFunc._coupleUser = snapshot.getValue<CB_User>()!!
                                    CB_AppFunc.coupleUser.strCoupleUid = myUid
                                    CB_AppFunc.getUsersRoot().child(coupleUid).setValue(CB_AppFunc.coupleUser)
                                    CB_AppFunc.getCouplesRoot().child(coupleUid).setValue(CB_Couple(myUid))

                                    CB_SingleSystemMgr.showToast(R.string.str_you_became_couple)

                                    backPressed()
                                }

                                override fun onCancelled(error: DatabaseError)
                                {
                                    CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                                        R.string.str_failed_to_find_user, R.drawable.error_icon, false)
                                    Log.e(strTag, "couple user load failed" + error.message)
                                }
                            })

                    }, getString(R.string.str_no), null)
            }

            override fun onCancelled(error: DatabaseError)
            {
                CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                    R.string.str_failed_to_find_user, R.drawable.error_icon, false)
                Log.e(strTag, "couple user load failed" + error.message)
            }
        })
    }

}