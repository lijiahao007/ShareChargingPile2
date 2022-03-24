package com.lijiahao.sharechargingpile2.di

import android.content.Context
import com.lijiahao.sharechargingpile2.dao.MessageDao
import com.lijiahao.sharechargingpile2.dao.MyRoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class) // 全局单例
class DatabaseModule {
    @Provides
    fun provideDatabase(@ApplicationContext context: Context):MyRoomDatabase {
        return MyRoomDatabase.getInstance(context)
    }

    @Provides
    fun provideMessageDao(database: MyRoomDatabase) : MessageDao {
        return database.messageDao()
    }
}