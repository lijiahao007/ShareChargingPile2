package com.lijiahao.sharechargingpile2.data

// station中的充电桩
data class ChargingPile(
    val id:Int = 0,
    val electricType:String = "",
    val powerRate:Float = 0.0f,
    val stationId: Int = 0,
    val state:String = "",
    val qrcodeUrl:String? = null,
)
