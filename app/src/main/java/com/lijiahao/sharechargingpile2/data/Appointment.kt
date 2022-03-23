package com.lijiahao.sharechargingpile2.data

data class Appointment(
    val id: Int,
    val beginTime: String,
    val endTime: String,
    val pileId: Int,
    val userId: Int
)
