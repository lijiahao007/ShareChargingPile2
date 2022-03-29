package com.lijiahao.sharechargingpile2.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.lijiahao.sharechargingpile2.network.interceptor.TokenHeaderInterceptor
import dagger.hilt.EntryPoint
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.InputStream
import javax.inject.Inject

@GlideModule
class MyAppGlideModule : AppGlideModule() {

    // 为Glide的OkhttpClient添加token拦截器
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val okHttpClient = OkHttpClient.Builder()
                            .addInterceptor(logger)
                            .addNetworkInterceptor(TokenHeaderInterceptor(context))
                            .build()
        val factory = OkHttpUrlLoader.Factory(okHttpClient)
        glide.registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
    }
}