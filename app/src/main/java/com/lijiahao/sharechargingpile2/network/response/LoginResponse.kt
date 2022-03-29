package com.lijiahao.sharechargingpile2.network.response

import dagger.hilt.android.AndroidEntryPoint


data class LoginResponse(
    val userId: String,
    val code: String,
    val token: String?
)
