package com.coupleblog.model

import com.google.firebase.database.IgnoreExtraProperties

/*
    The class must have a default constructor that takes no arguments
    The class must define public getters for the properties to be assigned.
    Properties without a public getter will be set to their default value when an instance is deserialized
    Generic collections of objects that satisfy the above constraints are also permitted,
    i.e. Map<String, MyPOJO>, as well as null values.
 */

@IgnoreExtraProperties
data class CB_User(
    var strUserName           : String? = ""  ,    // displayName
    var strUserEmail         : String? = ""  ,     // 유저 이메일
    var strSignUpDate        : String? = ""  ,
    /* var strUserImgUrl     : String? = ""*/      // 유저 프로필 이미지 Url
    var strCoupleUid         : String? = "",       // 커플 정보 Uid
    var isOnline             : Boolean? = false,   // 온라인 여부
    var strLogoutDate        : String? = "",       // 로그아웃 시기
)
