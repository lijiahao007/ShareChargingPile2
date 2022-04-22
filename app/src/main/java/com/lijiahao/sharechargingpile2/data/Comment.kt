package com.lijiahao.sharechargingpile2.data

data class Comment(
    val id:String,
    val text:String,
    val star:String,
    val like: Int,
    val createTime: String,
    val updateTime:String,
    val userId: Int,
    val stationId: Int,
    val pileId: Int
)
