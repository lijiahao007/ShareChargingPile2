package com.lijiahao.sharechargingpile2.repository

import android.content.Context
import android.util.Log
import com.lijiahao.sharechargingpile2.data.*
import com.lijiahao.sharechargingpile2.network.request.StationInfoRequest
import com.lijiahao.sharechargingpile2.network.response.ModifyStationResponse
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.ui.publishStationModule.AddStationFragment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.nio.file.Files
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChargingPileStationRepository @Inject constructor(
    val chargingPileStationService: ChargingPileStationService,
    @ApplicationContext val context: Context
) {

    suspend fun uploadStationPics(stationId: String, files: List<File>): String {
        val picPartList: ArrayList<MultipartBody.Part> = ArrayList()
        files.forEach {
            val part = MultipartBody.Part.createFormData(
                "stationPic",
                it.name,
                it.asRequestBody("multipart/form-data".toMediaType())
            )
            picPartList.add(part)
            Log.i(AddStationFragment.TAG, "${it.name} + $part")
        }

        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        builder.addFormDataPart("stationId", stationId)
        picPartList.forEach {
            builder.addPart(it)
        }
        return chargingPileStationService.uploadStationPics(builder.build())
    }

    suspend fun uploadStationInfo(
        stationInfo: StationInfoRequest, // 充电站信息
        files: List<File> // 充电站图片
    ): String {
        val picPartList: ArrayList<MultipartBody.Part> = ArrayList()
        files.forEach {
            val part = MultipartBody.Part.createFormData(
                "stationPic",
                it.name,
                it.asRequestBody("multipart/form-data".toMediaType())
            )
            picPartList.add(part)
            Log.i(AddStationFragment.TAG, "${it.name} + $part")
        }
        var file:File? = null
        if (files.isEmpty()) {
            file = File.createTempFile("temp", ".temp", context.filesDir)
            val emptyPart = MultipartBody.Part.createFormData(
                "stationPic",
                file.name,
                file.asRequestBody("multipart/form-data".toMediaType())
            )
            picPartList.add(emptyPart)
        }
        val res = chargingPileStationService.uploadStationAllInfo(stationInfo, picPartList)
        file?.delete()
        return res
    }


    suspend fun modifyStationInfo(
        stationInfo: StationInfoRequest,
        remotePicUris: ArrayList<String>, // 远程已存在图片(可以是空列表)
        files: List<File>  // 新添加图片(可以是空列表)
    ): ModifyStationResponse {
        val picPartList: ArrayList<MultipartBody.Part> = ArrayList()
        files.forEach {
            val part = MultipartBody.Part.createFormData(
                "newPics",
                it.name,
                it.asRequestBody("multipart/form-data".toMediaType())
            )
            picPartList.add(part)
        }

        if (remotePicUris.isEmpty()) {
            remotePicUris.add("")
        }
        var file: File? = null
        if (files.isEmpty()) {
            val file = File.createTempFile("temp", ".temp", context.filesDir)
            val length = file.length()
            val emptyPart = MultipartBody.Part.createFormData(
                "newPics",
                file.name,
                file.asRequestBody("multipart/form-data".toMediaType())
            )
            picPartList.add(emptyPart)
        }

        val res = chargingPileStationService.modifyStationInfo(stationInfo, remotePicUris, picPartList)
        file?.delete()
        return res
    }


    suspend fun getStation(): StationAllInfo {
        return withContext(Dispatchers.IO) {
            chargingPileStationService.getStationAllInfo()
        }
    }


    suspend fun getStation(userId: String): StationAllInfo {
        return withContext(Dispatchers.IO) {
            chargingPileStationService.getStationInfoByUserId(userId)
        }
    }

}