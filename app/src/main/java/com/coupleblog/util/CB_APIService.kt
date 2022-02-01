package com.coupleblog.util

import com.coupleblog.model.CB_Notification
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class CB_Result(
    var error: String? = "",
    var message_id: String? = "",
)

data class CB_Response(
    var success: Int = 0,
    var failure: Int = 0,
    var results: ArrayList<CB_Result> = arrayListOf())

interface CB_APIService
{
    @Headers(
        "Authorization:key=AAAAO6G_v58:APA91bEnxvxDL5FTB-10FEv7XzgYy6bQfAFYt2B-G1zJR3NFr0awn7zH988VQcgCkmx7-FQzVmO7tfYHyaUDWEkjNG7wzb1yjHizMKBAI0-CgHyzz9ualYwnYWurI7Ueijtj5rcdt6YY",
        "Content-Type:application/json"
    )

    @POST("fcm/send")
    fun sendNotification(@Body notification: CB_Notification): Call<CB_Response>
}