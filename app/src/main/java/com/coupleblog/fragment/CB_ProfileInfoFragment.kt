package com.coupleblog.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.adapter.CB_CommentAdapter
import com.coupleblog.dialog.CB_ImageDialog
import com.coupleblog.model.CB_Post
import com.coupleblog.model.CB_User
import com.coupleblog.parent.CB_BaseFragment
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

class CB_ProfileInfoFragment: CB_BaseFragment("ProfileInfo")
{
    companion object
    {
        const val ARGU_UID = "strUid"
    }

    private var strUid = ""
    private var profileUserListener: ValueEventListener? = null

    private var _binding            : ProfileInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = ProfileInfoBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment = this@CB_ProfileInfoFragment
            viewModel = CB_ViewModel.Companion
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        // get uid and set live data
        strUid = requireArguments().getString(ARGU_UID)!!
        CB_ViewModel.setProfileInfo(strUid)
    }

    override fun onStart()
    {
        super.onStart()

        // add event listener to profileUser
        // we'll ignore profileCoupleUser's change
        val eventListener = object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                val userInfo = snapshot.getValue<CB_User>()!!
                CB_ViewModel.profileUser.postValue(userInfo)

                if(CB_ViewModel.hasCouple.value == false && !userInfo.strCoupleUid.isNullOrEmpty())
                {
                    // prev: no couple, now : couple
                    // update data
                    CB_ViewModel.setProfileInfo(strUid)
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
                Log.e(strTag, "failed to load user data")
            }
        }

        CB_AppFunc.getUsersRoot().child(strUid).addValueEventListener(eventListener)
        profileUserListener = eventListener
    }

    override fun onStop()
    {
        super.onStop()
        profileUserListener?.let { CB_AppFunc.getUsersRoot().child(strUid).removeEventListener(it) }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }

    fun imageButton(strImagePath: String)
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.IMAGE))
            return

        CB_ViewModel.strImagePath.postValue(strImagePath)
        CB_ImageDialog(requireActivity())
    }

    fun editButton()
    {
        // only my profile
        beginAction(R.id.action_CB_ProfileInfoFragment_to_CB_EditProfileFragment, R.id.CB_ProfileInfoFragment)
    }

    override fun backPressed()
    {
        findNavController().popBackStack()
    }
}