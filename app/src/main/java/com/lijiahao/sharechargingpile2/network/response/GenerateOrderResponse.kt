package com.lijiahao.sharechargingpile2.network.response

import com.lijiahao.sharechargingpile2.data.Order

data class GenerateOrderResponse(
    val code:String,
    val order:Order
)
