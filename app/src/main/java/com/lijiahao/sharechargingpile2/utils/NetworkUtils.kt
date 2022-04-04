package com.lijiahao.sharechargingpile2.utils

import android.util.Log
import com.google.gson.Gson
import com.lijiahao.sharechargingpile2.network.request.MessageRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.WebSocket
import okio.ByteString.Companion.toByteString
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


        @JvmStatic
        // 通过WebSocket发送文本消息
        fun sendTextMessage(webSocket: WebSocket, request:MessageRequest):Boolean{
            val gson = Gson()
            val text = gson.toJson(request)
            Log.i("sendTextMessage", "text=$text")
            return webSocket.send(text)
        }

        @JvmStatic
        fun sendImageMessage(webSocket: WebSocket, request:MessageRequest, file:File):Boolean {
            val fileByteArray = file.readBytes()
            val gson = Gson()
            val text = gson.toJson(request)
            Log.i("sendImageMessage", "text=$text, bytelen = ${fileByteArray.size}")

            // 字节数组头200个字节存储MessageRequest
            val size = fileByteArray.size + 200
            val byteArray = ByteArray(size)
            text.encodeToByteArray().copyInto(byteArray, 0, 0)
            fileByteArray.copyInto(byteArray, 200, 0)
            val byteString = byteArray.toByteString()
            return webSocket.send(byteString)
        }
    }
}