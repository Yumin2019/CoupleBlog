package com.coupleblog.singleton

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.coupleblog.ListLiveData
import com.coupleblog.R
import com.coupleblog.adapter.CB_EmailAdapter
import com.coupleblog.fragment.PAGE_TYPE
import com.coupleblog.model.CB_Mail
import com.coupleblog.model.CB_Post
import com.coupleblog.model.CB_User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// we'll use LiveData with SingleTon because it's simple
class CB_ViewModel
{
    companion object
    {
        // when you go out of MainFragment
        // when you click view pages
        var bAddButton = MutableLiveData(false)
        var iPageType = MutableLiveData(PAGE_TYPE.MY_POSTS.ordinal)

        // User LiveData
        var curUser = MutableLiveData<CB_User>()
        var coupleUser = MutableLiveData<CB_User>()

        // ProfileInfoFragment
        var isMyProfile = MutableLiveData(true)

        // get user info with isMyProfile
        fun getUser(): MutableLiveData<CB_User> = if(isMyProfile.value!!) curUser else coupleUser
        fun getOtherUser(): MutableLiveData<CB_User> = if(isMyProfile.value!!) coupleUser else curUser

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