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
    val remark:String?,
    val score: Double = 0.0  // 该属性只在服务端中根据评价修改，客户端其他操作，即时修改该属性也无效。
)
