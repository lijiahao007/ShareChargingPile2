package com.lijiahao.sharechargingpile2.network.interceptor

import android.content.Context
import android.content.Intent
import android.util.Log
import com.lijiahao.sharechargingpile2.ui.loginRegisterModule.LoginActivity
import com.lijiahao.sharechargingpile2.utils.LOGIN_OUT_OF_TIME
import com.lijiahao.sharechargingpile2.utils.SHARED_PREFERENCES_NAME
import okhttp3.Interceptor
import okhttp3.Response


class TokenHeaderInterceptor(val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val sharedPreferences =
            context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        Log.i(TAG, "token=$token")
        val response = token?.let {
            val request = chain.request().newBuilder()
                .header("token", token)
                .build()
            chain.proceed(request)
        } ?: chain.proceed(chain.request())

        Log.i(TAG, "response:$response, code: ${response.code}")
        if (response.code == 401) {
            // 登录失效，跳转登录界面
            val intent = Intent(context, LoginActivity::class.java)
            intent.putExtra(LoginActivity.NEW_INTENT_EXTRA, LoginActivity.TOKENINTERCEPTOR_TO_LOGINACTIVITY)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        return response
    }

    companion object {
        const val TAG = "TokenHeaderInterceptor"
    }
}