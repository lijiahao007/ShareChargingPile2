package com.lijiahao.sharechargingpile2.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class Appointment(
    val id: Int,
    val beginDateTime: String, // LocalDateTime
    val endDateTime: String, // LocalDateTime
    val pileId: Int,
    val userId: Int,
    val stationId: Int,
    val state: String
) {

    fun getBeginDateTime(): LocalDateTime {
        return LocalDateTime.parse(beginDateTime)
    }

    fun getEndDateTime(): LocalDateTime {
        return LocalDateTime.parse(endDateTime)
    }

    fun getBeginTime(): LocalTime {
        return getBeginDateTime().toLocalTime()
    }

    fun getEndTime(): LocalTime {
        return getEndDateTime().toLocalTime()
    }

    fun getDate(): LocalDate {
        return getBeginDateTime().toLocalDate()
    }


    companion object {
        const val STATE_FINISH = "已完成"
        const val STATE_CANCEL = "已取消"
        const val STATE_WAITING = "待使用"
        const val STATE_OUT_DATE = "已过期"
    }
}
