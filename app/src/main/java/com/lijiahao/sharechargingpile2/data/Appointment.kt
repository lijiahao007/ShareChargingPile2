package com.lijiahao.sharechargingpile2.data

data class Appointment(
    val id: Int,
    val beginDateTime: String, // LocalDateTime
    val endDateTime: String, // LocalDateTime
    val pileId: Int,
    val userId: Int,
    val stationId: Int
) {
    companion object {
        const val STATE_FINISH = "已完成"
        const val STATE_CANCEL = "已取消"
        const val STATE_WAITING = "待使用"
    }
}
