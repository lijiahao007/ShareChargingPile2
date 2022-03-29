package com.lijiahao.sharechargingpile2.network.response

data class UserInfoResponse(
    val userId: String,
    val phone: String,
    var name: String,
    var avatarUrl: String,
    var extend: Map<String, String>,
    var remark:String
)
