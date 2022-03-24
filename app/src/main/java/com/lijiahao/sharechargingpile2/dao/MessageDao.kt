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
    @Query("SELECT * FROM message WHERE sendId==:userId or targetId==:userId")
    fun queryMessageByUserId(userId: String):List<Message>

    // 限制查找num个。
    @Query("SELECT * FROM message WHERE sendId==:userId or targetId==:userId LIMIT :num")
    fun queryMessageByUserId(userId:String , num:Int) : List<Message>


}