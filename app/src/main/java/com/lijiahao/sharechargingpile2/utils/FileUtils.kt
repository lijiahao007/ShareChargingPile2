package com.lijiahao.sharechargingpile2.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class FileUtils {
    companion object {
        @JvmStatic
        suspend fun getLocalPicsFromUris(context: Context, uris:List<Uri>):List<File> {
            val fileList = ArrayList<File>()
            withContext(Dispatchers.IO) {
                uris.forEach { uri ->
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val outputFile = File.createTempFile("111", ".jpg", context.filesDir)
                    val outputStream = FileOutputStream(outputFile)
                    try {
                        val buffer = ByteArray(1024)
                        while (true) {
                            val len = inputStream!!.read(buffer, 0, 1024)
                            if (len == -1) {
                                break;
                            }
                            outputStream.write(buffer)
                        }
                        Log.i(
                            "AddStationViewModel",
                            "filename:${outputFile.name}  outputFileLen = ${outputFile.length()}"
                        )
                        fileList.add(outputFile)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.i("AddStationViewModel", "图片获取失败了")
                    } finally {
                        inputStream?.close()
                        outputStream.close()
                    }
                }
            }
            return fileList
        }
    }
}