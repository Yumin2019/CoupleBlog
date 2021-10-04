package com.coupleblog.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.util.HashMap

// 포스트할 내용에 대한 정보

enum class REACTION_TYPE
{
    NONE,
    LOVE,
    LIKE,
    CHECK,
    HAHA,
    WOW,
    SAD,
}

@IgnoreExtraProperties
data class CB_Post(
    var strAuthorUid        : String? = "",
    var strTitle            : String? = "",
    var strBody             : String? = "",
    var iIconType           : Int?    = REACTION_TYPE.NONE.ordinal,
    var strPostDate         : String? = "",
    var strRecentEditDate   : String? = "",
    /*var strImgUrl       : String? = ""*/
){
    @Exclude
    fun toMap(): Map<String, Any?>
    {
        return mapOf(
            "strAuthorUid"         to strAuthorUid,
            "strTitle"             to strTitle,
            "strBody"              to strBody,
            "iIconType"            to iIconType,
            "strPostDate"          to strPostDate,
            "strRecentEditDate"    to strRecentEditDate,
        )
    }
}
