package com.lijiahao.sharechargingpile2.ui.mainModule

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.databinding.ActivityMainBinding
import com.lijiahao.sharechargingpile2.ui.service.MessageWebSocketService
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.WebSocket

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var webSocket: WebSocket? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = Color.TRANSPARENT
        initBottomNav()
        initService()
    }


    private fun initBottomNav() {
        // 报错：没有设置ActionBar
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        // 设置navView 在那些 destination 显示
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.chatFragment, R.id.userExtendInfoFragment, R.id.orderListFragment,
                R.id.userInfoFragment, R.id.pileUsingFragment, R.id.payFragment,
                R.id.orderPayFragment, R.id.orderDetailFragment, R.id.commentFragment,
                R.id.bookPileFragment, R.id.choosePileFragment-> {
                    navView.visibility = View.GONE
                }
                R.id.navigation_home -> {
                    navView.visibility = View.VISIBLE
                    navView.alpha = 0.5F
                }
                else -> {
                    navView.visibility = View.VISIBLE
                    navView.alpha = 1F
                }
            }
        }
    }

    private fun initService() {
        val intent = Intent(this, MessageWebSocketService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE) // 绑定服务
    }

    override fun onDestroy() {
        unbindService(serviceConnection) // 解绑服务
        super.onDestroy()
    }


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            webSocket = (service as MessageWebSocketService.WebSocketBinder).webSocket
            Log.i(TAG, "完成绑定 $webSocket")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i(TAG, "MainService 与 MessageWebSocketService断开连接")
            webSocket = null
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}