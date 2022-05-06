package com.lijiahao.sharechargingpile2.network.service

import com.lijiahao.sharechargingpile2.network.request.LoginRequest
import com.lijiahao.sharechargingpile2.network.request.SignUpRequest
import com.lijiahao.sharechargingpile2.network.response.LoginResponse
import com.lijiahao.sharechargingpile2.network.response.SignUpResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {

    @POST("user/login")
    suspend fun login(@Body request: LoginRequest) : LoginResponse


    @POST("user/signup")
    suspend fun signup(@Body request: SignUpRequest): SignUpResponse

}