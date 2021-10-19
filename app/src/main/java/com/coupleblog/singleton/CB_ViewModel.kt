package com.coupleblog.singleton

import androidx.lifecycle.MutableLiveData
import com.coupleblog.fragment.PAGE_TYPE
import com.coupleblog.model.CB_Mail
import com.coupleblog.model.CB_Post

// we'll use LiveData with SingleTon because it's simple
class CB_ViewModel
{
    companion object
    {
        // when you go out of MainFragment
        // when you click view pages
        var bAddButton = MutableLiveData(false)
        var iPageType = MutableLiveData(PAGE_TYPE.MY_POSTS.ordinal)

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
        var bMailButton = MutableLiveData(false)
        val emailCheckedList = ArrayList<Int>()

        fun clearCheckedList() { emailCheckedList.clear() }
        fun checkEmail(iIdx: Int)
        {
            if(!findCheck(iIdx))
            {
                // at first
                emailCheckedList.add(iIdx)
            }
            else
            {
                assert(false) { "tried to add invalid value" }
            }
        }
        fun unCheckEmail(iIdx: Int)
        {
            if(!emailCheckedList.remove(iIdx))
            {
                assert(false) { "tried to delete invalid value" }
            }
        }
        fun emailCheckedSize() = emailCheckedList.size
        fun findCheck(iIdx: Int): Boolean
        {
            emailCheckedList.forEach {
                if(it == iIdx)
                    return true
            }
            return false
        }

    }
}