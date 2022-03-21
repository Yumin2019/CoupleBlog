package com.coupleblog.util

import android.graphics.Bitmap
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coupleblog.R
import com.coupleblog.dialog.EDIT_FIELD_TYPE
import com.coupleblog.fragment.PAGE_TYPE
import com.coupleblog.model.*
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_AppFunc.Companion.toCalendar
import com.coupleblog.singleton.CB_ViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.util.*
import kotlin.math.absoluteValue

@BindingAdapter("bind:bitmap")
fun setBitmapImg(imageView: ImageView, bitmap: Bitmap?)
{
    imageView.visibility = if(bitmap == null) View.GONE else View.VISIBLE
    imageView.setImageBitmap(bitmap)
}

@BindingAdapter("bind:picture_image_path")
fun setPictureImagePath(imageView: ImageView, strPath: String?)
{
    CB_AppFunc.setImageWithGlide(strPath, imageView, null)
}

@BindingAdapter("bind:user_image_path")
fun setUserImagePath(imageView: ImageView, strPath: String?)
{
    CB_AppFunc.setImageWithGlide(strPath, imageView, R.drawable.ic_baseline_account_circle_24)
}

@BindingAdapter("bind:image_uid")
fun setImageUid(imageView: ImageView, strUid: String?)
{
    if(strUid.isNullOrEmpty())
    {
        imageView.setImageResource(R.drawable.ic_baseline_account_circle_24)
        return
    }

    when(strUid)
    {
        CB_AppFunc.getUid() ->
        {
            CB_AppFunc.setImageWithGlide(
                CB_AppFunc.curUser.strImgPath.toString(),
                imageView, R.drawable.ic_baseline_account_circle_24
            )
        }

        CB_AppFunc.curUser.strCoupleUid ->
        {
            CB_AppFunc.setImageWithGlide(
                CB_AppFunc.coupleUser.strImgPath.toString(),
                imageView, R.drawable.ic_baseline_account_circle_24
            )
        }
        else ->
        {
            // if it's unknowns uid, find user info
            CB_AppFunc.getUsersRoot().child(strUid).addListenerForSingleValueEvent(object: ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot)
                {
                    val userInfo = snapshot.getValue<CB_User>()
                    if(userInfo == null)
                        imageView.setImageResource(R.drawable.ic_baseline_account_circle_24)
                    else
                        CB_AppFunc.setImageWithGlide(
                            userInfo.strImgPath.toString(),
                            imageView, R.drawable.ic_baseline_account_circle_24
                        )
                }

                override fun onCancelled(error: DatabaseError)
                {
                    imageView.setImageResource(R.drawable.ic_baseline_account_circle_24)
                }
            })
        }
    }
}

@BindingAdapter("bind:field_type_hint")
fun setFieldTypeHint(textView: TextView, iFieldType: Int)
{
    val strText = when(iFieldType)
    {
        EDIT_FIELD_TYPE.NAME                        .ordinal -> CB_AppFunc.getString(R.string.str_user_name)
        EDIT_FIELD_TYPE.IMAGE                       .ordinal -> CB_AppFunc.getString(R.string.str_user_image)
        EDIT_FIELD_TYPE.EMAIL                       .ordinal -> CB_AppFunc.getString(R.string.str_email)
        EDIT_FIELD_TYPE.GENDER                      .ordinal -> CB_AppFunc.getString(R.string.str_gender)
        EDIT_FIELD_TYPE.DATE_OF_BIRTH               .ordinal -> CB_AppFunc.getString(R.string.str_date_of_birth)
        EDIT_FIELD_TYPE.REGION                      .ordinal -> CB_AppFunc.getString(R.string.str_region)
        EDIT_FIELD_TYPE.INTRODUCTION                .ordinal -> CB_AppFunc.getString(R.string.str_introduction)
        EDIT_FIELD_TYPE.EDUCATION                   .ordinal -> CB_AppFunc.getString(R.string.str_education)
        EDIT_FIELD_TYPE.CAREER                      .ordinal -> CB_AppFunc.getString(R.string.str_career)
        EDIT_FIELD_TYPE.PHONE_NUMBER                .ordinal -> CB_AppFunc.getString(R.string.str_phone_number)
        EDIT_FIELD_TYPE.FAVORITES                   .ordinal -> CB_AppFunc.getString(R.string.str_favorites)
        EDIT_FIELD_TYPE.DISLIKES                    .ordinal -> CB_AppFunc.getString(R.string.str_dislikes)
        EDIT_FIELD_TYPE.RESET_PASSWORD_EMAIL        .ordinal -> CB_AppFunc.getString(R.string.str_input_email)
        else -> ""
    }
    textView.hint = strText
}

