package com.lijiahao.sharechargingpile2.network.service

import com.lijiahao.sharechargingpile2.network.request.MessageRequest
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MessageService {

    @POST("message/sendTextMessage")
    suspend fun sendTextMessage(@Body request:MessageRequest): String

    @Multipart
    @POST("message/sendImageMessage")
    suspend fun sendImageMessage(@Part pic:MultipartBody.Part,
                                 @Part("messageRequest") messageRequest: MessageRequest):String


}