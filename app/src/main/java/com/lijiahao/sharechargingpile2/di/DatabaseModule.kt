package com.lijiahao.sharechargingpile2.di

import android.content.Context
import com.lijiahao.sharechargingpile2.dao.MessageDao
import com.lijiahao.sharechargingpile2.dao.MyRoomDatabase
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context,
        sharedPreferenceData: SharedPreferenceData
    ):MyRoomDatabase {
        return MyRoomDatabase.getInstance(context, sharedPreferenceData.userId)
    }

    @Provides
    fun provideMessageDao(database: MyRoomDatabase) : MessageDao {
        return database.messageDao()
    }
}