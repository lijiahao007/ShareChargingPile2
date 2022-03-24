package com.lijiahao.sharechargingpile2.data

// station中的充电桩
data class ChargingPile(
    val id:Int,
    val electricType:String,
    val powerRate:Float,
    var pileNum:Int,
    val stationId: Int,
    val state:String
)