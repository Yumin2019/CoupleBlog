package com.coupleblog.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class CB_Comment(
    var strAuthorUid        : String? = "",
    var strComment          : String? = "",
    var strCommentDate      : String? = "",
    var iIconType           : Int?    = REACTION_TYPE.NONE.ordinal
)
