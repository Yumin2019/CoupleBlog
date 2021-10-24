package com.coupleblog.singleton

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coupleblog.R
import com.coupleblog.fragment.PAGE_TYPE
import com.coupleblog.model.CB_User
import com.coupleblog.model.MAIL_TYPE
import com.coupleblog.model.REACTION_TYPE
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue


@BindingAdapter("bind:user_uid")
fun setTextByUid(textView: TextView, strUid: String?)
{
    if(strUid.isNullOrEmpty())
        return

    // current User's uid or couple's uid
    when(strUid)
    {
        CB_AppFunc.getUid() ->
        {
            textView.text = CB_AppFunc.curUser.strUserName
        }

        CB_AppFunc.curUser.strCoupleUid ->
        {
            textView.text = CB_AppFunc.coupleUser.strUserName
        }
        else ->
        {
            // if it's unknowns uid, find user info
            CB_AppFunc.getUsersRoot().child(strUid).addListenerForSingleValueEvent(object: ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot)
                {
                    val userInfo = snapshot.getValue<CB_User>()
                    textView.text = if(userInfo == null)
                    {
                        CB_AppFunc.getString(R.string.str_unknown)
                    }
                    else
                    {
                        userInfo.strUserName
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    textView.text = CB_AppFunc.getString(R.string.str_unknown)
                }
            })
        }
    }
}

@BindingAdapter("bind:heart_icon_tint")
fun setHeartIconTint(imageView: ImageView, boolean: Boolean)
{
    imageView.imageTintList =
    if(boolean)  CB_AppFunc.getColorStateList(R.color.red)
    else         CB_AppFunc.getColorStateList(R.color.grey)
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

@BindingAdapter("bind:mail_type")
fun setCoupleRequestTextView(textView: TextView, iMailType: Int)
{
    // already this user is a couple
    if(!CB_AppFunc.curUser.strCoupleUid.isNullOrEmpty())
    {
        textView.visibility = View.GONE
        return
    }

    if(iMailType == MAIL_TYPE.REQUEST_COUPLE.ordinal)
        textView.visibility = View.VISIBLE
    else
        textView.visibility = View.GONE
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

    // UTC to Local
    val calendar = CB_AppFunc.convertUtcToLocale(strDate)
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
