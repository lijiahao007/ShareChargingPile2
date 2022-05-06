package com.lijiahao.sharechargingpile2

import com.google.gson.*
import com.lijiahao.sharechargingpile2.data.TimeBarData
import org.junit.Test
import java.lang.reflect.Type
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class GsonTest {

    val gson = GsonBuilder()
    .registerTypeAdapter(LocalTime::class.java, object: JsonSerializer<LocalTime> {
        override fun serialize(
            src: LocalTime?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return JsonPrimitive(src?.format(DateTimeFormatter.ofPattern("HH:mm")))
        }
    })
    .registerTypeAdapter(LocalTime::class.java, object: JsonDeserializer<LocalTime> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): LocalTime {
            return LocalTime.parse(json?.asString, DateTimeFormatter.ofPattern("HH:mm"))
        }
    })
    .create()


    @Test
    fun test() {
        val begin = LocalTime.of(0,0,0)
        val end = LocalTime.of(23, 59, 59)
        val timeBarData = TimeBarData(begin, end, TimeBarData.STATE_FREE)
        val json = gson.toJson(timeBarData)
        println("json = $json")
        println("begin:${begin.format(DateTimeFormatter.ofPattern("HH:mm"))}")
        println("begin:${end.format(DateTimeFormatter.ofPattern("HH:mm"))}")

    }

}