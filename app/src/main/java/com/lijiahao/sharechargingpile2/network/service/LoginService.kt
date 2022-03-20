package com.lijiahao.sharechargingpile2.network.service

import com.lijiahao.sharechargingpile2.network.response.Login1Result
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface LoginService {

    @GET("$baseMapper/login2")
    suspend fun login(@Query("username") username: String, @Query("password") password: String):Login1Result

    @POST("user/login5")
    suspend fun login5(@Body user: Login1Result):Login1Result

    @Multipart
    @POST("user/login6")
    suspend fun login6(@Part body: MultipartBody.Part):String

    @Streaming // 不将ResponseBody转化为byte[]
    @GET("user/login7")
    suspend fun login7(@Query("filePath") filePath:String):ResponseBody

    @Streaming
    @GET("user/login8")
    suspend fun login8(@Query("filePath") filePath: String) : retrofit2.Response<ResponseBody>


    companion object {
        const val baseMapper = "user"
    }
}