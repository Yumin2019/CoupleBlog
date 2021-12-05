package com.coupleblog.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coupleblog.R
import com.coupleblog.fragment.PostItemBinding
import com.coupleblog.model.CB_Post
import com.coupleblog.parent.CB_PostListFragment
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseError

// FirebaseRecyclerAdapter 내부에서 데이터를 Query 값으로 가지고 있는다.
class CB_PostAdapter(val fragment: CB_PostListFragment, options: FirebaseRecyclerOptions<CB_Post>)
    : FirebaseRecyclerAdapter<CB_Post, CB_PostAdapter.ViewHolder>(options)
{
    class ViewHolder(private val binding: PostItemBinding) : RecyclerView.ViewHolder(binding.root)
    {
        fun bind(argFragment: CB_PostListFragment, argPostKey: String, argPostData: CB_Post)
        {
            binding.apply {
                fragment    = argFragment
                postKey     = argPostKey
                postData    = argPostData
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder =
        DataBindingUtil.inflate<PostItemBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.cb_post_item, viewGroup, false
        ).let { ViewHolder(it) }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: CB_Post)
    {
        // Post 클릭 상황에서 필요한 PostKey 정보를 미리 넣어준다.
        // model 에 대한 UI처리는 dataBinding
        val postKey = getRef(position).key
        holder.bind(fragment, postKey!!, model)
    }

    override fun onDataChanged()
    {
        super.onDataChanged()
    }

    override fun onError(error: DatabaseError)
    {
        super.onError(error)
        Log.e("CB_PostAdapter", error.message)
    }
}