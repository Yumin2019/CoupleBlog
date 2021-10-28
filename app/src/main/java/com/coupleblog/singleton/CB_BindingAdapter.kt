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

@BindingAdapter("bind:user_presence")
fun setUserPresence(textView: TextView, userData: CB_User)
{
    if(userData.isOnline == true)
    {
        // online status
        textView.setTextColor(CB_AppFunc.getColorStateList(R.color.green))
        textView.text = CB_AppFunc.getString(R.string.str_online)
        return
    }

    textView.setTextColor(CB_AppFunc.getColorStateList(R.color.grey))

    // if this user is offline, set format strings
    if(userData.strLogoutDate.isNullOrEmpty())
    {
        textView.text = ""
        return
    }

    // 현재 시간 - 로그아웃 시간(영국 기준)
    val logoutDate = CB_AppFunc.stringToCalendar(userData.strLogoutDate).time
    val curDate = CB_AppFunc.getCalendarForSave().time
    var iMin = (curDate.time - logoutDate.time).toInt() / (60 * 1000) // millisecond to minute
    var iHour = iMin / 60
    val iDay = iHour / 24
    val iMonth = iDay / 30
    val iYear = iMonth / 12
    var strPresence = "◆ "

    if(iYear > 0) // n years
    {
        // 1year ago, 2years ago
        strPresence += "$iYear"
        strPresence += if(iYear == 1) CB_AppFunc.getString(R.string.str_year_ago)
                       else           CB_AppFunc.getString(R.string.str_years_ago)
    }
    else if(iMonth > 0) // 1 ~ 11 months
    {
        // 1month ago, 2months ago
        strPresence += "$iMonth"
        strPresence += if(iMonth == 1) CB_AppFunc.getString(R.string.str_month_ago)
                       else            CB_AppFunc.getString(R.string.str_months_ago)
    }
    else if(iDay > 0) // 1 ~ 29 days
    {
        CB_AppFunc.getString(R.string.str_h)
        // 2d ago, 1d 3h ago
        iHour %= 24
        strPresence += "$iDay" + CB_AppFunc.getString(R.string.str_d)
        strPresence += if(iHour > 0)
                           "$iHour" + CB_AppFunc.getString(R.string.str_h_ago)
                       else
                           CB_AppFunc.getString(R.string.str_ago)
    }
    else if(iHour > 0) // 1 ~ 23 hours
    {
        // 1h ago, 1h 25m ago
        iMin %= 60
        strPresence += "$iHour" + CB_AppFunc.getString(R.string.str_h)
        strPresence += if(iMin > 0)
                          "$iMin" + CB_AppFunc.getString(R.string.str_m_ago)
                       else
                           CB_AppFunc.getString(R.string.str_ago)
    }
    else // 0 ~ 59 minutes
    {
        strPresence += "$iMin" + CB_AppFunc.getString(R.string.str_m_ago)
    }

    textView.text = strPresence
}

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

@BindingAdapter("bind:visibility")
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
