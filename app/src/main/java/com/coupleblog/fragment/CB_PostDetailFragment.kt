package com.coupleblog.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.coupleblog.R
import com.coupleblog.adapter.CB_CommentAdapter
import com.coupleblog.dialog.CB_CommentEditDialog
import com.coupleblog.dialog.CB_ItemListDialog
import com.coupleblog.dialog.CB_LoadingDialog
import com.coupleblog.dialog.DialogItem
import com.coupleblog.model.CB_Comment
import com.coupleblog.model.CB_Post
import com.coupleblog.model.REACTION_TYPE
import com.coupleblog.base.CB_BaseFragment
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel
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

class CB_PostDetailFragment: CB_BaseFragment()
{
    companion object
    {
        const val ARGU_POST_KEY = "strPostKey"
        const val ARGU_AUTHOR_UID = "strAuthorUid"
    }

    private lateinit var postKey: String

    private lateinit var postRef: DatabaseReference
    private lateinit var commentRef: DatabaseReference

    private var adapter: CB_CommentAdapter? = null
    private var postListener: ValueEventListener? = null

    private var _binding            : PostDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = PostDetailBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_PostDetailFragment
            layoutManager   = LinearLayoutManager(context)
            viewModel       = CB_ViewModel.Companion
        }
        return binding.root
    }

    override fun onResume()
    {
        super.onResume()
        CB_ViewModel.bAddButton.postValue(false)
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
            val isMyPost = (CB_AppFunc.getUid() == strAuthorUid)
            postRef = if(isMyPost)
            {
                Log.d(strTag,"My Post")
                CB_AppFunc.getUserPostsRoot().child(strAuthorUid).child(postKey)
            }
            else
            {
                Log.d(strTag,"Not My Post")
                CB_AppFunc.getUserPostsRoot().child(CB_AppFunc.curUser.strCoupleUid!!).child(postKey)
            }

            // update isMyPostFlag
            CB_ViewModel.isMyPost.postValue(isMyPost)

            // get commentRef
            commentRef = CB_AppFunc.getPostCommentsRoot().child(postKey)
        }
    }

    override fun onStart()
    {
        super.onStart()

        // add value event listener to the post
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
                Log.e(strTag, "onCancelled:post load failed")
                CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                    R.string.str_post_data_load_failed, R.drawable.error_icon, true)
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
        findNavController().popBackStack()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }

    fun commentMenuButton(strCommentKey: String, commentData: CB_Comment)
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.ITEM_LIST_DIALOG))
            return

        // 내가 권한이 없으면 메뉴 아이콘이 보이지 않도록 처리 되어 있다.
        // 내가 댓글 작성자인 경우, 편집과 삭제 기능이 있다.
        // 내가 포스트 작성자인 경우, 댓글에 스티커를 줄 수 있다.

        // 즉, 자신이 쓴 글에서 댓글을 달면 모든 기능을 사용할 수 있다.
        // 하지만, 상대방이 쓴 글이라면 나는 편집, 삭제만 가능하고 스티커 기능은 없다.
        // 작성자의 권한, 댓글 작성자의 권한이 있어서 따로 따로 관리가 된다.
        val listItem = ArrayList<DialogItem>()

        // comment author permissions
        if(commentData.strAuthorUid == CB_AppFunc.getUid())
        {
            listItem.apply {

                add(DialogItem(getString(R.string.str_edit_comment), R.drawable.pencil,
                callback = {

                    if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.COMMENT_EDIT_DIALOG))
                        return@DialogItem

                    CB_CommentEditDialog(requireActivity(), this@CB_PostDetailFragment,
                    commentData, strCommentKey, false)

                }))

                add(DialogItem(getString(R.string.str_delete_comment), R.drawable.trash_can,
                callback = { deleteComment(strCommentKey) }))
            }
        }

        // post author permissions
        if(CB_ViewModel.isMyPost.value!!)
        {
            listItem.add(DialogItem(getString(R.string.str_reaction_icons), R.drawable.haha_icon,
            callback = { commentReactionIconsButton(strCommentKey, commentData) }))
        }

        CB_ItemListDialog(requireActivity(), getString(R.string.str_comment_menu), listItem, true)
    }

    fun postMenuButton()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.ITEM_LIST_DIALOG))
            return

        if(CB_ViewModel.isMyPost.value!!)
        {
            // if it's my post, we can edit or delete this post
            val listItem = arrayListOf(
                DialogItem(getString(R.string.str_edit_post), R.drawable.pencil,
                callback = { editPost() }),

                DialogItem(getString(R.string.str_delete_post), R.drawable.trash_can,
                callback = { deletePost() })
            )

            CB_ItemListDialog(requireActivity(), getString(R.string.str_my_post_menu), listItem, true)
        }
        else
        {
            // if it's not my post, we can attach a sticker on it
            val listItem = arrayListOf(
                DialogItem(getString(R.string.str_none_icon), R.drawable.ic_baseline_do_not_disturb_24,
                callback = { setPostReactionIcon(REACTION_TYPE.NONE) }),

                DialogItem(getString(R.string.str_heart_icon), R.drawable.heart_icon,
                callback = { setPostReactionIcon(REACTION_TYPE.HEART) }),

                DialogItem(getString(R.string.str_like_icon), R.drawable.like_icon,
                callback = { setPostReactionIcon(REACTION_TYPE.LIKE) }),

                DialogItem(getString(R.string.str_check_icon), R.drawable.check_icon,
                callback = { setPostReactionIcon(REACTION_TYPE.CHECK) }),

                DialogItem(getString(R.string.str_haha_icon), R.drawable.haha_icon,
                callback = { setPostReactionIcon(REACTION_TYPE.HAHA) }),

                DialogItem(getString(R.string.str_wow_icon), R.drawable.wow_icon,
                callback = { setPostReactionIcon(REACTION_TYPE.WOW) }),

                DialogItem(getString(R.string.str_sad_icon), R.drawable.sad_icon,
                callback = { setPostReactionIcon(REACTION_TYPE.SAD) })

                // ADD ICON IF UPDATED
            )

            CB_ItemListDialog(requireActivity(), getString(R.string.str_reaction_icons), listItem, true)
        }
    }

    fun editComment(strCommentKey: String, commentData: CB_Comment)
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        // CommentEditDialog call this func
        val dialog = CB_LoadingDialog(requireActivity()).apply { show() }
        CB_AppFunc.networkScope.launch {

            try
            {
                commentRef.child(strCommentKey).setValue(commentData)
                launch(Dispatchers.Main)
                {
                    dialog.cancel()
                    CB_SingleSystemMgr.showToast(R.string.str_comment_edited)
                }
            }
            catch (e: FirebaseException)
            {
                e.printStackTrace()
                launch(Dispatchers.Main)
                {
                    dialog.cancel()
                    CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                        R.string.str_post_failed, R.drawable.error_icon, true)
                }
            }
        }
    }

    fun deleteComment(strCommentKey: String)
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.CONFIRM_DIALOG))
            return

        CB_AppFunc.confirmDialog(requireActivity(), R.string.str_warning,
            R.string.str_delete_msg, R.drawable.warning_icon, true,
            R.string.str_delete,
            yesListener = { _, _ ->

                // if user really want to delete, delete comment
                val dialog = CB_LoadingDialog(requireContext()).apply { show() }

                CB_AppFunc.networkScope.launch {

                    try
                    {
                        // delete post, post comments
                        commentRef.child(strCommentKey).setValue(null).await()

                        launch(Dispatchers.Main)
                        {
                            dialog.cancel()
                            CB_SingleSystemMgr.showToast(R.string.str_comment_deleted)
                        }
                    }
                    catch(e: FirebaseException)
                    {
                        // if fail, error dialog
                        e.printStackTrace()
                        launch(Dispatchers.Main)
                        {
                            dialog.cancel()
                            CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                                R.string.str_post_delete_failed, R.drawable.error_icon, true)
                        }
                    }
                }
            }, R.string.str_cancel, null)
    }

    fun commentReactionIconsButton(strCommentKey: String, commentData: CB_Comment)
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.ITEM_LIST_DIALOG))
            return

        val listItem = arrayListOf(
            DialogItem(getString(R.string.str_none_icon), R.drawable.ic_baseline_do_not_disturb_24,
                callback = { setCommentReactionIcon(REACTION_TYPE.NONE, strCommentKey, commentData) }),

            DialogItem(getString(R.string.str_heart_icon), R.drawable.heart_icon,
            callback = { setCommentReactionIcon(REACTION_TYPE.HEART, strCommentKey, commentData) }),

            DialogItem(getString(R.string.str_like_icon), R.drawable.like_icon,
            callback = { setCommentReactionIcon(REACTION_TYPE.LIKE, strCommentKey, commentData) }),

            DialogItem(getString(R.string.str_check_icon), R.drawable.check_icon,
            callback = { setCommentReactionIcon(REACTION_TYPE.CHECK, strCommentKey, commentData) }),

            DialogItem(getString(R.string.str_haha_icon), R.drawable.haha_icon,
            callback = { setCommentReactionIcon(REACTION_TYPE.HAHA, strCommentKey, commentData) }),

            DialogItem(getString(R.string.str_wow_icon), R.drawable.wow_icon,
            callback = { setCommentReactionIcon(REACTION_TYPE.WOW, strCommentKey, commentData) }),

            DialogItem(getString(R.string.str_sad_icon), R.drawable.sad_icon,
            callback = { setCommentReactionIcon(REACTION_TYPE.SAD, strCommentKey, commentData) })

            // ADD ICON IF UPDATED
        )

        CB_ItemListDialog(requireActivity(), getString(R.string.str_reaction_icons), listItem, true)
    }

    fun setCommentReactionIcon(iReactionType: REACTION_TYPE, strCommentKey: String, commentData: CB_Comment)
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        if(commentData.iIconType == iReactionType.ordinal)
            return

        val dialog = CB_LoadingDialog(requireActivity()).apply { show() }
        CB_AppFunc.networkScope.launch {

            try
            {
                // previous data and modify
                commentData.iIconType = iReactionType.ordinal
                commentRef.child(strCommentKey).setValue(commentData).await()
                Log.d(strTag, "commentRef updated")

                launch(Dispatchers.Main)
                {
                    dialog.cancel()
                }
            }
            catch (e: FirebaseException)
            {
                e.printStackTrace()
                launch(Dispatchers.Main)
                {
                    dialog.cancel()
                    CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                        R.string.str_reaction_icon_attach_failed, R.drawable.error_icon, true)
                }
            }
        }
    }

    fun setPostReactionIcon(iReactionType: REACTION_TYPE)
    {
        val postData = CB_ViewModel.tPost.value!!

        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        if(postData.iIconType == iReactionType.ordinal)
            return

        val dialog = CB_LoadingDialog(requireActivity()).apply { show() }
        CB_AppFunc.networkScope.launch {

            try
            {
                // previous data and modify
                postData.iIconType = iReactionType.ordinal
                postRef.setValue(postData).await()
                Log.d(strTag, "postRef updated")

                launch(Dispatchers.Main)
                {
                    dialog.cancel()
                }
            }
            catch (e: FirebaseException)
            {
                e.printStackTrace()
                launch(Dispatchers.Main)
                {
                    dialog.cancel()
                    CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                        R.string.str_reaction_icon_attach_failed, R.drawable.error_icon, true)
                }
            }
        }
    }

    fun editPost()
    {
        // move to NewPostFragment for editing
        val arguments = bundleOf(ARGU_POST_KEY to postKey)
        beginAction(R.id.action_CB_PostDetailFragment_to_CB_NewPostFragment, R.id.CB_PostDetailFragment, arguments)
    }

    fun deletePost()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        // we ask the user if you really want to delete post
        CB_AppFunc.confirmDialog(requireActivity(), R.string.str_warning, R.string.str_delete_msg,
            R.drawable.warning_icon, true,
            R.string.str_delete,
            yesListener = { _, _ ->

                // if user really want to delete, delete post
                val dialog = CB_LoadingDialog(requireContext()).apply { show() }

                CB_AppFunc.networkScope.launch {

                    try
                    {
                        // delete post, post comments
                        val imgPath = CB_ViewModel.tPost.value!!.strImgPath
                        Log.d(strTag, "strImgPath:$imgPath")
                        if(!imgPath.isNullOrEmpty())
                        {
                            // find upper folder for post
                            CB_AppFunc.deleteFileFromStorage(imgPath, strTag,
                                "post image deleted path:$imgPath",
                                "post image delete failed path:$imgPath")
                        }

                        postRef.setValue(null).await()
                        commentRef.setValue(null).await()
                        Log.d(strTag, "postRef, commentRef deleted")

                        launch(Dispatchers.Main)
                        {
                            // if success, move to MainFragment
                            dialog.cancel()
                            CB_SingleSystemMgr.showToast(R.string.str_post_deleted)
                            backPressed()
                        }
                    }
                    catch(e: FirebaseException)
                    {
                        // if fail, error dialog
                        e.printStackTrace()
                        launch(Dispatchers.Main)
                        {
                            dialog.cancel()
                            CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                                R.string.str_post_delete_failed, R.drawable.error_icon, true)
                        }
                    }
                }

            }, R.string.str_cancel, null)
    }

    fun profileButton(strUid: String)
    {
        beginAction(R.id.action_CB_PostDetailFragment_to_CB_ProfileInfoFragment,
            R.id.CB_PostDetailFragment, bundleOf(CB_ProfileInfoFragment.ARGU_UID to strUid))
    }

    fun postButton()
    {
        CB_AppFunc.clearFocusing(requireActivity())

        // 포스트 내용을 알아온다.
        val strComment = binding.commentEditText.text.toString()
        if(strComment.isEmpty())
        {
            CB_AppFunc.okDialog(requireActivity(), R.string.str_warning,
                R.string.str_input_comment, R.drawable.warning_icon, true)
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

                launch(Dispatchers.Main)
                {
                    dialog.cancel()
                    CB_SingleSystemMgr.showToast(R.string.str_comment_posted)

                    // scroll to bottom
                    binding.scrollView.fullScroll(View.FOCUS_DOWN)
                }
            }
            catch(e: FirebaseException)
            {
                e.printStackTrace()
                launch(Dispatchers.Main)
                {
                    dialog.cancel()
                    CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                        R.string.str_post_failed, R.drawable.error_icon, true)
                }
            }
        }

    }

}