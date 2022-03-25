package com.lijiahao.sharechargingpile2.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lijiahao.sharechargingpile2.data.Message

@Dao
interface MessageDao {

    @Insert
    fun insertMessage(vararg msg:Message): List<Long>

    // 查找所有和某个人相关的信息
    @Query("SELECT * FROM message WHERE sendId==:userId OR targetId==:userId ORDER BY sendTime DESC")
    fun queryMessageByUserId(userId: Int):List<Message>

    // 限制查找num个。
    @Query("SELECT * FROM message WHERE sendId==:userId OR targetId==:userId ORDER BY sendTime DESC LIMIT :num")
    fun queryMessageByUserId(userId:Int , num:Int) : List<Message>

    // 找到在time之前最近的num个Message
    @Query("SELECT * FROM message WHERE (sendId == :userId OR targetId == :userId) AND sendTime < :time ORDER BY sendTime DESC LIMIT :num")
    fun queryMessageByUserIdAndTime(userId:Int, time:Long, num:Int) : List<Message>

    @Update
    fun updateMessage(vararg msg:Message)

}