package com.lijiahao.sharechargingpile2.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.lijiahao.sharechargingpile2.di.annotation.HttpConnect
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.io.InputStream

@GlideModule
class MyAppGlideModule : AppGlideModule() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface MyAppGlideModuleEntryPoint {
        @HttpConnect
        fun okHttpClient():OkHttpClient
    }


    // 为Glide的OkhttpClient添加token拦截器
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val okHttpClient = EntryPointAccessors.fromApplication(context.applicationContext, MyAppGlideModuleEntryPoint::class.java).okHttpClient()
        val factory = OkHttpUrlLoader.Factory(okHttpClient)
        glide.registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
    }
}