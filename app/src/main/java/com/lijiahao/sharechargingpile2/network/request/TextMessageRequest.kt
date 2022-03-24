package com.lijiahao.sharechargingpile2.network.request

data class TextMessageRequest(
    val uuid:String,
    val type:String,
    val sendUserId: String,
    val targetUserId: String,
    val text:String,
    val timeStamp:Long
)
