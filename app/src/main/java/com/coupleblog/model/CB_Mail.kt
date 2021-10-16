package com.coupleblog.model

import com.google.firebase.database.IgnoreExtraProperties

enum class MAIL_TYPE
{
    NORMAL,
    REQUEST_COUPLE,
    RELEASE_NOTE,

    // add
}

@IgnoreExtraProperties
data class CB_Mail(
    var strSenderUid: String? = "",      // 보낸 사람의 Uid
    var strSendDate: String? = "",      // 보낸 시점
    //var strImageUrl          : String? = ""  ,
    var iMailType: Int? = MAIL_TYPE.NORMAL.ordinal, // 이메일 타입
    var strTitle: String? = "",         // 제목
    var strMessage: String? = "",      // 메세지
    var bRead: Boolean? = false,         // 읽음 여부(내 기준)
    var bHeartIcon: Boolean? = false    // Heart Icon
)