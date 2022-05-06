package com.lijiahao.sharechargingpile2.network.request

import com.lijiahao.sharechargingpile2.data.UserExtendInfo

data class ModifyExtendInfoRequest(
    val info: List<UserExtendInfo>,
    val userId: String
)
