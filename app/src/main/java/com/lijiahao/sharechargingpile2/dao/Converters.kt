package com.lijiahao.sharechargingpile2.dao

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.lijiahao.sharechargingpile2.data.MsgBody
import com.lijiahao.sharechargingpile2.data.MsgState
import com.lijiahao.sharechargingpile2.data.MsgType
import java.io.*
import java.util.*

class Converters {

    // MsgType <-> String
    @TypeConverter
    fun msgTypeToString(msgType: MsgType):String {
        return when(msgType) {
            MsgType.TEXT -> "TEXT"
            MsgType.IMAGE -> "IMAGE"
        }
    }

    @TypeConverter
    fun stringToMsgType(str:String):MsgType {
        return when(str) {
            "TEXT" -> MsgType.TEXT
            "IMAGE" -> MsgType.IMAGE
            else -> MsgType.TEXT
        }
    }

    @TypeConverter
    fun msgBodyToString(msgBody:MsgBody) :String {
        val gson = Gson()
        return gson.toJson(msgBody)
    }

    @TypeConverter
    fun stringToMsgBody(str:String):MsgBody {
        val gson = Gson()
        return gson.fromJson(str, MsgBody::class.java)
    }

    @TypeConverter
    fun msgStateToString(msgState:MsgState):String {
        return when (msgState) {
            MsgState.SENDING -> "SENDING"
            MsgState.SENT -> "SENT"
            MsgState.FAILED -> "FAILED"
        }
    }

    @TypeConverter
    fun stringToMsgState(str:String) :MsgState {
        return when (str) {
            "SENDING" -> MsgState.SENDING
            "SENT" -> MsgState.SENT
            "FAILED" -> MsgState.FAILED
            else -> MsgState.FAILED
        }
    }


}