package com.lijiahao.sharechargingpile2.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lijiahao.sharechargingpile2.network.request.MessageRequest
import java.io.Serializable

@Entity(tableName = "message")
data class Message(
    @PrimaryKey(autoGenerate = true)
    var id:Int,
    val uuid:String,
    val sendId: String,
    val targetId: String,
    val sendTime: Long,
    val isCheck: Boolean,
    val msgType: MsgType,
    var msgBody: MsgBody,
    var state: MsgState
): Serializable{
    fun toTextMessageRequest(): MessageRequest {
        val text = (msgBody as TextMsgBody).message
        return MessageRequest(uuid, "TEXT", sendId, targetId, text, System.currentTimeMillis())
    }
}
