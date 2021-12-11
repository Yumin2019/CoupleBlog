package com.coupleblog.fragment.mail

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.coupleblog.R
import com.coupleblog.adapter.CB_EmailAdapter
import com.coupleblog.model.*
import com.coupleblog.base.CB_BaseFragment
import com.coupleblog.fragment.MailboxBinding
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

class CB_MailBoxFragment: CB_BaseFragment()
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

            layoutManager   = LinearLayoutManager(context).apply {
                reverseLayout = true
                stackFromEnd  = true
            }

            viewModel       = CB_ViewModel.Companion
        }
        return binding.root
    }

    override fun onResume()
    {
        super.onResume()
        CB_ViewModel.bAddButton.postValue(true)
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
                CB_AppFunc.logout {
                    // 프레그먼트를 종료시킨다.
                    findNavController().popBackStack()
                }
            }

            R.id.action_delete_mail ->
            {
                // same with heart code below
                var hasCheck = false
                for(i in CB_ViewModel.checkList.indices)
                {
                    if(CB_ViewModel.checkList[i])
                    {
                        hasCheck = true
                        break
                    }
                }

                if(!hasCheck)
                {
                    CB_SingleSystemMgr.showToast(R.string.str_check_mail)
                    return true
                }

                // 선택한 메일 항목에 대해서 삭제한다.
                CB_AppFunc.networkScope.launch {

                    try
                    {
                        val mailBoxRef = CB_AppFunc.getMailBoxRoot().child(CB_AppFunc.getUid())
                        for(i in CB_ViewModel.checkList.indices)
                        {
                            if(CB_ViewModel.checkList[i])
                            {
                                mailBoxRef.child(adapter!!.emailKeyList[i]).setValue(null)
                            }
                        }

                        launch(Dispatchers.Main)
                        {
                            CB_SingleSystemMgr.showToast(R.string.str_deleted_these)
                        }
                    }
                    catch(e: FirebaseException)
                    {
                        e.printStackTrace()
                        launch(Dispatchers.Main)
                        {
                            CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                                R.string.str_failed_to_delete_mail, R.drawable.error_icon, true)
                        }
                    }
                }
            }

            R.id.action_heart_mark ->
            {
                // same with heart code below
                var hasCheck = false
                for(i in CB_ViewModel.checkList.indices)
                {
                    if(CB_ViewModel.checkList[i])
                    {
                        hasCheck = true
                        break
                    }
                }

                if(!hasCheck)
                {
                    CB_SingleSystemMgr.showToast(R.string.str_check_mail)
                    return true
                }

                // 선택한 메일 항목에 대해서 heart icon 변경
                CB_AppFunc.networkScope.launch {

                    try
                    {
                        val mailBoxRef = CB_AppFunc.getMailBoxRoot().child(CB_AppFunc.getUid())
                        for(i in CB_ViewModel.checkList.indices)
                        {
                            if(CB_ViewModel.checkList[i])
                            {
                                val prevData = adapter!!.emailList[i]
                                prevData.bHeartIcon = !prevData.bHeartIcon!!
                                mailBoxRef.child(adapter!!.emailKeyList[i]).setValue(prevData)
                            }
                        }

                        // no toast when we change icons
                    }
                    catch(e: FirebaseException)
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

            R.id.action_read_mark ->
            {
                // same with heart code below
                var hasCheck = false
                for(i in CB_ViewModel.checkList.indices)
                {
                    if(CB_ViewModel.checkList[i])
                    {
                        hasCheck = true
                        break
                    }
                }

                if(!hasCheck)
                {
                    CB_SingleSystemMgr.showToast(R.string.str_check_mail)
                    return true
                }

                if(CB_ViewModel.checkList.isEmpty())
                {
                    CB_SingleSystemMgr.showToast(R.string.str_check_mail)
                    return true
                }

                // 선택한 메일 항목에 대해서 heart icon 변경
                CB_AppFunc.networkScope.launch {

                    try
                    {
                        val mailBoxRef = CB_AppFunc.getMailBoxRoot().child(CB_AppFunc.getUid())
                        for(i in CB_ViewModel.checkList.indices)
                        {
                            if(CB_ViewModel.checkList[i])
                            {
                                val prevData = adapter!!.emailList[i]
                                prevData.bRead = !prevData.bRead!!
                                mailBoxRef.child(adapter!!.emailKeyList[i]).setValue(prevData)
                            }
                        }

                        // no toast when we change icons
                    }
                    catch(e: FirebaseException)
                    {
                        e.printStackTrace()
                        launch(Dispatchers.Main)
                        {
                            CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                                R.string.str_failed_to_change_read_state, R.drawable.error_icon, true)
                        }
                    }
                }
            }

          /*  R.id.action_select_all ->
            {

            }*/

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
    }
}