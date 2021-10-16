package com.coupleblog.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.coupleblog.R
import com.coupleblog.fragment.CB_PostDetailFragment
import com.coupleblog.model.CB_Comment
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr


class CB_CommentEditDialog(context: Activity, fragment: CB_PostDetailFragment,
                           commentData: CB_Comment, commentKey: String, bCancelable: Boolean) : Dialog(context)
{
    init
    {
        val binding: CommentEditItemBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
            R.layout.dialog_cb_comment_edit, null, false)

        setContentView(binding.root)
        binding.apply {
            this.commentData    = commentData
            this.commentKey     = commentKey

            commentPostButton.setOnClickListener {

                // if it's empty dialog and return
                val strComment = commentEditText.text.toString()
                if(strComment.isEmpty())
                {
                    CB_AppFunc.okDialog(context, R.string.str_warning,
                       R.string.str_input_comment, R.drawable.warning_icon, true)
                    return@setOnClickListener
                }

                cancel()
                // dialog dismiss and editComment
                commentData.strComment = strComment
                fragment.editComment(commentKey, commentData)
            }

            CB_AppFunc.openIME(commentEditText, context)
        }

        window!!.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        setCanceledOnTouchOutside(bCancelable)
        CB_SingleSystemMgr.registerDialog(CB_SingleSystemMgr.DIALOG_TYPE.COMMENT_EDIT_DIALOG)
        show()
    }

    override fun dismiss()
    {
        super.dismiss()
        CB_SingleSystemMgr.releaseDialog(CB_SingleSystemMgr.DIALOG_TYPE.COMMENT_EDIT_DIALOG)
    }
}