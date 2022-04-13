package com.lijiahao.sharechargingpile2.data

data class Order(
    val id: String,
    val createTime: String,
    val completeTime:String,
    val updateTime:String,
    val beginChargeTime:String,
    val state:String,
    val price:Float,
    val pileId:String,
    val userId:String,
    val uuid:String
)
