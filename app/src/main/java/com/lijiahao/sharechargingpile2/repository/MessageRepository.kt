package com.lijiahao.sharechargingpile2.repository

import com.lijiahao.sharechargingpile2.dao.MessageDao
import com.lijiahao.sharechargingpile2.data.Message
import com.lijiahao.sharechargingpile2.data.MsgState
import com.lijiahao.sharechargingpile2.network.service.MessageService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    val messageDao: MessageDao,
    val messageService: MessageService
    ) {

    suspend fun sendAndSaveTextMessage(message: Message):Boolean {
        // 先上传消息，上传成功后再写入数据库。该方法不要在主线程调用啊。
        var flag = true
        try {
            val networkRes = messageService.sendTextMessage(message.toTextMessageRequest())
        } catch (e: Exception) {
            e.printStackTrace()
            flag = false;
            message.state = MsgState.FAILED
        } finally {
            if (flag) {
                message.state = MsgState.SENT
            }
            val databaseRes = messageDao.insertMessage(message)
        }
        return flag
    }

}