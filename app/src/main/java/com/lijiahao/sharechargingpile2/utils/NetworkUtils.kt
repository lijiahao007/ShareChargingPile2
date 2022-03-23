package com.lijiahao.sharechargingpile2.utils

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class NetworkUtils {
    companion object {
        @JvmStatic
        fun getMultipartBody(
            params: Map<String, Any>,
            fileKey: String,
            files: List<File>,
        ): MultipartBody {
            val builder = MultipartBody.Builder()
            builder.setType(MultipartBody.FORM)
            params.forEach { builder.addFormDataPart(it.key, it.value.toString()) }
            files.forEach {
                val part = MultipartBody.Part.createFormData(
                    fileKey,
                    it.name,
                    it.asRequestBody("multipart/form-data".toMediaType())
                )
                builder.addPart(part)
            }
            return builder.build()
        }
    }
}