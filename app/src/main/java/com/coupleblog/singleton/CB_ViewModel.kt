package com.coupleblog.singleton

import androidx.lifecycle.MutableLiveData
import com.coupleblog.model.CB_Post

// we'll use LiveData with SingleTon because it's simple
class CB_ViewModel
{
    companion object
    {
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
    }
}