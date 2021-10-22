package com.coupleblog.fragment

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coupleblog.R
import com.coupleblog.adapter.CB_EmailAdapter
import com.coupleblog.model.*
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

class CB_MailBoxFragment: CB_BaseFragment("MailBoxFragment")
{
    private var _binding            : MailboxBinding? = null
    private val binding get() = _binding!!

    private var adapter: CB_EmailAdapter? = null
    private lateinit var mailBoxRef: DatabaseReference

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

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        // get emailRef
        mailBoxRef = CB_AppFunc.getMailBoxRoot().child(CB_AppFunc.getUid())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        inflater.inflate(R.menu.menu_mail_box, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId)
        {
            R.id.action_logout ->
            {
                // 로그아웃을 진행한다.
                CB_AppFunc.getAuth().signOut()

                // 프레그먼트를 종료시킨다.
                findNavController().popBackStack()
            }

            R.id.action_delete_mail ->
            {

            }

            R.id.action_heart_mark ->
            {

            }

            R.id.action_read_mark ->
            {

            }

            R.id.action_select_all ->
            {

            }

            else -> {super.onOptionsItemSelected(item)}
        }

        return true
    }

    override fun onStart()
    {
        super.onStart()

        // MailAdapter
        adapter = CB_EmailAdapter(this@CB_MailBoxFragment, mailBoxRef)
        binding.mailRecyclerView.adapter = adapter
    }

    override fun onStop()
    {
        super.onStop()
        adapter?.clearListener()
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

    fun heartButton(mailKey: String)
    {
        // change heart icon state in this mail
        CB_AppFunc.networkScope.launch {

            try
            {
                val mailRef = mailBoxRef.child(mailKey)
                mailRef.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot)
                    {
                        // get data and update data from server
                        val mail = snapshot.getValue<CB_Mail>()!!
                        mail.bHeartIcon = !mail.bHeartIcon!!
                        mailRef.setValue(mail)
                    }

                    override fun onCancelled(error: DatabaseError)
                    {
                        CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                            R.string.str_failed_to_change_heart_state, R.drawable.error_icon, true)
                    }
                })
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

    fun emailItem(mailData: CB_Mail, mailKey: String)
    {
        // 메일 아이템을 클릭한 경우, 메일 디테일 화면을 출력한다.
        beginAction(R.id.action_CB_MainFragment_to_CB_MailDetailFragment, R.id.CB_MainFragment,
            bundleOf(CB_MailDetailFragment.ARGU_MAIL_KEY to mailKey))


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