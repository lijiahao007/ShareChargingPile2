package com.lijiahao.sharechargingpile2.network.service

import com.lijiahao.sharechargingpile2.data.Comment
import com.lijiahao.sharechargingpile2.network.request.CommentRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CommentService {

    @POST("comment/publishComment")
    suspend fun publishComment(@Body commentRequest: CommentRequest): String

    // TODO("这些最好要分页")
    @GET("comment/queryCommentByStationId")
    suspend fun queryCommentByStationId(@Query("stationId") stationId: Int): List<Comment>

    // 返回当前like数量
    @GET("comment/likeComment")
    suspend fun likeComment(@Query("commentId") commentId:Int, @Query("userId") userId:Int): String

    // 返回当前like数量
    @GET("comment/unlikeComment")
    suspend fun unlikeComment(@Query("commentId") commentId:Int, @Query("userId") userId:Int): String
}