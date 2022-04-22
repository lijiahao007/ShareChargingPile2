package com.lijiahao.sharechargingpile2.network.request

data class CommentRequest(
    val text:String,
    val star: String,
    val userId:Int,
    val stationId: Int,
    val pileId: Int
)
