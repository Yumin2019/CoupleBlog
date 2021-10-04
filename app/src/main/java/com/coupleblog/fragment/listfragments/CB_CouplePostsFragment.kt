package com.coupleblog.fragment.listfragments

import com.coupleblog.CB_AppFunc
import com.coupleblog.parent.CB_PostListFragment
import com.google.firebase.database.Query

class CB_CouplePostsFragment: CB_PostListFragment()
{
    override fun getQuery(): Query
    {
        // user-posts - uid -> post 정보
        // 데이터가 없는 경우에 어떻게 처리하는지는 모르겠음. 그냥 없는대로 띄우는지 ..?
        val coupleUid = CB_AppFunc.curUser.strCoupleUid ?: ""
        return CB_AppFunc.getUserPostsRoot().child(coupleUid)
/*
        if(coupleUid == null || coupleUid.isEmpty())
        {
            val uid = CB_AppFunc.getUid()
            return CB_AppFunc.getUserPostsRoot().child(uid)
        }
        else
        {
        }*/
    }
}