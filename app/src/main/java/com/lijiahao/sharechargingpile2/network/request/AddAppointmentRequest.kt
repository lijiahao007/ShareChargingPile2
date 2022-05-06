package com.lijiahao.sharechargingpile2.network.request

data class AddAppointmentRequest(
    val id: Int = 0,
    val date: String,
    val beginTime: String,
    val endTime: String,
    val pileId: Int,
    val userId: Int,
    val stationId: Int,
    val state: String
)