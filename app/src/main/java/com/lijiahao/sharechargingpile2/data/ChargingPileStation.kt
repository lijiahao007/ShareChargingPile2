package com.lijiahao.sharechargingpile2.data

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

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
)
