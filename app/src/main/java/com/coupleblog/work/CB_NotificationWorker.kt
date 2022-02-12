package com.coupleblog.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.coupleblog.R
import com.coupleblog.singleton.CB_AppFunc


/*** 정해진 시간 이후에 호출되어 정해진 데이터를 이용해 푸시알림을 생성하는 Worker
 *  - DaysItem에 대한 처리에 사용한다.
 *  - inputData: strTitle(EventName), strBody(bodyText) strFcmToken(to)
 */
class CB_NotificationWorker(context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {

    override fun doWork(): Result {
        val strTitle = inputData.getString("strTitle") ?: ""
        val strBody = inputData.getString("strBody") ?: ""
        val strFcmToken = inputData.getString("strFcmToken") ?: ""

        CB_AppFunc.sendFCM(strTitle, strBody, strFcmToken)
        return Result.success()
    }
}