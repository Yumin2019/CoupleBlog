package com.coupleblog.model

import com.google.gson.annotations.SerializedName

data class CB_Notification(

    @SerializedName("to")
    var strToken: String? = "",

    @SerializedName("data")
    var tData: CB_FCMData? = CB_FCMData(),
)
