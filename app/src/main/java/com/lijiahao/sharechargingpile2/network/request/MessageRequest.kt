package com.lijiahao.sharechargingpile2.network.request

import com.lijiahao.sharechargingpile2.data.*
import com.lijiahao.sharechargingpile2.utils.SERVER_BASE_HTTP_URL

data class MessageRequest(
    val uuid:String,
    val type:String,
    val sendUserId: String,
    val targetUserId: String,
    val text:String,
    val timeStamp:Long
) {

    // 从MessageRequest获取具体的Message
    fun toMessage() : Message {
        val msgType = when (type) {
            "TEXT" -> MsgType.TEXT
            "IMAGE" -> MsgType.IMAGE
            else -> MsgType.TEXT
        }

        val msgBody: MsgBody = when (msgType) {
            MsgType.TEXT -> { TextMsgBody(text, "") }
            MsgType.IMAGE -> {
                ImageMsgBody("", "$SERVER_BASE_HTTP_URL$text", "")
            }
        }

        return Message(0, uuid, sendUserId, targetUserId, timeStamp, false, msgType, msgBody, MsgState.UNCHECKED)
    }
}
