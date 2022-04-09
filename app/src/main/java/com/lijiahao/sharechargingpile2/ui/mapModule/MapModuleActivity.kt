package com.lijiahao.sharechargingpile2.ui.mapModule

import android.Manifest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.databinding.ActivityMapModuleBinding
import com.lijiahao.sharechargingpile2.utils.PermissionTool
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapModuleActivity : AppCompatActivity() {

    private val binding: ActivityMapModuleBinding by lazy {
        DataBindingUtil.setContentView<ActivityMapModuleBinding>(
            this,
            R.layout.activity_map_module
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionReady = PermissionTool.usedOnCreate(this,
            permissions,
            permissionsHint
        )
        if (permissionReady) {
            init()
        } else {
            setContentView(R.layout.activity_map_module_without_permission)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val res = PermissionTool.usedOnRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            this,
            permissionsHint

        )
        if (res) {
            init()
        }
    }

    private fun init() {
        binding // 加载一下
    }



    companion object {
        const val TAG = "MAPACTIVITY"
        const val REQUEST_CODE = 1
        private val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
        )

        @RequiresApi(Build.VERSION_CODES.P)
        private val permissionsR = arrayOf(
            Manifest.permission.FOREGROUND_SERVICE,
        )

        @RequiresApi(Build.VERSION_CODES.Q)
        private val permissionsQ = arrayOf(
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )

        private val permissionsHint = arrayOf(
            "网络权限",
            "获取粗糙位置权限",
            "读取手机状态权限",
            "获取网络状态权限",
            "获取WIFI状态权限",
            "更改WIFI状态",
            "写拓展存储控件权限",
            "读拓展存储权限",
            "读取精确位置",
            "获取位置附加信息"
        )
    }
}