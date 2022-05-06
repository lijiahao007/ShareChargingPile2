package com.lijiahao.sharechargingpile2.di

import android.content.Context
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import com.lijiahao.sharechargingpile2.di.annotation.HttpConnect
import com.lijiahao.sharechargingpile2.di.annotation.WebSocketConnect
import com.lijiahao.sharechargingpile2.network.interceptor.TokenHeaderInterceptor
import com.lijiahao.sharechargingpile2.network.service.*
import com.lijiahao.sharechargingpile2.utils.SERVER_BASE_HTTP_URL
import com.lijiahao.sharechargingpile2.utils.SERVER_BASE_WEB_SOCKET_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class) // 全局单例
class NetworkModule {


    @HttpConnect
    @Provides
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        return OkHttpClient.Builder()
            .addInterceptor(logger) // 添加日志拦截器
            .addNetworkInterceptor(TokenHeaderInterceptor(context)) // 给请求头添加token
            .build()
    }


    @WebSocketConnect
    @Provides
    fun provideWebSocketOkHttpClient(
        @ApplicationContext context: Context,
    ): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .addNetworkInterceptor(TokenHeaderInterceptor(context))
            .pingInterval(10, TimeUnit.SECONDS) // 设置一个10s的心跳包
            .build()
    }

    @Provides
    fun provideWebSocketRequest(
        sharedPreferenceData: SharedPreferenceData
    ): Request {
        val userId = sharedPreferenceData.userId
        return Request.Builder().url("$SERVER_BASE_WEB_SOCKET_URL${userId}").build()
    }


    @Provides
    fun provideRetrofit(@HttpConnect okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(SERVER_BASE_HTTP_URL)
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

    @Provides
    fun provideOrderService(retrofit2: Retrofit): OrderService {
        return retrofit2.create(OrderService::class.java)
    }

    @Provides
    fun provideCommentService(retrofit2: Retrofit): CommentService {
        return retrofit2.create(CommentService::class.java)
    }

    @Provides
    fun provideAppointmentService(retrofit2: Retrofit): AppointmentService {
        return retrofit2.create(AppointmentService::class.java)
    }

}