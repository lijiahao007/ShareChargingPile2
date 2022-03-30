package com.lijiahao.sharechargingpile2.di

import android.content.Context
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import com.lijiahao.sharechargingpile2.utils.SHARED_PREFERENCES_NAME
import com.lijiahao.sharechargingpile2.utils.TOKEN_IN_PREFERENCES
import com.lijiahao.sharechargingpile2.utils.USER_ID_IN_PREFERENCES
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class SharedPreferenceModule {

    @Provides
    fun provideSharedPreferenceData(@ApplicationContext context: Context):SharedPreferenceData {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString(USER_ID_IN_PREFERENCES, "")
        val token = sharedPreferences.getString(TOKEN_IN_PREFERENCES, "")
        return SharedPreferenceData(userId!!, token!!)
    }


}