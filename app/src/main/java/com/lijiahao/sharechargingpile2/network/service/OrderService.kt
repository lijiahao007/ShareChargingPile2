package com.lijiahao.sharechargingpile2.network.service

import com.lijiahao.sharechargingpile2.network.response.GenerateOrderResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OrderService {

    @GET("order/generateOrder")
    suspend fun generateOrder(@Query("pileId") pileId:String, @Query("userId") userId:String):GenerateOrderResponse


    @GET("order/finishOrder")
    suspend fun finishOrder(@Query("orderId") orderId:String) :GenerateOrderResponse

    @GET("order/payOrder")
    suspend fun payOrder(@Query("orderId") orderId:String):String
}