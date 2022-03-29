package com.lijiahao.sharechargingpile2.network.request

data class ModifyUserInfoRequest(
    val name: String,
    val remark: String,
    val userId: String
)
