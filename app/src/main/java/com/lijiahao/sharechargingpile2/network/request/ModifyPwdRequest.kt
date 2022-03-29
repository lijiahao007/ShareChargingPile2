package com.lijiahao.sharechargingpile2.network.request

data class ModifyPwdRequest(
    val userId: String,
    val oldPwd: String,
    val newPwd: String
)
