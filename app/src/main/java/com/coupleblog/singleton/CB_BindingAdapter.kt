package com.coupleblog.singleton

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coupleblog.R
import com.coupleblog.model.REACTION_TYPE

@BindingAdapter("bind:layout_manager")
fun setLayoutManager(recyclerView: RecyclerView, layoutManager: RecyclerView.LayoutManager)
{
    recyclerView.layoutManager = layoutManager
}

@BindingAdapter("bind:adapter")
fun setAdapter(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>)
{
    recyclerView.adapter = adapter
}

@BindingAdapter("bind:visible")
fun setVisibility(view: View, flag: Boolean)
{
    view.visibility = if(flag) View.VISIBLE else View.GONE
}

@BindingAdapter("bind:date_text")
fun setDateText(textView: TextView, strDate: String)
{
    val calendar = CB_AppFunc.stringToCalendar(strDate)
    textView.text = CB_AppFunc.getDateStringForOutput(calendar)
}

@BindingAdapter("bind:icon_image")
fun setIconImage(imageView: ImageView, iIconType: Int)
{
    val resId = when(iIconType)
    {
        REACTION_TYPE.CHECK.ordinal -> R.drawable.check_icon
        REACTION_TYPE.HAHA.ordinal -> R.drawable.haha_icon
        REACTION_TYPE.LIKE.ordinal -> R.drawable.like_icon
        REACTION_TYPE.LOVE.ordinal -> R.drawable.love_icon
        REACTION_TYPE.SAD.ordinal -> R.drawable.sad_icon
        REACTION_TYPE.WOW.ordinal -> R.drawable.wow_icon
        else ->  -1
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
