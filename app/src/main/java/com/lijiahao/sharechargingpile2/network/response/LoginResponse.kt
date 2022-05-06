package com.lijiahao.sharechargingpile2.network.response


data class LoginResponse(
    val userId: String,
    val code: String,
    val token: String?
)
