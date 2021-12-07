package com.coupleblog.fragment.listfragments

import android.os.Bundle
import android.view.MenuItem
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.base.CB_PostListFragment
import com.coupleblog.singleton.CB_ViewModel
import com.google.firebase.database.Query

class CB_CouplePostsFragment: CB_PostListFragment()
{
    override fun getQuery(): Query?
    {
        // user-posts - uid -> post 정보
        val coupleUid = CB_AppFunc.curUser.strCoupleUid
        if(coupleUid.isNullOrEmpty())
            return null

        return CB_AppFunc.getUserPostsRoot().child(coupleUid)
    }
}