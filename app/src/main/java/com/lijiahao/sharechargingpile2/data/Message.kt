package com.lijiahao.sharechargingpile2.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lijiahao.sharechargingpile2.network.request.TextMessageRequest

@Entity(tableName = "message")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    val uuid:String,
    val sendId: String,
    val targetId: String,
    val sendTime: Long,
    val isCheck: Boolean,
    val msgType: MsgType,
    val msgBody: MsgBody,
    var state: MsgState
) {
    fun toTextMessageRequest(): TextMessageRequest {
        val text = (msgBody as TextMsgBody).message
        return TextMessageRequest(uuid, "TEXT", sendId, targetId, text, System.currentTimeMillis())
    }
}
