package com.lijiahao.sharechargingpile2.di

import android.content.Context
import androidx.room.PrimaryKey
import com.lijiahao.sharechargingpile2.network.interceptor.TokenHeaderInterceptor
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.network.service.LoginService
import com.lijiahao.sharechargingpile2.network.service.MessageService
import com.lijiahao.sharechargingpile2.network.service.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class) // 全局单例
class NetworkModule {


    @Provides
    fun provideOkHttpClient(@ApplicationContext context: Context):OkHttpClient {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .addNetworkInterceptor(TokenHeaderInterceptor(context))
            .build()
    }

    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://172.16.191.206:30000/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build();
    }

    @Provides
    fun provideLoginService(retrofit2: Retrofit): LoginService {
        return retrofit2.create(LoginService::class.java)
    }

    @Provides
    fun provideChargingPileStationService(retrofit2: Retrofit): ChargingPileStationService {
        return retrofit2.create(ChargingPileStationService::class.java)
    }

    @Provides
    fun provideMessageService(retrofit2: Retrofit): MessageService {
        return retrofit2.create(MessageService::class.java)
    }

    @Provides
    fun provideUserService(retrofit2: Retrofit): UserService {
        return retrofit2.create(UserService::class.java)
    }

}