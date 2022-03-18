package com.lijiahao.sharechargingpile2.network

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ChargingPileStationService {

    @FormUrlEncoded
    @POST("chargingPileStation/test")
    suspend fun insertTestChargingPile(
        @Field("longitude") longitude:Double,
        @Field("latitude") latitude:Double,
        @Field("posDescription") posDescription:String
    ) : String

}