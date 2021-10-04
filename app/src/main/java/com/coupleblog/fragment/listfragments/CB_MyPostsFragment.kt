package com.coupleblog.fragment.listfragments

import com.coupleblog.CB_AppFunc
import com.coupleblog.parent.CB_PostListFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class CB_MyPostsFragment: CB_PostListFragment()
{
    override fun getQuery(): Query
    {
        // user-posts - uid -> post 정보
        val uid = CB_AppFunc.getUid()
        return CB_AppFunc.getUserPostsRoot().child(uid)
    }

}