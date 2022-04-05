package com.lijiahao.sharechargingpile2.ui.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.lijiahao.sharechargingpile2.data.Message
import com.lijiahao.sharechargingpile2.utils.MESSAGE_ARRIVED_BROADCAST_ACTION
import com.lijiahao.sharechargingpile2.utils.MESSAGE_BROADCAST_BUNDLE

open class MessageReceiver: BroadcastReceiver() {
    var message: Message? = null

    // 注意在重写的时候需要保留 super.onReceive
    override fun onReceive(context: Context?, intent: Intent?) {
        message = intent?.getSerializableExtra(MESSAGE_BROADCAST_BUNDLE) as Message?
        Log.i("MessageReceiver", "message: $message")
    }

    fun getIntentFilter():IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(MESSAGE_ARRIVED_BROADCAST_ACTION)
        return intentFilter
    }
}