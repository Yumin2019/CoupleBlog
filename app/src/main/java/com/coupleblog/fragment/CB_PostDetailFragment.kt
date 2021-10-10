package com.coupleblog.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.coupleblog.R
import com.coupleblog.adapter.CB_CommentAdapter
import com.coupleblog.dialog.CB_ItemListDialog
import com.coupleblog.dialog.CB_LoadingDialog
import com.coupleblog.dialog.DialogItem
import com.coupleblog.model.CB_Comment
import com.coupleblog.model.CB_Post
import com.coupleblog.parent.CB_BaseFragment
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.IllegalArgumentException

class CB_PostDetailFragment: CB_BaseFragment("PostDetail")
{
    companion object
    {
        const val ARGU_POST_KEY = "strPostKey"
        const val ARGU_AUTHOR_UID = "strAuthorUid"
    }

    private lateinit var postKey: String
    private var isMyPost: Boolean = true

    private lateinit var postRef: DatabaseReference
    private lateinit var commentRef: DatabaseReference

    private var adapter: CB_CommentAdapter? = null
    private var postListener: ValueEventListener? = null

    private var _binding            : PostDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        CB_ViewModel.bAddButton.postValue(false)

        _binding = PostDetailBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_PostDetailFragment
            layoutManager   = LinearLayoutManager(context)
            viewModel       = CB_ViewModel.Companion
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        // get postKey from arguments
        with(requireArguments())
        {
            // postKey
            postKey = getString(ARGU_POST_KEY) ?: throw IllegalArgumentException("must pass postKey")

            // authorUid
            val strAuthorUid = getString(ARGU_AUTHOR_UID) ?: throw IllegalArgumentException("must pass strAuthorUid")

            // get postRef with authorUid and postKey
            // we need to know where this reference from
            isMyPost = (CB_AppFunc.getUid() == strAuthorUid)
            postRef = if(isMyPost)
            {
                CB_AppFunc.getUserPostsRoot().child(strAuthorUid).child(postKey)
            }
            else
            {
                CB_AppFunc.getUserPostsRoot().child(CB_AppFunc.curUser.strCoupleUid!!).child(postKey)
            }

            // get commentRef
            commentRef = CB_AppFunc.getPostCommentsRoot().child(postKey)
        }
    }

    override fun onStart()
    {
        super.onStart()

        // add value event listener to the post
        // onCreate, onDestroy에서 안 하는 이유는 잘 모르겠는데 아마 이유가 있어서 이런 거 같다. (따라가자)
        val postListener = object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                val post = snapshot.getValue<CB_Post>()
                post?.let {
                    // ViewModel 데이터를 갱신시킨다.
                    CB_ViewModel.tPost.postValue(post)
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
                Log.e(CB_CommentAdapter.strTag, "onCancelled:post load")
                CB_AppFunc.okDialog(requireActivity(), getString(R.string.str_error),
                    getString(R.string.str_post_data_load_failed), R.drawable.error_icon, true)
            }
        }

        // if changes happen at this location, call this listener
        postRef.addValueEventListener(postListener)

        // keep it so it can be removed on app stop
        this.postListener = postListener

        // commentAdapter
        adapter = CB_CommentAdapter(this@CB_PostDetailFragment, commentRef)
        binding.commentRecyclerView.adapter = adapter
    }

    override fun onStop()
    {
        super.onStop()

        postListener?.let { postRef.removeEventListener(it) }
        adapter?.clearListener()
    }

    override fun backPressed()
    {
        beginAction(R.id.action_CB_PostDetailFragment_to_CB_MainFragment, R.id.CB_PostDetailFragment)
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }

    fun menuButton()
    {
        isMyPost
    }

    fun postMenuButton()
    {
        // Post Menu Button 테스트 X (해야 한다.)
        
        if(isMyPost)
        {
            // if it's my post, we can edit or delete this post
            val listItem = arrayListOf(
                DialogItem(getString(R.string.str_edit_post), R.drawable.ic_baseline_edit_24,
                callback = {
                    // if you click edit item

                }),
                DialogItem(getString(R.string.str_delete_post), R.drawable.ic_baseline_delete_forever_24,
                callback = {
                    // if you click delete item

                })
            )

            CB_ItemListDialog(requireActivity(), getString(R.string.str_my_post_menu), listItem, true)
        }
        else
        {
            // if it's not my post, we can attach a sticker on it
            val listItem = arrayListOf(
                DialogItem(getString(R.string.str_love_icon), R.drawable.love_icon,
                callback = {
                    // if you click love item

                }),

                DialogItem(getString(R.string.str_like_icon), R.drawable.like_icon,
                callback = {
                    // if you click like item

                }),

                DialogItem(getString(R.string.str_check_icon), R.drawable.check_icon,
                callback = {
                    // if you click check item

                }),

                DialogItem(getString(R.string.str_haha_icon), R.drawable.haha_icon,
                callback = {
                    // if you click haha item

                }),

                DialogItem(getString(R.string.str_wow_icon), R.drawable.wow_icon,
                callback = {
                    // if you click wow item

                }),

                DialogItem(getString(R.string.str_sad_icon), R.drawable.sad_icon,
                callback = {
                    // if you click sad item

                }),
            )

            CB_ItemListDialog(requireActivity(), getString(R.string.str_my_post_menu), listItem, true)
        }
    }

    fun postButton()
    {
        // 포스트 내용을 알아온다.
        val strComment = binding.commentEditText.text.toString()
        if(strComment.isEmpty())
        {
            CB_AppFunc.okDialog(requireActivity(), getString(R.string.str_warning),
                getString(R.string.str_input_comment), R.drawable.warning_icon, true)
            return
        }

        // text clear
        binding.commentEditText.text?.clear()

        val dialog = CB_LoadingDialog(requireActivity()).apply { show() }

        CB_AppFunc.networkScope.launch {

            try
            {
                val comment = CB_Comment(CB_AppFunc.getUid(), strComment, CB_AppFunc.getDateStringForSave())
                commentRef.push().setValue(comment).await()

                dialog.cancel()
            }
            catch(e: FirebaseException)
            {
                e.printStackTrace()
                launch(Dispatchers.Main)
                {
                    dialog.cancel()
                    CB_AppFunc.okDialog(requireActivity(), getString(R.string.str_error),
                        getString(R.string.str_post_failed), R.drawable.error_icon, true)
                }
            }
        }

    }

}