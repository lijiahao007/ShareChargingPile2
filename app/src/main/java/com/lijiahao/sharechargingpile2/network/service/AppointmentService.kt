package com.lijiahao.sharechargingpile2.network.service

import com.lijiahao.sharechargingpile2.data.Appointment
import com.lijiahao.sharechargingpile2.network.request.AddAppointmentRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AppointmentService {

    // 根据stationID 获取未完成的预约信息
    @GET("appointment/getAppointmentByStationId")
    suspend fun getAppointmentByStationId(@Query("stationId") stationId:Int): List<Appointment>


    @POST("appointment/addAppointment")
    suspend fun addAppointment(@Body request: AddAppointmentRequest): String

    @GET("appointment/getAppointmentByUserId")
    suspend fun getAppointmentByUserId(@Query("userId") userId: Int): List<Appointment>



}