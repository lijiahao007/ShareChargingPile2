package com.lijiahao.sharechargingpile2.dao

import android.util.Log
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.lijiahao.sharechargingpile2.data.*
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

        return when(msgBody.localMsgType){
            MsgType.TEXT -> {
                gson.toJson((msgBody as TextMsgBody))
            }
            MsgType.IMAGE -> {
                gson.toJson((msgBody as ImageMsgBody))
            }
        }
    }

    @TypeConverter
    fun stringToMsgBody(str:String):MsgBody {
        val gson = Gson()
        return if (str.contains("TEXT")) {
            gson.fromJson(str, TextMsgBody::class.java)
        } else {
            gson.fromJson(str, ImageMsgBody::class.java)
        }
    }

    @TypeConverter
    fun msgStateToString(msgState:MsgState):String {
        return when (msgState) {
            MsgState.SENDING -> "SENDING"
            MsgState.SENT -> "SENT"
            MsgState.FAILED -> "FAILED"
            MsgState.CHECKED -> "CHECKED"
            MsgState.UNCHECKED -> "UNCHECKED"
        }
    }

    @TypeConverter
    fun stringToMsgState(str:String) :MsgState {
        return when (str) {
            "SENDING" -> MsgState.SENDING
            "SENT" -> MsgState.SENT
            "FAILED" -> MsgState.FAILED
            "CHECKED" -> MsgState.CHECKED
            "UNCHECKED" -> MsgState.UNCHECKED
            else -> MsgState.FAILED
        }
    }


}