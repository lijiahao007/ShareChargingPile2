package com.lijiahao.sharechargingpile2.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lijiahao.sharechargingpile2.data.Message

@Database(entities = [Message::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class MyRoomDatabase: RoomDatabase() {
    abstract fun messageDao():MessageDao

    companion object {
        @Volatile private var instance: MyRoomDatabase? = null
        private const val DATABASE_NAME = "ChargingpileDatabase"

        private fun buildDatabase(context: Context):MyRoomDatabase {
            return Room.databaseBuilder(context, MyRoomDatabase::class.java, DATABASE_NAME)
                .build()
        }


        fun getInstance(context: Context): MyRoomDatabase {
            // 单例模式
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }
    }
}