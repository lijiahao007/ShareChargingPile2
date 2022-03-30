package com.lijiahao.sharechargingpile2.repository

import android.util.Log
import com.lijiahao.sharechargingpile2.data.*
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.ui.mapModule.MapActivity
import com.lijiahao.sharechargingpile2.ui.publishStationModule.AddStationFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChargingPileStationRepository @Inject constructor(
    val chargingPileStationService: ChargingPileStationService
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


    suspend fun getStation(): StationAllInfo {
        return withContext(Dispatchers.IO) {
           chargingPileStationService.getStationAllInfo()
        }
    }


    suspend fun getStation(userId:String): StationAllInfo {
        return withContext(Dispatchers.IO) {
            chargingPileStationService.getStationInfoByUserId(userId)
        }
    }

}