package com.coupleblog.singleton

import androidx.lifecycle.MutableLiveData
import com.coupleblog.model.CB_Post

// we'll use LiveData with SingleTon because it's simple
class CB_ViewModel
{
    companion object
    {
        // when you go out of mainFragment
        // when you click view pages
        var bAddButton = MutableLiveData(false)

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

        // MailBoxFragment
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