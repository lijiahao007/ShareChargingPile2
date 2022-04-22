package com.lijiahao.sharechargingpile2.network.service

import com.lijiahao.sharechargingpile2.data.*
import com.lijiahao.sharechargingpile2.network.request.CommentRequest
import com.lijiahao.sharechargingpile2.network.request.StationInfoRequest
import com.lijiahao.sharechargingpile2.network.response.ModifyStationResponse
import com.lijiahao.sharechargingpile2.network.response.StationAllInfo
import com.lijiahao.sharechargingpile2.network.response.StationInfo
import okhttp3.MultipartBody
import retrofit2.http.*

interface ChargingPileStationService {

    @FormUrlEncoded
    @POST("chargingPileStation/test")
    suspend fun insertTestChargingPile(
        @Field("longitude") longitude: Double,
        @Field("latitude") latitude: Double,
        @Field("posDescription") posDescription: String
    ): String


    // 获取充电桩基本信息
    @GET("chargingPileStation/getRangeStation")
    suspend fun getRangeStation(
        @Query("latitude_northeast") latitude_northeast: Double,
        @Query("longitude_northeast") longitude_northeast: Double,
        @Query("latitude_southwest") latitude_southwest: Double,
        @Query("longitude_southwest") longitude_southwest: Double,
    ): List<ChargingPileStation>

    @GET("chargingPileStation/getStation")
    suspend fun getStations(): List<ChargingPileStation>

    @GET("chargingPileStation/getStationTags")
    suspend fun getStationTags(): Map<String, List<Tags>>

    @GET("chargingPileStation/getStationPiles")
    suspend fun getStationPiles(): Map<String, List<ChargingPile>>

    @GET("chargingPileStation/getStationOpenTime")
    suspend fun getStationOpenTime(): Map<String, List<OpenTime>>

    @GET("chargingPileStation/getStationOpenDay")
    suspend fun getStationOpenDay(): Map<String, List<OpenDayInWeek>>

    @GET("chargingPileStation/getStationElectricCharge")
    suspend fun getStationElectricCharge(): Map<String, List<ElectricChargePeriod>>

    @GET("chargingPileStation/getStationInfo")
    suspend fun getStationAllInfo(): StationAllInfo

    @GET("chargingPileStation/getStationInfoByUserId")
    suspend fun getStationInfoByUserId(@Query("userId") userId: String): StationAllInfo

    @GET("chargingPileStation/getStationInfoByStationId")
    suspend fun getStationInfoByStationId(@Query("stationId") stationId: String): StationInfo

    @GET("chargingPileStation/getStationPicUrl")
    suspend fun getStationPicUrl(@Query("stationId") stationId: Int): List<String>

    @GET("chargingPileStation/addStationCollection")
    suspend fun addStationCollection(@Query("stationId") stationId: Int): String

    @GET("chargingPileStation/subtractStationCollection")
    suspend fun subtractStationCollection(@Query("stationId") stationId: Int): String

    @POST("chargingPileStation/uploadStationInfo")
    // 单独上传Station信息
    suspend fun uploadStationInfo(@Body stationInfo: StationInfoRequest): String

    @POST("chargingPileStation/uploadStationPic")
    // 单独上传Station图片
    suspend fun uploadStationPics(@Body body: MultipartBody): String

    @Multipart
    @POST("chargingPileStation/uploadRemainStationIds")
    // 上传剩余stationIds, 让服务器删除剩余stationIds
    suspend fun uploadRemainStationIds(
        @Part("stationIds") stationIds: List<Int>,
        @Part("userId") userId: Int
    ): String


    @Multipart
    @POST("chargingPileStation/uploadStationAllInfo")
    // 将图片连同信息一起上传
    suspend fun uploadStationAllInfo(
        @Part("stationInfo") stationInfo: StationInfoRequest,
        @Part stationPics: List<MultipartBody.Part>
    ): String

    @Multipart
    @POST("chargingPileStation/modifyStationInfo")
    suspend fun modifyStationInfo(
        @Part("stationInfo") stationInfo: StationInfoRequest,
        @Part("remotePicsUris") remotePicUris: List<String>,
        @Part newPics: List<MultipartBody.Part>  // 注意MultipartBody.Part 的Part在这里不需要标注value，具体的变量名是在MultipartBody构建的时候写入的
    ): ModifyStationResponse

}