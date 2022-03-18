package com.lijiahao.sharechargingpile2.data.response

import com.google.gson.annotations.SerializedName

data class Login1Result(
    @field:SerializedName("id") val id: Int,
    @field:SerializedName("name") val name: String,
    @field:SerializedName("phone") val phone: String
)
