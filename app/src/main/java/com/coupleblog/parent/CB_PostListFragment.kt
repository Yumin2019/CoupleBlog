package com.coupleblog.parent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.coupleblog.CB_PostAdapter
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = AllPostsBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner      = viewLifecycleOwner
            fragment            = this@CB_PostListFragment

            // LayoutManager 를 가지고 순서를 뒤집는다.
            layoutManager       = LinearLayoutManager(activity).apply {
                reverseLayout = true
                stackFromEnd  = true
            }

            // FirebaseRecyclerAdapter 를 생성하여 바인딩한다.
            val options = FirebaseRecyclerOptions.Builder<CB_Post>()
                .setQuery(getQuery(), CB_Post::class.java)
                .build()
            adapter = CB_PostAdapter(this@CB_PostListFragment, options)


            // 정해진 양만큼 로드한다.
            postRecyclerView.setHasFixedSize(true)
        }

        return binding.root
    }

    // 각 List Fragment 마다 원하는 쿼리를 작성한다.
    abstract fun getQuery(): Query

    fun clickedPostItem(postKey: String)
    {
        // PostDetailFragment 를 실행할 때 인자로 "postKey" - string 값을 준다.
        val args = bundleOf(CB_PostDetailFragment.ARGU_POST_KEY to postKey)
        findNavController().navigate(R.id.action_CB_MainFragment_to_CB_PostDetailFragment, args)
    }

    override fun onStart()
    {
        super.onStart()
        (binding.postRecyclerView.adapter as CB_PostAdapter).startListening()
    }

    override fun onStop()
    {
        super.onStop()
        (binding.postRecyclerView.adapter as CB_PostAdapter).stopListening()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }

    override fun backPressButton(){}
}