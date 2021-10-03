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
    var strUerName           : String? = ""  ,      // displayName
    var strUserEmail         : String? = ""  ,      // 유저 이메일
    /* var strUserImgUrl       : String? = ""*/      // 유저 프로필 이미지 Url

    var strCoupleUid         : String? = ""  ,      // 커플 uid
    var iCoupleDays          : Int     = 0   ,
    var strSignUpDate        : String? = ""  ,
)
