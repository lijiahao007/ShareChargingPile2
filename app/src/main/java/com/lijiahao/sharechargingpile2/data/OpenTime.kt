package com.lijiahao.sharechargingpile2.data

data class OpenTime(
    val id : Int,
    val beginTime: String,
    val endTime: String,
    val electricCharge: Float,
    val stationId: Int
)
