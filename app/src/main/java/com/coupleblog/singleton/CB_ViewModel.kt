package com.coupleblog.singleton

import androidx.lifecycle.MutableLiveData

// SINGLETON, LiveData
class CB_ViewModel
{
    companion object
    {
        var bAddButton = MutableLiveData(false)
    }
}