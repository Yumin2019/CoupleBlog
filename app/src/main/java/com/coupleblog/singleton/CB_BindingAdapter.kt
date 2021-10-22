package com.coupleblog.singleton

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coupleblog.R
import com.coupleblog.adapter.CB_EmailAdapter
import com.coupleblog.fragment.PAGE_TYPE
import com.coupleblog.model.REACTION_TYPE
import com.google.android.material.floatingactionbutton.FloatingActionButton


@BindingAdapter("bind:user_uid")
fun setTextByUid(textView: TextView, strUid: String?)
{
    // current User's uid or couple's uid
    textView.text =
        if (strUid == CB_AppFunc.getUid())
            CB_AppFunc.curUser.strUserName
        else
            CB_AppFunc.coupleUser.strUserName
}

@BindingAdapter("bind:heart_icon_tint")
fun setHeartIconTint(imageView: ImageView, boolean: Boolean)
{
    imageView.imageTintList =
    if(boolean)  CB_AppFunc.getColorStateList(R.color.red)
    else         CB_AppFunc.getColorStateList(R.color.gray)
}

@BindingAdapter("bind:layout_manager")
fun setLayoutManager(recyclerView: RecyclerView, layoutManager: RecyclerView.LayoutManager?)
{
    recyclerView.layoutManager = layoutManager
}

@BindingAdapter("bind:adapter")
fun setAdapter(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>?)
{
    recyclerView.adapter = adapter
}

@BindingAdapter("bind:visible")
fun setVisibility(view: View, flag: Boolean?)
{
    view.visibility = if(flag == true) View.VISIBLE else View.GONE
}

@BindingAdapter("bind:page_idx")
fun setFloatingButtonImg(floatingActionButton: FloatingActionButton, iPageIdx: Int)
{
    when(iPageIdx)
    {
        PAGE_TYPE.MY_POSTS.ordinal ->
        {
            floatingActionButton.setImageResource(R.drawable.ic_add)
        }

        PAGE_TYPE.MAILBOX.ordinal ->
        {
            floatingActionButton.setImageResource(R.drawable.ic_baseline_mail_24)
        }
        else -> {}
    }
}

@BindingAdapter("bind:comment_uid")
fun setVisibility(view: View, strAuthorUid: String?)
{
    // if this user has a permission, visible or gone
    if(CB_ViewModel.isMyPost.value!! || strAuthorUid == CB_AppFunc.getUid())
    {
        view.visibility = View.VISIBLE
    }
    else
    {
        view.visibility = View.GONE
    }
}

@BindingAdapter("bind:date_text")
fun setDateText(textView: TextView, strDate: String?)
{
    if(strDate == null)
        return

    val calendar = CB_AppFunc.stringToCalendar(strDate)
    textView.text = CB_AppFunc.getDateStringForOutput(calendar)
}

@BindingAdapter("bind:drawable_id")
fun setImage(imageView: ImageView, iResIdx: Int)
{
    imageView.setImageResource(iResIdx)
}

@BindingAdapter("bind:color_id")
fun setImageColor(imageView: ImageView, iColorRes: Int)
{
    if(iColorRes == -1)
        return
    imageView.imageTintList = CB_AppFunc.getColorStateList(iColorRes)
}
@BindingAdapter("bind:icon_image")
fun setIconImage(imageView: ImageView, iIconType: Int)
{
    val resId = when(iIconType)
    {
        REACTION_TYPE.CHECK.ordinal  ->  R.drawable.check_icon
        REACTION_TYPE.HAHA.ordinal   ->  R.drawable.haha_icon
        REACTION_TYPE.LIKE.ordinal   ->  R.drawable.like_icon
        REACTION_TYPE.HEART.ordinal  ->  R.drawable.heart_icon
        REACTION_TYPE.SAD.ordinal    ->  R.drawable.sad_icon
        REACTION_TYPE.WOW.ordinal    ->  R.drawable.wow_icon
        else ->  -1 // NONE
    }

    if (resId != -1)
    {
        imageView.visibility = View.VISIBLE
        imageView.setImageResource(resId)
    }
    else
    {
        imageView.visibility = View.GONE
    }
}
