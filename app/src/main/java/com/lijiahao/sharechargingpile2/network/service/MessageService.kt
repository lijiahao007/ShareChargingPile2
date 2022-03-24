package com.lijiahao.sharechargingpile2.network.service

import com.lijiahao.sharechargingpile2.network.request.TextMessageRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface MessageService {

    @POST("message/sendTextMessage")
    suspend fun sendTextMessage(@Body request:TextMessageRequest): String

}