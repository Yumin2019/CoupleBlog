package com.coupleblog.singleton

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coupleblog.R
import com.coupleblog.dialog.EDIT_FIELD_TYPE
import com.coupleblog.fragment.PAGE_TYPE
import com.coupleblog.model.CB_User
import com.coupleblog.model.GENDER
import com.coupleblog.model.MAIL_TYPE
import com.coupleblog.model.REACTION_TYPE
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

@BindingAdapter("bind:field_type_hint")
fun setFieldTypeHint(textView: TextView, iFieldType: Int)
{
    val strText = when(iFieldType)
    {
        EDIT_FIELD_TYPE.NAME         .ordinal -> CB_AppFunc.getString(R.string.str_user_name)
        EDIT_FIELD_TYPE.IMAGE        .ordinal -> CB_AppFunc.getString(R.string.str_user_image)
        EDIT_FIELD_TYPE.EMAIL        .ordinal -> CB_AppFunc.getString(R.string.str_email)
        EDIT_FIELD_TYPE.GENDER       .ordinal -> CB_AppFunc.getString(R.string.str_gender)
        EDIT_FIELD_TYPE.DATE_OF_BIRTH.ordinal -> CB_AppFunc.getString(R.string.str_date_of_birth)
        EDIT_FIELD_TYPE.REGION       .ordinal -> CB_AppFunc.getString(R.string.str_region)
        EDIT_FIELD_TYPE.INTRODUCTION .ordinal -> CB_AppFunc.getString(R.string.str_introduction)
        EDIT_FIELD_TYPE.EDUCATION    .ordinal -> CB_AppFunc.getString(R.string.str_education)
        EDIT_FIELD_TYPE.CAREER       .ordinal -> CB_AppFunc.getString(R.string.str_career)
        EDIT_FIELD_TYPE.PHONE_NUMBER .ordinal -> CB_AppFunc.getString(R.string.str_phone_number)
        EDIT_FIELD_TYPE.FAVORITES    .ordinal -> CB_AppFunc.getString(R.string.str_favorites)
        EDIT_FIELD_TYPE.DISLIKES     .ordinal -> CB_AppFunc.getString(R.string.str_dislikes)
        else -> ""
    }
    textView.hint = strText
}

@BindingAdapter("bind:field_type")
fun setFieldType(textView: TextView, iFieldType: Int)
{
    val strText = when(iFieldType)
    {
        EDIT_FIELD_TYPE.NAME         .ordinal -> CB_AppFunc.getString(R.string.str_user_name)
        EDIT_FIELD_TYPE.IMAGE        .ordinal -> CB_AppFunc.getString(R.string.str_user_image)
        EDIT_FIELD_TYPE.EMAIL        .ordinal -> CB_AppFunc.getString(R.string.str_email)
        EDIT_FIELD_TYPE.GENDER       .ordinal -> CB_AppFunc.getString(R.string.str_gender)
        EDIT_FIELD_TYPE.DATE_OF_BIRTH.ordinal -> CB_AppFunc.getString(R.string.str_date_of_birth)
        EDIT_FIELD_TYPE.REGION       .ordinal -> CB_AppFunc.getString(R.string.str_region)
        EDIT_FIELD_TYPE.INTRODUCTION .ordinal -> CB_AppFunc.getString(R.string.str_introduction)
        EDIT_FIELD_TYPE.EDUCATION    .ordinal -> CB_AppFunc.getString(R.string.str_education)
        EDIT_FIELD_TYPE.CAREER       .ordinal -> CB_AppFunc.getString(R.string.str_career)
        EDIT_FIELD_TYPE.PHONE_NUMBER .ordinal -> CB_AppFunc.getString(R.string.str_phone_number)
        EDIT_FIELD_TYPE.FAVORITES    .ordinal -> CB_AppFunc.getString(R.string.str_favorites)
        EDIT_FIELD_TYPE.DISLIKES     .ordinal -> CB_AppFunc.getString(R.string.str_dislikes)
        else -> ""
    }
    textView.text = strText
}

