package com.lijiahao.sharechargingpile2.ui.mainModule

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.databinding.ActivityMainBinding
import com.lijiahao.sharechargingpile2.ui.service.MessagePollingService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
                R.id.chatFragment -> { navView.visibility = View.GONE}
                R.id.userExtendInfoFragment -> { navView.visibility = View.GONE }
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
        val intent = Intent(this, MessagePollingService::class.java)
        startService(intent)
    }


    override fun onDestroy() {
        stopService(Intent(this, MessagePollingService::class.java))
        super.onDestroy()
    }
}