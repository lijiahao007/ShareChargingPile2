package com.lijiahao.sharechargingpile2.network.service

import com.lijiahao.sharechargingpile2.data.Appointment
import com.lijiahao.sharechargingpile2.network.request.AddAppointmentRequest
import com.lijiahao.sharechargingpile2.network.request.ModifyAppointmentRequest
import retrofit2.http.*

interface AppointmentService {

    // 根据stationID 获取未完成的预约信息
    @GET("appointment/getAppointmentByStationId")
    suspend fun getAppointmentByStationId(@Query("stationId") stationId: Int): List<Appointment>


    @POST("appointment/addAppointment")
    suspend fun addAppointment(@Body request: AddAppointmentRequest): String

    @GET("appointment/getAppointmentByUserId")
    suspend fun getAppointmentByUserId(@Query("userId") userId: Int): List<Appointment>

    @POST("appointment/modifyAppointment")
    suspend fun modifyAppointment(@Body request: ModifyAppointmentRequest): String

    @FormUrlEncoded
    @POST("appointment/deleteAppointment")
    suspend fun deleteAppointment(@Field("stationId") stationId: Int): String

    @GET("appointment/getAllAppointment")
    suspend fun getAllAppointment(): Map<String, List<Appointment>>


}