@BindingAdapter("bind:joined_date")
fun setJoinDate(textView: TextView, userData: CB_User)
{
    textView.visibility = View.GONE
    if(userData.strSignUpDate.isNullOrEmpty())
        return

    // according to gmt time
    val joinedDate = CB_AppFunc.stringToCalendar(userData.strSignUpDate).time
    val curDate = CB_AppFunc.getCalendarForSave().time

    // milliseconds to days
    val days = ((curDate.time - joinedDate.time) / (24 * 60 * 60 * 1000)).toInt()

    // if it's under 1 day, do not write
    if(days < 1)
        return

    textView.visibility = View.VISIBLE
    textView.text = if(days == 1)
                        CB_AppFunc.getString(R.string.str_joined_one_day_ago)
                    else
                        CB_AppFunc.getString(R.string.str_joined_n_days_ago).format(days)
}

@BindingAdapter("bind:region")
fun setRegion(textView: TextView, userData: CB_User)
{
    with(CB_AppFunc)
    {
        var strText = ""
        if(userData.iGmtOffset != null)
        {
            // get user's local time to string 03:20 PM
            val date = convertUtcToLocale(userData.iGmtOffset!!).time
            strText += strTimeOutputFormat.format(date)

            if(!userData.strRegion.isNullOrEmpty())
                strText += ", ${userData.strRegion}" // 03:20 PM, Korea
            else
                strText += ", " + getString(R.string.str_not_set)
        }

        textView.text = strText
    }
}

@BindingAdapter("bind:default_text")
fun setDefaultText(textView: TextView, strText: String?)
{
    if(!strText.isNullOrEmpty())
        textView.text = strText
    else
        textView.text = CB_AppFunc.getString(R.string.str_not_set)
}

@BindingAdapter("bind:age")
fun setAge(textView: TextView, userData: CB_User)
{
    textView.text = ""
    if(userData.strBirthDate.isNullOrEmpty())
        return

    if(userData.iGmtOffset == null)
        return

    // UTC Time
    val birthDate = CB_AppFunc.stringToCalendar(userData.strBirthDate).time
    val curDate = CB_AppFunc.getCalendarForSave().time

    // days to years
    val hours = (curDate.time - birthDate.time) / (60 * 60 * 1000)
    textView.text = (hours / (12 * 30 * 24)).toInt().toString()

    // textColor
    val iColor = when(userData.iGender)
    {
        GENDER.MALE.ordinal   -> R.color.blue
        GENDER.FEMALE.ordinal -> R.color.red
        else                  -> R.color.grey
    }

    textView.setTextColor(CB_AppFunc.getColorStateList(iColor))
}

@BindingAdapter("bind:birth_date")
fun setBirthDate(textView: TextView, userData: CB_User)
{
    if(userData.strBirthDate.isNullOrEmpty())
    {
        textView.text = CB_AppFunc.getString(R.string.str_not_set)
        return
    }

    // 20021203, without gmt offset
    val calendar = CB_AppFunc.stringToCalendar(userData.strBirthDate)
    textView.text = CB_AppFunc.getBirthDateString(calendar)
}

@BindingAdapter("bind:gender_idx")
fun setGenderImg(imageView: ImageView, userData: CB_User)
{
    val iRes = when(userData.iGender)
    {
        GENDER.MALE.ordinal   -> R.drawable.male
        GENDER.FEMALE.ordinal -> R.drawable.female
        else                  -> R.drawable.question
    }

    if(iRes != -1)
        imageView.setImageResource(iRes)
}

@BindingAdapter("bind:couple_with")
fun setCoupleWith(textView: TextView, userData: CB_User)
{
    val strText = CB_AppFunc.getString(R.string.str_couple_with) + " ${userData.strUserName}"
    textView.text = strText
}

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
    var iMin = ((curDate.time - logoutDate.time) / (60 * 1000)).toInt() // millisecond to minute
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
        // 2d ago, 1d 3h ago
        iHour %= 24
        strPresence += "$iDay" + CB_AppFunc.getString(R.string.str_d) + " "
        strPresence += if(iHour > 0)
                           "$iHour" + CB_AppFunc.getString(R.string.str_h_ago)
                       else
                           CB_AppFunc.getString(R.string.str_ago)
    }
    else if(iHour > 0) // 1 ~ 23 hours
    {
        // 1h ago, 1h 25m ago
        iMin %= 60
        strPresence += "$iHour" + CB_AppFunc.getString(R.string.str_h) + " "
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
    {
        textView.text = ""
        return
    }

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
    {
        textView.text = ""
        return
    }

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
