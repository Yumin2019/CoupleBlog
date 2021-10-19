package com.coupleblog.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.dialog.CB_ItemListDialog
import com.coupleblog.dialog.CB_LoadingDialog
import com.coupleblog.dialog.DialogItem
import com.coupleblog.model.CB_Mail
import com.coupleblog.model.REACTION_TYPE
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
import io.reactivex.rxjava3.internal.util.NotificationLite.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class CB_MailDetailFragment : CB_BaseFragment("MailDetail")
{
    companion object
    {
        const val ARGU_MAIL_KEY = "mailKey"
    }

    private lateinit var mailKey: String
    private lateinit var mailRef: DatabaseReference

    private var _binding            : MailDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = MailDetailBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_MailDetailFragment
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
                    CB_ViewModel.tMail.postValue(mail)
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
        // delete this mail in my mail box

    }

    fun heartButton()
    {
        // change heart icon state in this mail
        val dialog = CB_LoadingDialog(requireActivity()).apply { show() }

        CB_AppFunc.networkScope.launch {

            try
            {
                val prevMail = CB_ViewModel.tMail.value!!
                launch(Dispatchers.Main)
                {
                    // local data
                    prevMail.bHeartIcon = !(prevMail.bHeartIcon!!)

                }.join()

                // server data
                mailRef.setValue(prevMail)
            }
            catch (e: FirebaseException)
            {
                e.printStackTrace()
                launch(Dispatchers.Main)
                {
                    dialog.cancel()
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
}