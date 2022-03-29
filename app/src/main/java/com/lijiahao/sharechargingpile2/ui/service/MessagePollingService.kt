package com.lijiahao.sharechargingpile2.ui.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class MessagePollingService : Service() {

    private val mBinder: MessageBinder = MessageBinder()

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(serviceJob)


    override fun onCreate() {
        super.onCreate()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        serviceScope.launch(Dispatchers.IO) {

        }
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }


    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    class MessageBinder : Binder() {
        // 这里用于与Activity通讯，暂时不需要
    }

    companion object {
        const val TAG = "MessagePollingService"
    }
}