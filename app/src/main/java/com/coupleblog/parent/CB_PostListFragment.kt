package com.coupleblog.parent

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.coupleblog.adapter.CB_PostAdapter
import com.coupleblog.R
import com.coupleblog.fragment.AllPostsBinding
import com.coupleblog.fragment.CB_PostDetailFragment
import com.coupleblog.model.CB_Post
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.Query

abstract class CB_PostListFragment : CB_BaseFragment("PostList")
{
    private var _binding            : AllPostsBinding? = null
    private val binding get() = _binding!!
    private var adapter: CB_PostAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = AllPostsBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner      = viewLifecycleOwner
            fragment            = this@CB_PostListFragment
            adapter             = this@CB_PostListFragment.adapter

            // LayoutManager 를 가지고 순서를 뒤집는다.
            layoutManager       = LinearLayoutManager(activity).apply {
                reverseLayout = true
                stackFromEnd  = true
            }

            // 정해진 양만큼 로드한다.
            postRecyclerView.setHasFixedSize(true)
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        // FirebaseRecyclerAdapter 를 생성하여 바인딩한다.
        // Query 값이 null 이라면 설정하지 않는다.
        val query = getQuery()
        if(query != null)
        {
            val options = FirebaseRecyclerOptions.Builder<CB_Post>()
                .setQuery(query, CB_Post::class.java)
                .build()
            adapter = CB_PostAdapter(this@CB_PostListFragment, options)
        }
    }

    // 각 List Fragment 마다 원하는 쿼리를 작성한다.
    abstract fun getQuery(): Query?

    fun clickedPostItem(postKey: String, strAuthorUid: String)
    {
        // PostDetailFragment 를 실행할 때 인자로 postKey, strAuthorUid 정보를 준다.
        val args = bundleOf(
            CB_PostDetailFragment.ARGU_POST_KEY to postKey,
            CB_PostDetailFragment.ARGU_AUTHOR_UID to strAuthorUid
        )
        beginAction(R.id.action_CB_MainFragment_to_CB_PostDetailFragment, R.id.CB_MainFragment, args)
    }

    override fun onStart()
    {
        super.onStart()
        adapter?.startListening()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onStop()
    {
        super.onStop()
        // if adapter didn't have any listeners from array, list in adapter will be destroyed
        adapter?.let{
            it.stopListening()
            it.notifyDataSetChanged()
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }

    override fun backPressed()
    {
        finalBackPressed()
    }
}