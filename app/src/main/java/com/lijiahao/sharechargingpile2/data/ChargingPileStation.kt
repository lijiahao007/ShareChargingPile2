package com.lijiahao.sharechargingpile2.data

data class ChargingPileStation(
    var id:Int,
    var latitude: Double,
    var longitude: Double,
    var name: String,
    var posDescription: String,
    var parkFee: Float,
    var collection: Int,
    var userId: Int,
    var createTime: String,
    var updateTime: String,
    val remark:String?,
    val score: Double = 0.0,  // score & userdTime 属性的修改都放在了服务端。
    val usedTime: Int = 0
)