@BindingAdapter("bind:field_type")
fun setFieldType(textView: TextView, iFieldType: Int)
{
    val strText = when(iFieldType)
    {
        EDIT_FIELD_TYPE.NAME                        .ordinal -> CB_AppFunc.getString(R.string.str_user_name)
        EDIT_FIELD_TYPE.IMAGE                       .ordinal -> CB_AppFunc.getString(R.string.str_user_image)
        EDIT_FIELD_TYPE.EMAIL                       .ordinal -> CB_AppFunc.getString(R.string.str_email)
        EDIT_FIELD_TYPE.GENDER                      .ordinal -> CB_AppFunc.getString(R.string.str_gender)
        EDIT_FIELD_TYPE.DATE_OF_BIRTH               .ordinal -> CB_AppFunc.getString(R.string.str_date_of_birth)
        EDIT_FIELD_TYPE.REGION                      .ordinal -> CB_AppFunc.getString(R.string.str_region)
        EDIT_FIELD_TYPE.INTRODUCTION                .ordinal -> CB_AppFunc.getString(R.string.str_introduction)
        EDIT_FIELD_TYPE.EDUCATION                   .ordinal -> CB_AppFunc.getString(R.string.str_education)
        EDIT_FIELD_TYPE.CAREER                      .ordinal -> CB_AppFunc.getString(R.string.str_career)
        EDIT_FIELD_TYPE.PHONE_NUMBER                .ordinal -> CB_AppFunc.getString(R.string.str_phone_number)
        EDIT_FIELD_TYPE.FAVORITES                   .ordinal -> CB_AppFunc.getString(R.string.str_favorites)
        EDIT_FIELD_TYPE.DISLIKES                    .ordinal -> CB_AppFunc.getString(R.string.str_dislikes)
        EDIT_FIELD_TYPE.RESET_PASSWORD_EMAIL        .ordinal -> CB_AppFunc.getString(R.string.str_email)

        else -> ""
    }
    textView.text = strText
}

@BindingAdapter("bind:field_button_text")
fun setFieldButtonText(textView: TextView, iFieldType: Int)
{
    textView.text = CB_AppFunc.getString(R.string.str_edit)

    if(iFieldType == EDIT_FIELD_TYPE.RESET_PASSWORD_EMAIL.ordinal)
        textView.text = CB_AppFunc.getString(R.string.str_send)
}

