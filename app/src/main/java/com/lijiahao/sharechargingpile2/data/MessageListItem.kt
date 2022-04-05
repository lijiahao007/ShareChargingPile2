package com.lijiahao.sharechargingpile2.data

import com.lijiahao.sharechargingpile2.network.response.UserInfoResponse

data class MessageListItem(
    var message:Message, // 消息信息
    val user: UserInfoResponse // 用户信息
)
