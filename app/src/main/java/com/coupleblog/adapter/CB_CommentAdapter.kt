package com.coupleblog.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coupleblog.R
import com.coupleblog.fragment.CB_PostDetailFragment
import com.coupleblog.model.CB_Comment
import com.coupleblog.singleton.CB_AppFunc
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.getValue

class CB_CommentAdapter(private val fragment: CB_PostDetailFragment,
                        private val commentRef: DatabaseReference)
    : RecyclerView.Adapter<CB_CommentAdapter.ViewHolder>()
{
    companion object
    {
        const val strTag = "CommentAdapter"
    }

    private var childEventListener: ChildEventListener? = null
    private val commentKeyList = ArrayList<String>()
    private val commentList = ArrayList<CB_Comment>()

    init
    {
        val childEventListener = object : ChildEventListener
        {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?)
            {
                // Comment 아이템이 추가된 상황
                val comment = snapshot.getValue<CB_Comment>()

                commentKeyList.add(snapshot.key!!)
                commentList.add(comment!!)
                notifyItemChanged(commentList.size - 1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?)
            {
                // Comment 아이템이 변경이 된 경우
                // 새 정보를 받아온다.
                val newComment = snapshot.getValue<CB_Comment>()
                val commentKey = snapshot.key

                // 해당 위치 정보의 인덱스를 찾는다.
                val commentIdx = commentKeyList.indexOf(commentKey)
                if(commentIdx > -1 && newComment != null)
                {
                    // 이전에 있던 데이터이고 새로운 정보가 제대로 들어온 경우에 처리한다. 로컬 정보 갱신
                    commentList[commentIdx] = newComment
                    notifyItemChanged(commentIdx)
                }
                else
                {
                    Log.e(strTag, "onChildChanged:unknown data")
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot)
            {
                // Comment 아이템이 삭제된 경우
                val commentKey = snapshot.key

                val commentIdx = commentKeyList.indexOf(commentKey)
                if(commentIdx > -1)
                {
                    // 해당 아이템을 삭제한다.
                    commentKeyList.removeAt(commentIdx)
                    commentList.removeAt(commentIdx)
                    notifyItemRemoved(commentIdx)
                }
                else
                {
                    Log.e(strTag, "onChildRemoved:unknown data")
                }
            }

            // we don't support onChildMoved
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?){}

            override fun onCancelled(error: DatabaseError)
            {
                Log.e(strTag, "onCancelled:comment list")
                CB_AppFunc.okDialog(fragment.requireActivity(), R.string.str_error,
                    R.string.str_comment_list_load_failed, R.drawable.error_icon, true)
            }
        }

        // register childEventListener to commentRef
        commentRef.addChildEventListener(childEventListener)

        // keep it so it can be removed on app stop
        this.childEventListener = childEventListener
    }

    fun clearListener()
    {
        childEventListener?.let { commentRef.removeEventListener(it) }
    }

    class ViewHolder(private val binding: CommentItemBinding) : RecyclerView.ViewHolder(binding.root)
    {
        fun bind(argFragment: CB_PostDetailFragment, argCommentKey: String, argComment: CB_Comment)
        {
            binding.apply {
                fragment    = argFragment
                commentData = argComment
                commentKey  = argCommentKey
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder =
        DataBindingUtil.inflate<CommentItemBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.comment_item, viewGroup, false
        ).let { ViewHolder(it) }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(fragment, commentKeyList[position], commentList[position])

    override fun getItemCount(): Int = commentList.size

}