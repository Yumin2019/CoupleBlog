package com.coupleblog.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.coupleblog.R
import com.coupleblog.adapter.CB_CommentAdapter
import com.coupleblog.adapter.CB_EmailAdapter
import com.coupleblog.dialog.CB_ItemListDialog
import com.coupleblog.dialog.DialogItem
import com.coupleblog.model.*
import com.coupleblog.parent.CB_BaseFragment
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

class CB_MailBoxFragment: CB_BaseFragment("MailBoxFragment")
{
    private var _binding            : MailboxBinding? = null
    private val binding get() = _binding!!

    private var adapter: CB_EmailAdapter? = null
    private lateinit var emailRef: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        CB_ViewModel.bAddButton.postValue(false)

        _binding = MailboxBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_MailBoxFragment
            layoutManager   = LinearLayoutManager(context)
            viewModel       = CB_ViewModel.Companion
        }
        return binding.root
    }

    override fun onStart()
    {
        super.onStart()

        // MailAdapter
        adapter = CB_EmailAdapter(this@CB_MailBoxFragment, emailRef)
        binding.mailRecyclerView.adapter = adapter
        CB_ViewModel.bMailButton.postValue(true)
    }

    override fun onStop()
    {
        super.onStop()
        adapter?.clearListener()
        CB_ViewModel.bMailButton.postValue(false)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        CB_ViewModel.clearCheckedList()

        // get emailRef
        emailRef = CB_AppFunc.getMailBoxRoot().child(CB_AppFunc.getUid())
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

    fun emailItem(mailData: CB_Mail, mailKey: String)
    {
       // 메일 아이템을 클릭한 경우, 메일 정보를 출력한다.


      /*
           if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.CONFIRM_DIALOG))
            return
      if(mailData.iMailType == MAIL_TYPE.REQUEST_COUPLE.ordinal)
        {
            CB_AppFunc.confirmDialog(requireActivity(), "Couple Request", "you want to allow this request from $mailData.strSenderUid",
            R.drawable.haha_icon, true,
                "yes", yesListener = { _, _ ->

                    // 수정해야한다 무조건
                    val prevUser = CB_AppFunc.curUser
                    val coupleUid = mailData.strSenderUid!!
                    val myUid = CB_AppFunc.getUid()

                    // 유저 정보 수정 내꺼
                    prevUser.strCoupleUid = coupleUid
                    CB_AppFunc.getUsersRoot().child(myUid).setValue(prevUser)
                    CB_AppFunc.getCouplesRoot().child(myUid).setValue( CB_Couple(coupleUid))

                    CB_AppFunc.getUsersRoot().child(coupleUid).addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {

                            // 커플꺼
                            CB_AppFunc._coupleUser = snapshot.getValue<CB_User>()!!
                            CB_AppFunc.coupleUser.strCoupleUid = myUid
                            CB_AppFunc.getUsersRoot().child(coupleUid).setValue(CB_AppFunc.coupleUser)
                            CB_AppFunc.getCouplesRoot().child(coupleUid).setValue( CB_Couple(myUid))

                            CB_SingleSystemMgr.showToast("Couple request OK")
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })

                }, "no", null)
        }*/
    }
}