package com.lijiahao.sharechargingpile2.network.request

import java.time.LocalDateTime

data class ModifyAppointmentRequest(
    val id: Int,
    val beginDateTime: String,
    val endDateTime: String
)