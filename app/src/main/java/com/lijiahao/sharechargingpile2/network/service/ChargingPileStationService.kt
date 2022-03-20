package com.lijiahao.sharechargingpile2.network.service

import com.lijiahao.sharechargingpile2.data.ChargingPile
import com.lijiahao.sharechargingpile2.data.ChargingPileStation
import com.lijiahao.sharechargingpile2.data.OpenTime
import com.lijiahao.sharechargingpile2.data.Tags
import retrofit2.http.*

interface ChargingPileStationService {

    @FormUrlEncoded
    @POST("chargingPileStation/test")
    suspend fun insertTestChargingPile(
        @Field("longitude") longitude:Double,
        @Field("latitude") latitude:Double,
        @Field("posDescription") posDescription:String
    ) : String


    // 获取充电桩基本信息
    @GET("chargingPileStation/getRangeStation")
    suspend fun getRangeStation(
        @Query("latitude_northeast") latitude_northeast: Double,
        @Query("longitude_northeast") longitude_northeast:Double,
        @Query("latitude_southwest") latitude_southwest:Double,
        @Query("longitude_southwest") longitude_southwest:Double,
    ) : List<ChargingPileStation>

    @GET("chargingPileStation/getStation")
    suspend fun getStations() :List<ChargingPileStation>

    @GET("chargingPileStation/getStationTags")
    suspend fun getStationTags() :Map<String, List<Tags>>

    @GET("chargingPileStation/getStationPiles")
    suspend fun getStationPiles(): Map<String, List<ChargingPile>>

    @GET("chargingPileStation/getStationOpenTime")
    suspend fun getStationOpenTime() : Map<String, List<OpenTime>>

    @GET("chargingPileStation/getStationPicUrl")
    suspend fun getStationPicUrl(@Query("stationId") stationId: Int): List<String>

    @GET("chargingPileStation/addStationCollection")
    suspend fun addStationCollection(@Query("stationId") stationId:Int) : String

    @GET("chargingPileStation/subtractStationCollection")
    suspend fun subtractStationCollection(@Query("stationId") stationId:Int) : String


}