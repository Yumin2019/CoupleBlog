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
import com.coupleblog.parent.CB_BaseFragment
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_ViewModel
import com.google.firebase.database.DatabaseReference

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

        // get emailRef
        emailRef = CB_AppFunc.getMailBoxRoot().child(CB_AppFunc.getUid())

        // MailAdapter
        adapter = CB_EmailAdapter(this@CB_MailBoxFragment, emailRef)
        binding.mailRecyclerView.adapter = adapter
    }

    override fun onStop()
    {
        super.onStop()
        adapter?.clearListener()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        CB_ViewModel.clearCheckedList()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }

    override fun backPressed()
    {
        // move to MailFragment
        beginAction(R.id.action_CB_MailBoxFragment_to_CB_MainFragment, R.id.CB_MailBoxFragment)
    }
}