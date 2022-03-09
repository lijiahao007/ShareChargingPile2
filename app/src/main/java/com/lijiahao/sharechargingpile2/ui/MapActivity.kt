package com.lijiahao.sharechargingpile2.ui

import android.Manifest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import com.amap.api.location.AMapLocation
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.databinding.ActivityMapBinding
import com.lijiahao.sharechargingpile2.databinding.MapActivityBottomSheetBinding
import com.lijiahao.sharechargingpile2.ui.observer.MapViewObserver
import com.lijiahao.sharechargingpile2.utils.PermissionTool
import dagger.hilt.android.AndroidEntryPoint
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption

import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.*
import com.amap.api.maps.model.*
import com.amap.api.services.core.ServiceSettings
import com.lijiahao.sharechargingpile2.ui.observer.LocationClientObserver
import com.lijiahao.sharechargingpile2.utils.AMAP_KEY


@AndroidEntryPoint
class MapActivity : AppCompatActivity(), AMapLocationListener {

    private val binding: ActivityMapBinding by lazy {
        DataBindingUtil.setContentView<ActivityMapBinding>(
            this,
            R.layout.activity_map
        )
    }
    private val bottomSheetBinding: MapActivityBottomSheetBinding by lazy { binding.bottomSheetInclude }
    private val bottomSheetBehavior: BottomSheetBehavior<View> by lazy {
        BottomSheetBehavior.from(
            bottomSheetBinding.bottomSheetLayout
        )
    }

    // 地图视图
    private val mapView: MapView by lazy { binding.mapView }

    // 对MapView生命周期进行管理
    private val mapViewObserver: MapViewObserver by lazy {
        val tmp = MapViewObserver(mapView, null)
        lifecycle.addObserver(tmp)
        tmp
    }

    // locationClient的相关配置
    private val locationOption: AMapLocationClientOption by lazy {
        AMapLocationClientOption().apply {
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            isOnceLocation = true // 单次即可
            isOnceLocationLatest = true // 取3秒内精度最高的
        }
    }

    // locationClient 负责获取定位信息
    private val locationClient: AMapLocationClient by lazy {
        // 对locationClient 的生命周期进行管理
        val tmp = AMapLocationClient(applicationContext)
        tmp.setLocationListener(this)
        lifecycle.addObserver(LocationClientObserver(tmp))
        tmp.setLocationOption(locationOption)
        tmp
    }

    private lateinit var aMap: AMap // 地图控制器
    private lateinit var uiSettings: UiSettings // 地图控件控制器


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapViewObserver.savedInstanceState = savedInstanceState
        // init binding
        WindowCompat.setDecorFitsSystemWindows(window, false) // 设置全屏显示
        // set bottomSheetBehavior
        bottomSheetBehavior.saveFlags = BottomSheetBehavior.SAVE_ALL
        initAMap() // AMap库初始化操作
        // 权限申请
        val permissionReady = PermissionTool.usedOnCreate(this, permissions, permissionsHint)
        if (permissionReady) {
            init()
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
        mapSetting()
        btnSetting()
    }

    private fun initAMap() {
        // 基本的初始化操作，需要在所有AMap库相关操作之前调用
        // 1. 更新隐私合规状态。
        // 2. 更新同意隐私状态
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)
        AMapLocationClient.updatePrivacyShow(this, true, true)
        AMapLocationClient.updatePrivacyAgree(this, true)
        ServiceSettings.updatePrivacyShow(this, true, true)
        ServiceSettings.updatePrivacyAgree(this, true)
        MapsInitializer.setApiKey(AMAP_KEY)
        AMapLocationClient.setApiKey(AMAP_KEY)
    }

    private fun mapSetting() {
        // 执行地图初始化操作
        // 1. 置AMAP_KEY
        MapsInitializer.setApiKey(AMAP_KEY)
        // 2. 初始化地图控制器对象
        aMap = mapView.map
        // 3. 地图交互控件控制类
        uiSettings = aMap.uiSettings
        // 4. 删除缩放控件
        uiSettings.isZoomControlsEnabled = false
        // 5. 显示定位蓝点
        val locationStyle = MyLocationStyle().apply {
            interval(2000)
            myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER) //连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
        }
        aMap.myLocationStyle = locationStyle
        aMap.isMyLocationEnabled = true
        onceLocation()
        aMap.addOnMyLocationChangeListener { location ->
            // 获取当前定位蓝点位置信息
            Log.i("AMapLocation", "$location")

        }

    }

    private fun btnSetting() {
        // 定位按钮
        val locationBtn = binding.btnLocation
        locationBtn.setOnClickListener {
            // 每次点击按钮，会地洞
            onceLocation()
            Log.i("AMapLocation", "FAB click")
        }
    }

    // 将地图视图调整到当前位置
    private fun onceLocation() {
        locationClient.apply {
            stopLocation() // 停止再开始，确保配置生效
            startLocation()
        }
    }


    override fun onLocationChanged(location: AMapLocation?) {
        // AMapLocationClient 的位置监听器，

        if (location != null) {
            if (location.errorCode == 0) {
                // 更新蓝点
                Log.i("AMapLocation", "${location.address} ${location.aoiName}")
                val latLng = LatLng(location.latitude, location.longitude) // 取出经纬度
                // 将视角转移到当前，并带有缩放
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f));

            } else {
                Log.e(
                    "AmapError", "location Error, ErrCode:"
                            + location.errorCode + ", errInfo:"
                            + location.errorInfo
                );
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // mapView保存状态 TODO: 把这个整合到MapViewObserver中
        mapView.onSaveInstanceState(outState)
    }


    companion object {
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
            "调用A-GPS模块",
            "前台服务",
            "获取后台定位"
        )
    }
}
