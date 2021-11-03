package com.coupleblog.model

import com.coupleblog.singleton.CB_AppFunc
import com.google.firebase.database.IgnoreExtraProperties

/*
    The class must have a default constructor that takes no arguments
    The class must define public getters for the properties to be assigned.
    Properties without a public getter will be set to their default value when an instance is deserialized
    Generic collections of objects that satisfy the above constraints are also permitted,
    i.e. Map<String, MyPOJO>, as well as null values.
 */

enum class GENDER
{
    NONE,
    MALE,
    FEMALE
}

@IgnoreExtraProperties
data class CB_User(
    var strUserName          : String? = "",                // displayName
    var strUserEmail         : String? = "",                // 유저 이메일
    var strSignUpDate        : String? = "",                // 가입 시기
    /* var strUserImgUrl     : String? = ""*/               // 유저 프로필 이미지 Url
    var strCoupleUid         : String? = "",                // 커플 정보 Uid
    var isOnline             : Boolean? = false,            // 온라인 여부
    var strLogoutDate        : String? = "",                // 로그아웃 시기
    var iGender              : Int? = GENDER.NONE.ordinal,  // gender
    var strBirthDate         : String? = "",                // 생년월일 (처리할 때 분, 초는 0)
    var strRegion            : String? = "",                // 국가 및 지역 (설정시 국가, 지역을 분리해서 처리)
    var iGmtOffset           : Int? = CB_AppFunc.getGMTOffset(),                    // gmt offset
    var strIntroduction      : String? = "",                // 자기소개 1000자
    var strEducation         : String? = "",                // 교육사항
    var strCareer            : String? = "",                // 경력 관련
    var strPhoneNumber       : String? = "",                // 핸드폰 번호
    var strFavorites         : String? = "",                // 좋아하는 것
    var strDislikes          : String? = "",                // 싫어하는 것
)
