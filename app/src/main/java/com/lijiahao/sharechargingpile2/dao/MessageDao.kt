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

    @Query("SELECT sendId FROM message WHERE (sendId != :curUserId) UNION SELECT targetId FROM message WHERE (targetId != :curUserId) ")
    fun queryMessageAllUserIdExceptCurUser(curUserId:String):List<String>

    // 获取某个用户相关的最新消息
    @Query("SELECT * FROM message WHERE (sendId == :userId) OR (targetId == :userId) ORDER BY sendTime DESC LIMIT 1")
    fun queryLatestMessage(userId: String):Message

    // 获取一列表的用户ID对应的最新消息 （返回的Message顺序与传入UserId顺序一致）
    fun queryLatestMessage(userIds:List<String>):List<Message> {
        val res = ArrayList<Message>()
        userIds.forEach {
            val message = queryLatestMessage(it)
            res.add(message)
        }
        return res
    }

}