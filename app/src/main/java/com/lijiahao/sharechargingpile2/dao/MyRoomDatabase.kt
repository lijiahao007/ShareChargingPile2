package com.lijiahao.sharechargingpile2.dao

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lijiahao.sharechargingpile2.data.Message

@Database(entities = [Message::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class MyRoomDatabase: RoomDatabase() {
    abstract fun messageDao():MessageDao


    // TODO("针对不同的用户创建不同的数据库")
    companion object {
        @Volatile private var instance: MyRoomDatabase? = null
        private const val DATABASE_NAME = "ChargingpileDatabase"
        private fun buildDatabase(context: Context, userId:String):MyRoomDatabase {
            Log.i("MyRoomDatabase", "Database_name = ${DATABASE_NAME}_${userId}")
            return Room.databaseBuilder(context, MyRoomDatabase::class.java, "${DATABASE_NAME}_${userId}")
                .build()
        }


        fun getInstance(context: Context, userId:String): MyRoomDatabase {
            // 单例模式
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context, userId).also { instance = it }
            }
        }
    }
}