@BindingAdapter("bind:profile_user_name")
fun setProfileUserName(textView: TextView, userData: CB_User)
{
    var strName = userData.strUserName
    if(CB_ViewModel.isMyProfile.value == true)
        strName += CB_AppFunc.getString(R.string.str_me)
    textView.text = strName
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
    textView.text = CB_AppFunc.getDateStringWithoutTime(calendar)
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
                       else CB_AppFunc.getString(R.string.str_years_ago)
    }
    else if(iMonth > 0) // 1 ~ 11 months
    {
        // 1month ago, 2months ago
        strPresence += "$iMonth"
        strPresence += if(iMonth == 1) CB_AppFunc.getString(R.string.str_month_ago)
                       else CB_AppFunc.getString(R.string.str_months_ago)
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

@BindingAdapter(value = ["bind:days_time", "bind:show_date"], requireAll = true)
fun setDaysTime(textView: TextView?, tDays: CB_Days, showDate: Boolean): String
{
    val eventCalendar = tDays.strEventDate.toCalendar()
    val curCalendar = CB_AppFunc.getCurCalendar()
    var eventDate = eventCalendar.time
    var curDate = curCalendar.time
    var iMin = ((curDate.time - eventDate.time) / (60 * 1000)).toInt() // millisecond to minute

    if(tDays.iEventType == DAYS_ITEM_TYPE.FUTURE_EVENT.ordinal)
        iMin *= -1;

    var iHour = iMin / 60
    var iDay = iHour / 24
    val iMonth = iDay / 30
    val iYear = iMonth / 12
    var strDate = ""

    // 특정 시점을 기준으로 시간이 얼마나 지났는지 판단한다.
    when(tDays.iTimeFormat)
    {
        DAYS_TIME_FORMAT.DAYS.ordinal ->
        {
            // Today / %d days
            strDate = when (iDay) {
                0 -> CB_AppFunc.getString(R.string.str_today)
                else -> iDay.toString() + " " + CB_AppFunc.getString(R.string.str_days)
            }
        }

        DAYS_TIME_FORMAT.MONTHS.ordinal ->
        {
            // today / 1 month / %d months / %d days
            strDate = when {
                iDay == 0 -> CB_AppFunc.getString(R.string.str_today)
                iMonth == 1 -> CB_AppFunc.getString(R.string.str_one_month)
                iMonth > 1 -> iMonth.toString() + " " + CB_AppFunc.getString(R.string.str_months)
                else -> iDay.toString() + " " + CB_AppFunc.getString(R.string.str_days)
            }
        }

        DAYS_TIME_FORMAT.YEARS.ordinal ->
        {
            // today / 1 year / %d years / 1 month / %d months / %d days
            strDate = when {
                iDay == 0 -> CB_AppFunc.getString(R.string.str_today)
                iYear == 1 -> CB_AppFunc.getString(R.string.str_one_year)
                iYear > 1 -> iYear.toString() + " " + CB_AppFunc.getString(R.string.str_years)
                iMonth == 1 -> CB_AppFunc.getString(R.string.str_one_month)
                iMonth > 1 -> iMonth.toString() + " " + CB_AppFunc.getString(R.string.str_months)
                else -> iDay.toString() + " " + CB_AppFunc.getString(R.string.str_days)
            }
        }
    }

    when(tDays.iEventType)
    {
        DAYS_ITEM_TYPE.PAST_EVENT.ordinal ->
        {
            if(showDate)
                strDate += "(" + CB_AppFunc.getDateStringWithoutTime(eventCalendar) + ")"
        }

        DAYS_ITEM_TYPE.FUTURE_EVENT.ordinal ->
        {
            if(iDay > 0)
            {
                strDate += " " + CB_AppFunc.getString(R.string.str_left)
            }
            else if(iDay < 0)
            {
                strDate = CB_AppFunc.getString(R.string.str_finished)
            }

            if(showDate)
                strDate += "(" + CB_AppFunc.getDateStringWithoutTime(eventCalendar) + ")"
        }

        DAYS_ITEM_TYPE.ANNUAL_EVENT.ordinal ->
        {
            // 연도를 맞추고 날짜를 다시 계산한다.
            eventCalendar[Calendar.YEAR] = curCalendar[Calendar.YEAR]
            if(eventCalendar[Calendar.MONTH] == curCalendar[Calendar.MONTH] &&
                    eventCalendar[Calendar.DAY_OF_MONTH] == curCalendar[Calendar.DAY_OF_MONTH])
            {
                strDate = CB_AppFunc.getString(R.string.str_today)

                if(showDate)
                    strDate += "(" + CB_AppFunc.getDateStringWithoutTime(eventCalendar) + ")"

                textView?.text = strDate
                return strDate
            }

            if(eventCalendar <= curCalendar)
            {
                // past
                eventCalendar[Calendar.YEAR] = curCalendar[Calendar.YEAR] + 1
            }

            eventDate = eventCalendar.time
            curDate = curCalendar.time
            iMin = ((curDate.time - eventDate.time).absoluteValue / (60 * 1000)).toInt() // millisecond to minute
            iHour = iMin / 60
            iDay = iHour / 24
            strDate = iDay.toString() + " " + CB_AppFunc.getString(R.string.str_days)
            strDate += " " + CB_AppFunc.getString(R.string.str_left)

            if(showDate)
                strDate += "(" + CB_AppFunc.getDateStringWithoutTime(eventCalendar) + ")"
        }
    }

    textView?.text = strDate
    return strDate
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
    if(boolean) CB_AppFunc.getColorStateList(R.color.red)
    else CB_AppFunc.getColorStateList(R.color.grey)
}

@BindingAdapter("bind:layout_manager")
fun setLayoutManager(recyclerView: RecyclerView, layoutManager: RecyclerView.LayoutManager?)
{
    recyclerView.layoutManager = layoutManager
}

@BindingAdapter("bind:has_fixed_size")
fun setHasFixedSize(recyclerView: RecyclerView, boolean: Boolean)
{
    recyclerView.setHasFixedSize(boolean)
}

@BindingAdapter("bind:item_view_cache_size")
fun setItemViewCacheSize(recyclerView: RecyclerView, iSize: Int)
{
    recyclerView.setItemViewCacheSize(iSize)
}

@BindingAdapter("bind:adapter")
fun setAdapter(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>?)
{
    recyclerView.adapter = adapter
}

@BindingAdapter("bind:adapter")
fun setAdapter(autoTextView: AutoCompleteTextView, adapter: ArrayAdapter<*>?)
{
    autoTextView.setAdapter(adapter)
}

@BindingAdapter("bind:visibility")
fun setVisibility(view: View, flag: Boolean?)
{
    view.visibility = if(flag == true) View.VISIBLE else View.GONE
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

@BindingAdapter("bind:visibility")
fun setVisibility(view: View, iSize: Int)
{
    view.visibility = if(iSize > 0) View.VISIBLE else View.GONE
}

@BindingAdapter("bind:isEmpty")
fun setIsEmpty(view: View, iSize: Int)
{
    view.visibility = if(iSize == 0) View.VISIBLE else View.GONE
}

@BindingAdapter("bind:isNotEmpty")
fun setIsNotEmpty(view: View, iSize: Int)
{
    view.visibility = if(iSize != 0) View.VISIBLE else View.GONE
}

@BindingAdapter("bind:password_type_or_email")
fun setPasswordInputTypeOrEmail(editText: TextInputEditText, isPassword: Boolean)
{
    editText.inputType = if(isPassword) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                         else           InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
}

@BindingAdapter("bind:password_layout_or_not")
fun setPasswordLayoutOrNot(inputLayout: TextInputLayout, isPassword: Boolean)
{
    inputLayout.endIconMode = if(isPassword) TextInputLayout.END_ICON_PASSWORD_TOGGLE
                              else           TextInputLayout.END_ICON_NONE

    inputLayout.error = null
}

@BindingAdapter("bind:request_mail")
fun setCoupleRequestTextView(textView: TextView, tMail: CB_Mail)
{
    // already this user is a couple or not request mail
    if(!CB_AppFunc.curUser.strCoupleUid.isNullOrEmpty() || tMail.iMailType != MAIL_TYPE.REQUEST_COUPLE.ordinal)
    {
        textView.visibility = View.GONE
        return
    }

    // expired
    val diffDays = CB_AppFunc.getTimeDiffDays(tMail.strSendDate.toCalendar())
    if(diffDays > 7)
    {
        textView.visibility = View.GONE
        return
    }

    textView.visibility = View.VISIBLE
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

@BindingAdapter("bind:days_calendar_text")
fun setDaysCalendarText(textView: TextView, strDate: String)
{
    val calendar = CB_AppFunc.stringToCalendar(strDate)
    val iDaysEventFormat = CB_ViewModel.iDaysEventType.value!!
    textView.text = when(iDaysEventFormat)
    {
        DAYS_ITEM_TYPE.ANNUAL_EVENT.ordinal ->
        {
            CB_AppFunc.getDayStringForOutput(calendar)
        }

        else -> {
            CB_AppFunc.getDateStringWithoutTime(calendar)
        }
    }
}

@BindingAdapter("bind:drawable_name")
fun setImage(imageView: ImageView, strDrawableName: String?)
{
    if(strDrawableName.isNullOrEmpty())
    {
        imageView.setImageDrawable(null)
        return
    }

    val iResIdx = CB_AppFunc.getDrawableIdentifier(CB_AppFunc.application, strDrawableName)
    if(iResIdx == 0)
    {
        imageView.setImageDrawable(null)
        return
    }

    imageView.setImageResource(iResIdx)
}

@BindingAdapter("bind:drawable")
fun setImage(imageView: ImageView, @DrawableRes iResIdx: Int)
{
    imageView.setImageResource(iResIdx)
}

@BindingAdapter("bind:image_color")
fun setImageColor(imageView: ImageView, @ColorRes iColorRes: Int)
{
    if(iColorRes == -1) return
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
