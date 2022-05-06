package com.lijiahao.sharechargingpile2.network.service

import com.lijiahao.sharechargingpile2.network.request.ModifyExtendInfoRequest
import com.lijiahao.sharechargingpile2.network.request.ModifyPwdRequest
import com.lijiahao.sharechargingpile2.network.request.ModifyUserInfoRequest
import com.lijiahao.sharechargingpile2.network.response.UserInfoResponse
import okhttp3.MultipartBody
import retrofit2.http.*


interface UserService {

    @GET("user/userInfo")
    suspend fun getUserInfo(@Query("userId") userId: String): UserInfoResponse


    @POST("user/modifyPwd")
    // response =  "success" -> 修改成功；  "failed" -> 旧密码错误
    suspend fun modifyPwd(@Body modifyPwdRequest: ModifyPwdRequest): String


    @POST("user/modifyExtendInfo")
    suspend fun modifyExtendInfo(@Body modifyExtendInfoRequest: ModifyExtendInfoRequest): String

    @Multipart
    @POST("user/modifyUserInfo")
    suspend fun modifyUserInfo(
        @Part avatar: MultipartBody.Part, // Part名字必须是 avatar
        @Part("request") request: ModifyUserInfoRequest
    ): String

    @POST("user/modifyUserInfoWithoutPic")
    suspend fun modifyUserInfoWithoutPic(@Body request: ModifyUserInfoRequest):String

}