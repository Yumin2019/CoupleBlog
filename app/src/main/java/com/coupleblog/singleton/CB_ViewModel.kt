package com.coupleblog.singleton

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.coupleblog.ListLiveData
import com.coupleblog.R
import com.coupleblog.adapter.CB_CommentAdapter
import com.coupleblog.adapter.CB_EmailAdapter
import com.coupleblog.fragment.PAGE_TYPE
import com.coupleblog.model.CB_Mail
import com.coupleblog.model.CB_Post
import com.coupleblog.model.CB_User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// we'll use LiveData with SingleTon because it's simple
class CB_ViewModel
{
    companion object
    {
        const val strTag = "ViewModel"

        // when you go out of MainFragment
        // when you click view pages
        var bAddButton = MutableLiveData(false)
        var iPageType = MutableLiveData(PAGE_TYPE.MY_POSTS.ordinal)

        // User LiveData
        var curUser = MutableLiveData(CB_User())
        var coupleUser = MutableLiveData(CB_User())

        // ProfileInfoFragment
        var isMyProfile = MutableLiveData(true)
        var hasCouple   = MutableLiveData(false)

        // get profile user
        var profileUser = MutableLiveData(CB_User())
        var profileCoupleUser = MutableLiveData(CB_User())
        var profileUserUid = MutableLiveData(String())

        fun getUid() = CB_AppFunc.getUid()
        fun getCoupleUid() = CB_AppFunc.curUser.strCoupleUid

        // set all of profile live data
        fun setProfileInfo(strUid: String)
        {
            profileUserUid.value = strUid
            isMyProfile.value = (CB_AppFunc.getUid() == strUid)
            CB_AppFunc.getUsersRoot().child(strUid).addListenerForSingleValueEvent(object: ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot)
                {
                    // set profileUser value
                    val userInfo = snapshot.getValue<CB_User>()
                    if(userInfo == null)
                    {
                        CB_SingleSystemMgr.showToast(R.string.str_failed_to_find_user)
                        profileUser.value = CB_User()
                        return
                    }

                    profileUser.value = userInfo

                    if(userInfo.strCoupleUid.isNullOrEmpty())
                    {
                        profileCoupleUser.value = CB_User()
                        hasCouple.value = false
                    }
                    else
                    {
                        // if it's couple
                        CB_AppFunc.getUsersRoot().child(userInfo.strCoupleUid!!).addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot)
                            {
                                val coupleUserInfo = snapshot.getValue<CB_User>()
                                if(coupleUserInfo == null)
                                {
                                    CB_SingleSystemMgr.showToast(R.string.str_failed_to_find_user)
                                    profileCoupleUser.value = CB_User()
                                    hasCouple.value = false
                                    return
                                }

                                profileCoupleUser.value = coupleUserInfo
                                hasCouple.value = true
                            }

                            override fun onCancelled(error: DatabaseError)
                            {
                                Log.e(strTag, "onCancelled:str_failed_to_find_user")
                                CB_SingleSystemMgr.showToast(R.string.str_failed_to_find_user)
                                profileCoupleUser.value = CB_User()
                                hasCouple.value = false
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    Log.e(strTag, "onCancelled:str_failed_to_find_user")
                    CB_SingleSystemMgr.showToast(R.string.str_failed_to_find_user)
                    profileUser.value = CB_User()
                    profileCoupleUser.value = CB_User()
                    hasCouple.value = false
                }
            })
        }

        // PostDetailFragment
        var tPost      = MutableLiveData(CB_Post())
        var isMyPost   = MutableLiveData(true)

        // NewPostFragment
        var strTitle   = MutableLiveData("")
        var strBody    = MutableLiveData("")

        fun resetNewPostFragmentLiveData()
        {
            strTitle.postValue("")
            strBody.postValue("")
        }

        // MailDetailFragment
        var tMail = MutableLiveData(CB_Mail())

        // MailBoxFragment
        val checkList = ArrayList<Boolean>() // checkList for checking mails

        fun checkMail(viewHolder: RecyclerView.ViewHolder)
        {
            val iIdx = viewHolder.layoutPosition
            checkList[iIdx] = !checkList[iIdx]

            val iRes = if(checkList[iIdx]) R.drawable.ic_baseline_check_box_24
                       else                R.drawable.ic_baseline_check_box_outline_blank_24

            (viewHolder as CB_EmailAdapter.ViewHolder).binding.checkboxImageView.setImageResource(iRes)
        }
    }
}