package com.coupleblog.model

import com.google.firebase.database.IgnoreExtraProperties

// Couple 관련 정보를 저장한다. 이 데이터는
@IgnoreExtraProperties
data class CB_Couple(
    var strCoupleUid         : String? = ""  ,      // 커플 uid
    var strBeginDate         : String? = ""  ,      // 연애 시작한 날 string
    )
