package com.lijiahao.sharechargingpile2.di

import android.content.Context
import android.util.Log
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import com.lijiahao.sharechargingpile2.utils.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

/**
 * 注意这个Module的调用需要在LoginActivity之后调用，否则后去的数据会不准确。 （即不要在LoginActivity中使用）
 */
@Module

@InstallIn(SingletonComponent::class)
class SharedPreferenceModule {

    @Provides
    fun provideSharedPreferenceData(@ApplicationContext context: Context):SharedPreferenceData {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString(USER_ID_IN_PREFERENCES, "")
        val token = sharedPreferences.getString(TOKEN_IN_PREFERENCES, "")
        val account = sharedPreferences.getString(USER_ACCOUNT_IN_PREFERENCES, "")
        val password = sharedPreferences.getString(USER_PASSWORD_IN_PREFERENCES, "")
        Log.i("SharedPreferenceData", "userId: $userId, token：$token, account:$account, password: $password")
        return SharedPreferenceData(userId!!, token!!, account!!, password!!)
    }


}