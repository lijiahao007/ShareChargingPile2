package com.lijiahao.sharechargingpile2.data

import java.time.LocalTime

data class TimeBarData(
    val beginTime: LocalTime,
    val endTime: LocalTime,
    val state: String
) {

    companion object {
        const val STATE_USING = "使用中"
        const val STATE_FREE = "空闲"
        const val STATE_SUSPEND = "暂停营业"
        const val APPOINTMENT = "被预约"
        const val MY_APPOINTMENT = "我的预约"
    }
}
