package com.lijiahao.sharechargingpile2.di

import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.network.service.LoginService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class) // 全局单例
class NetworkModule {


    @Provides
    fun provideOkHttpClient():OkHttpClient {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        return OkHttpClient.Builder()
            .addInterceptor(logger)
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
}