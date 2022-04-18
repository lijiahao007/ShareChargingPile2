package com.lijiahao.sharechargingpile2.data

data class ElectricChargePeriod(
    val id: Int,
    var beginTime: String,
    var endTime: String,
    val stationId: Int,
    var electricCharge: Float
)
