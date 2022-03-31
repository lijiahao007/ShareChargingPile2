package com.lijiahao.sharechargingpile2.di

import android.content.Context
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import com.lijiahao.sharechargingpile2.utils.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
class SharedPreferenceModule {

    @Provides
    fun provideSharedPreferenceData(@ApplicationContext context: Context):SharedPreferenceData {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString(USER_ID_IN_PREFERENCES, "")
        val token = sharedPreferences.getString(TOKEN_IN_PREFERENCES, "")
        val account = sharedPreferences.getString(USER_ACCOUNT_IN_PREFERENCES, "")
        val password = sharedPreferences.getString(USER_PASSWORD_IN_PREFERENCES, "")
        return SharedPreferenceData(userId!!, token!!, account!!, password!!)
    }


}