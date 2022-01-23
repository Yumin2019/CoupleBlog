package com.coupleblog.model

import com.google.firebase.database.IgnoreExtraProperties

enum class DAYS_ITEM_TYPE
{
    NONE,
    PAST_EVENT,
    FUTURE_EVENT,
    ANNUAL_EVENT
}

enum class DAYS_TIME_FORMAT
{
    NONE,
    DAYS,
    MONTHS,
    YEARS
}

@IgnoreExtraProperties
data class CB_Days(
    var strTitle: String? = "",             // 제목
    var strDesc: String? = "",              // Description
    var strIconRes: String? = "",           // Icon Res string
    var strEventDate: String? = "",         // Event Date
    var iEventType: Int? = 0,               // Days Type
    var iTimeFormat: Int? = 0,              // Days Time Format type
    var strRecentEditDate: String? = "",    // 최근 편집 시기
    var strRecentEditUid: String? = "",     // 최근 편입자
    var iOrderIdx: Int? = 0                 // order idx
)
{
    fun getEventTypeString(): String
    {
        return when(iEventType)
        {
            DAYS_ITEM_TYPE.PAST_EVENT.ordinal -> "past-event-list"
            DAYS_ITEM_TYPE.FUTURE_EVENT.ordinal -> "future-event-list"
            DAYS_ITEM_TYPE.ANNUAL_EVENT.ordinal -> "annual-event-list"
            else -> ""
        }
    }
}


