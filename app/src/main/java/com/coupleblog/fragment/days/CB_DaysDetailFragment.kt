package com.coupleblog.fragment.days

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.adapter.CB_CommentAdapter
import com.coupleblog.base.CB_BaseFragment
import com.coupleblog.dialog.CB_ItemListDialog
import com.coupleblog.dialog.CB_LoadingDialog
import com.coupleblog.dialog.DialogItem
import com.coupleblog.fragment.post.CB_PostDetailFragment
import com.coupleblog.fragment.profile.CB_ProfileInfoFragment
import com.coupleblog.model.CB_Days
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.IllegalArgumentException

class CB_DaysDetailFragment : CB_BaseFragment()
{
    companion object
    {
        val DAYS_KEY = "strDaysKey"
        val DAYS_EVENT_TYPE = "strEventType"
    }

    private val coupleRef = CB_AppFunc.getCouplesRoot().child(CB_AppFunc.curUser.strCoupleKey!!)

    private var daysListener: ValueEventListener? = null
    private lateinit var daysRef: DatabaseReference

    private var _binding            : DaysDetailBinding? = null
    private val binding get() = _binding!!

    lateinit var strDaysKey: String
    lateinit var strEvent: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = DaysDetailBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_DaysDetailFragment
            viewModel       = CB_ViewModel.Companion
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        with(requireArguments())
        {
            strEvent = getString(DAYS_EVENT_TYPE) ?: throw IllegalArgumentException("must pass strEvent")
            strDaysKey = getString(DAYS_KEY) ?: throw IllegalArgumentException("must pass strDaysKey")
            daysRef = coupleRef.child(strEvent).child(strDaysKey)
        }
    }

    override fun onStart()
    {
        super.onStart()

        // add value event listener to the days
        val daysListener = object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                val days = snapshot.getValue<CB_Days>()
                days?.let{
                    // ViewModel 데이터를 갱신시킨다.
                    CB_ViewModel.tDays.postValue(days)
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
                Log.e(strTag, "onCancelled:days load failed")
                CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                    R.string.str_days_data_load_failed, R.drawable.error_icon, true)
            }
        }

        // if changes happen at this location, call this listener
        daysRef.addValueEventListener(daysListener)

        // keep it so it can be removed on app stop
        this.daysListener = daysListener
    }

    override fun onStop()
    {
        super.onStop()
        daysListener?.let { daysRef.removeEventListener(it) }
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

    fun profileButton(strUid: String)
    {
        beginAction(R.id.action_CB_DaysDetailFragment_to_CB_ProfileInfoFragment,
            R.id.CB_DaysDetailFragment, bundleOf(CB_ProfileInfoFragment.ARGU_UID to strUid))
    }

    fun menuButton()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.ITEM_LIST_DIALOG))
            return

        val listItem = arrayListOf(
            DialogItem(getString(R.string.str_edit_days), R.drawable.pencil,
                callback = { editDays() }),

            DialogItem(getString(R.string.str_delete_days), R.drawable.trash_can,
                callback = { deleteDays() })
        )

        CB_ItemListDialog(requireActivity(), getString(R.string.str_mail_menu), listItem, true)
    }

    private fun editDays()
    {
        val arguments = bundleOf(
            DAYS_KEY to strDaysKey,
            DAYS_EVENT_TYPE to strEvent
        )
        beginAction(R.id.action_CB_DaysDetailFragment_to_CB_NewDaysFragment, R.id.CB_DaysDetailFragment, arguments)
    }

    private fun deleteDays()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        // we ask the user if you really want to delete post
        CB_AppFunc.confirmDialog(requireActivity(), R.string.str_warning, R.string.str_delete_msg,
            R.drawable.warning_icon, true,
            R.string.str_delete,
            yesListener = { _, _ ->

                // if user really want to delete, delete days
                val dialog = CB_LoadingDialog(requireActivity()).apply { show() }

                CB_AppFunc.networkScope.launch {

                    try
                    {
                        daysRef.setValue(null).await()
                        Log.d(strTag, "daysRef deleted")

                        launch(Dispatchers.Main)
                        {
                            // if success, move to DaysFragment
                            dialog.cancel()
                            CB_SingleSystemMgr.showToast(R.string.str_days_deleted)
                            backPressed()
                        }
                    }
                    catch(e: FirebaseException)
                    {
                        // if fail, error dialog
                        e.printStackTrace()
                        launch(Dispatchers.Main)
                        {
                            dialog.cancel()
                            CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                                R.string.str_days_delete_failed, R.drawable.error_icon, true)
                        }
                    }
                }

            }, R.string.str_cancel, null)
    }
}