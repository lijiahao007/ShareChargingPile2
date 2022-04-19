package com.lijiahao.sharechargingpile2.data

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lijiahao.sharechargingpile2.utils.TimeUtils
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.util.*

data class TokenInfo(
    val token:String,
    val aud:String, // 用户ID
    val exp:LocalDateTime, // 到期时间
    val iat:LocalDateTime, // 签发时间
) {
    companion object {
        fun getTokenInfoFromToken(token:String) :TokenInfo {
            val list = token.split(".")
            val payload = list[1]
            val payloadJson = getJson(payload)
            val gson = Gson()
            val jsonObject = gson.fromJson(payloadJson, JsonObject::class.java)
            val aud = jsonObject.get("aud").asString
            val exp = jsonObject.get("exp").asLong * 1000
            val iat = jsonObject.get("iat").asLong * 1000
            return TokenInfo(
                token,
                aud,
                TimeUtils.longToLocalDateTime(exp),
                TimeUtils.longToLocalDateTime(iat)
            )
        }

        private fun getJson(strEncoded: String): String {
            val decodedBytes: ByteArray = Base64.getDecoder().decode(strEncoded)
            return String(decodedBytes, Charset.defaultCharset())
        }
    }
}
