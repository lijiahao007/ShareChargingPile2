package com.lijiahao.sharechargingpile2.ui.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.gson.Gson
import com.lijiahao.sharechargingpile2.dao.MessageDao
import com.lijiahao.sharechargingpile2.di.annotation.WebSocketConnect
import com.lijiahao.sharechargingpile2.network.request.MessageRequest
import com.lijiahao.sharechargingpile2.utils.MESSAGE_ARRIVED_BROADCAST_ACTION
import com.lijiahao.sharechargingpile2.utils.MESSAGE_BROADCAST_BUNDLE
import com.lijiahao.sharechargingpile2.utils.WEB_SOCKET_NORMAL_CLOSE_CODE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import okhttp3.*
import okio.ByteString
import javax.inject.Inject

@AndroidEntryPoint
class MessageWebSocketService : Service() {

    private var mBinder: WebSocketBinder? = null

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(serviceJob)

    @WebSocketConnect
    @Inject lateinit var webSocketClient: OkHttpClient

    @Inject lateinit var webSocketRequest: Request

    @Inject lateinit var messageDao: MessageDao

    private lateinit var webSocket: WebSocket


    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "MessageWebSocketService 开始了")
        // 开启WebSocket
        webSocket = webSocketClient.newWebSocket(webSocketRequest, webSocketListener)
        Log.i(TAG, "webSocket连接打开")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return super.onStartCommand(intent, flags, startId)
    }


    // 在这里监听webSocket连接状态，并且接收信息
    private val webSocketListener = object: WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.i(TAG, "WebSocket 连接打开 onOpen $webSocket, $response")
        }
        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.i(TAG, "onMessage with text $webSocket, $text")
            // 分辨获取消息类型
            try {
                serviceScope.launch(Dispatchers.Default) {
                    val gson = Gson()
                    val request = gson.fromJson(text, MessageRequest::class.java)
                    val message = request.toMessage()

                    // message存储在本地
                    messageDao.insertMessage(message)

                    // 发送广播
                    val intent = Intent(MESSAGE_ARRIVED_BROADCAST_ACTION)
                    intent.putExtra(MESSAGE_BROADCAST_BUNDLE, message)
                    sendBroadcast(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.i(TAG, "收到非即时通讯消息")
            }
        }
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.i(TAG, "onClosed  $webSocket, $reason")
        }
        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.i(TAG, "onClosing  $webSocket, $reason")
        }
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            t.printStackTrace()
            Log.i(TAG, "onFailure  $webSocket, $response")
            // 执行重连操作
            this@MessageWebSocketService.webSocket = webSocketClient.newWebSocket(webSocketRequest, this)
            Log.i(TAG, "websocket冲顶连接")
        }
        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.i(TAG, "onMessage with bytes $webSocket, $bytes")
        }
    }


    override fun onDestroy() {
        serviceJob.cancel()
        webSocket.close(WEB_SOCKET_NORMAL_CLOSE_CODE, "用户退出应用") // 退出服务是关闭WebSocket连接
        super.onDestroy()
    }


    override fun onBind(intent: Intent?): IBinder? {
        // 把WebSocket传递给Activity
        if (mBinder == null) {
            mBinder = WebSocketBinder(webSocket)
        }
        return mBinder
    }


    class WebSocketBinder(
        val webSocket: WebSocket
        ) : Binder() {
    }

    companion object {
        const val TAG = "MessagePollingService"
    }
}