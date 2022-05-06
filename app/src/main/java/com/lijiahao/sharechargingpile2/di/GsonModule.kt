package com.lijiahao.sharechargingpile2.di

import com.google.gson.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.lang.reflect.Type
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Module
@InstallIn(SingletonComponent::class)
class GsonModule {

    @Provides
    fun provideGson():Gson {
        return GsonBuilder()
            .registerTypeAdapter(LocalTime::class.java, object:JsonSerializer<LocalTime> {
                override fun serialize(
                    src: LocalTime?,
                    typeOfSrc: Type?,
                    context: JsonSerializationContext?
                ): JsonElement {
                    return JsonPrimitive(src?.format(DateTimeFormatter.ofPattern("HH:mm")))
                }
            })
            .registerTypeAdapter(LocalTime::class.java, object:JsonDeserializer<LocalTime> {
                override fun deserialize(
                    json: JsonElement?,
                    typeOfT: Type?,
                    context: JsonDeserializationContext?
                ): LocalTime {
                    return LocalTime.parse(json?.asString, DateTimeFormatter.ofPattern("HH:mm"))
                }
            })
            .create()
    }
}