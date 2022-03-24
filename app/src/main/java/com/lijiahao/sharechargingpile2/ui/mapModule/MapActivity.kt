package com.lijiahao.sharechargingpile2.ui.mapModule

import android.Manifest
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.databinding.ActivityMapBinding
import com.lijiahao.sharechargingpile2.databinding.MapActivityBottomSheetBinding
import com.lijiahao.sharechargingpile2.ui.mapModule.observer.MapViewObserver
import com.lijiahao.sharechargingpile2.utils.PermissionTool
import dagger.hilt.android.AndroidEntryPoint
import com.amap.api.location.AMapLocationClient

import com.amap.api.maps.*
import com.amap.api.maps.model.*
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.ServiceSettings
import com.amap.api.services.geocoder.*
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.network.service.LoginService
import com.lijiahao.sharechargingpile2.ui.mapModule.viewmodel.MapViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class MapActivity : AppCompatActivity() {

    private val viewModel: MapViewModel by viewModels()
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
    private val mapView: MapView by lazy { binding.mapView }   // 地图视图
    private lateinit var aMap: AMap // 地图控制器
    private lateinit var uiSettings: UiSettings // 地图控件控制器
    private var mapCenterMarker: Marker? = null // 屏幕中心标点
    private lateinit var geocodeSearch: GeocodeSearch // 地址 <-> 经纬度 转换工具
    private var firstFlag = true


    // AMap 隐私相关授权
    private fun privacyInit() {
        // 基本的初始化操作，需要在所有AMap库相关操作之前调用
        // 1. 更新隐私合规状态。
        // 2. 更新同意隐私状态
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)
        AMapLocationClient.updatePrivacyShow(this, true, true)
        AMapLocationClient.updatePrivacyAgree(this, true)
        ServiceSettings.updatePrivacyShow(this, true, true)
        ServiceSettings.updatePrivacyAgree(this, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false) // 设置全屏显示
        bottomSheetBehavior.saveFlags = BottomSheetBehavior.SAVE_ALL
        privacyInit()
        mapView.onCreate(savedInstanceState)
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
        viewModel // 加载一下ViewModel。
        initMap()
        initUi()
        initGeoSearch()
        initMarker() // 插入充电桩marker
    }

    // 执行地图初始化操作
    private fun initMap() {

        // 1. 初始化地图控制器对象
        aMap = mapView.map

        // 2. 地图交互控件控制类
        uiSettings = aMap.uiSettings

        // 3. 删除缩放控件
        uiSettings.isZoomControlsEnabled = false

        // 4. 显示定位蓝点
        val locationStyle = MyLocationStyle().apply {
            interval(2000) // 2秒获取一次位置
            myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER) //连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
        }
        aMap.myLocationStyle = locationStyle
        aMap.isMyLocationEnabled = true

        // 5. 设置各种AMap的监听器，监控地图状态。
        setAMapListener()

        // 6. 使用 MapViewObserver 来管理MapView的生命周期
        val mapViewObserver = MapViewObserver(mapView)
        lifecycle.addObserver(mapViewObserver)


    }

    // 设置和AMap地图相关的Listener
    private fun setAMapListener() {

        // 1. 通过监听器回调，获取定位蓝点信息
        aMap.addOnMyLocationChangeListener { location ->
            // 获取当前定位蓝点位置信息
            viewModel.bluePointPos = LatLng(location.latitude, location.longitude)
            if (firstFlag) {
                aMap.animateCamera(CameraUpdateFactory.newLatLng(viewModel.bluePointPos))
                firstFlag = false
            }
        }

        // 2. 设置屏幕移动监视器
        aMap.addOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChange(p0: CameraPosition?) {
                if (p0 != null) {
                    setCenterMarkerPosition(p0.target)
                }
            }

            override fun onCameraChangeFinish(p0: CameraPosition?) {
                if (p0 != null) {
                    setCenterMarkerPosition(p0.target)
                }
                viewModel.projection = aMap.projection
                Log.i("Projection", "projection = ${viewModel.projection}")
                Log.i("Projection", "visibleRegion = ${viewModel.projection.visibleRegion}")
                Log.i("Projection", "latLngBounds = ${viewModel.projection.visibleRegion.latLngBounds}")
            }
        })

        // 3. AMap.OnMapLoadedListener (最好这时才使用AMap和地图相关的属性)
        aMap.setOnMapLoadedListener {
            // 7.1 设置marker
        }

        // 4. marker 点击事件
        aMap.setOnMarkerClickListener { marker ->
            if (marker.isClickable && marker.title != null) {
                val id = marker.title.toInt()
                findNavController(R.id.bottom_sheet_fragment_container_view).popBackStack(R.id.StationListFragment, false)
                navigationToDetailFragment(id)
                true
            } else {
                false
            }
        }

    }

    @Inject
    lateinit var loginService: LoginService

    @Inject
    lateinit var chargingPileStationService: ChargingPileStationService

    // 设置各个UI的事件
    private fun initUi() {
        // 定位按钮
        binding.btnLocation.setOnClickListener {
            // 每次点击按钮，会地洞
//            onceLocation()
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(viewModel.bluePointPos, 12f))
            Log.i("AMapLocation", "FAB click")
        }
        binding.btnInsert.setOnClickListener {
            mapCenterMarker?.position?.let {
                lifecycleScope.launch(Dispatchers.IO) {

                    try {
                        val lat = viewModel.mapCenterPos.latitude
                        val lon = viewModel.mapCenterPos.longitude
                        val result = withContext(Dispatchers.IO) {
                            val query =
                                RegeocodeQuery(LatLonPoint(lat, lon), 10f, GeocodeSearch.AMAP)
                            geocodeSearch.getFromLocation(query)
                        }
                        Log.i("btnInsertGeoSearch", result.formatAddress)
                        val insertRes = chargingPileStationService.insertTestChargingPile(
                            lon,
                            lat,
                            result.formatAddress
                        )
                        Log.i("btnInsertGeoSearch", insertRes)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.i("btnInsertGeoSearch", "IO错误")
                    } finally {

                    }
                }
            }

        }
    }

    // 经纬度->地址转换
    private fun initGeoSearch() {
        geocodeSearch = GeocodeSearch(this)

        // 1. 设置查询回调 (异步方法回调)
        geocodeSearch.setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {
            override fun onRegeocodeSearched(result: RegeocodeResult?, rCode: Int) {
                // 经纬度 -> 地址信息
                if (rCode == 1000) {
                    // 成功
                    val address = result?.regeocodeAddress?.formatAddress
                    Log.i("ReGeocodeSearch", "address=$address")
                } else {
                    Log.e("ReGeocodeSearch", "rCode=$rCode, result=$result")
                }
            }

            override fun onGeocodeSearched(result: GeocodeResult?, rCode: Int) {
                // 地址信息 -> 经纬度
                if (rCode == 1000) {
                    // 成功
                    val latLonPoint = result?.geocodeAddressList?.get(0)?.latLonPoint
                    Log.i(
                        "GeocodeSearch",
                        "latitude=${latLonPoint?.latitude}  longitude=${latLonPoint?.longitude}"
                    )
                } else {
                    Log.e("GeocodeSearch", "rCode=$rCode, result=$result")
                }
            }
        })

        // 2. 查询地址经纬度
        val address = "广东省汕头市金平区大学路322号靠近汕头大学-图书馆 汕头大学"
        val city = "shantou"
        val geoQuery = GeocodeQuery(address, city)
        geocodeSearch.getFromLocationNameAsyn(geoQuery) // 异步方法

        // 3. 查询经纬度对应地址
        val reGeoQuery =
            RegeocodeQuery(LatLonPoint(23.410183, 116.635294), 100f, GeocodeSearch.AMAP)
        geocodeSearch.getFromLocationAsyn(reGeoQuery)

    }

    // 初始化地图上的UI， 如圆，如标点
    private fun initMarker() {
        viewModel.isReady.observe(this) {
            Log.i("initMarker", "viewModel.stationList.size=${viewModel.stationList.size}")
            // 添加标点
            viewModel.stationList.forEach { station ->
                aMap.addMarker(
                    MarkerOptions()
                        .visible(true)
                        .draggable(false)
                        .position(LatLng(station.latitude, station.longitude))
                        .icon(
                            BitmapDescriptorFactory.fromBitmap(
                                BitmapFactory.decodeResource(
                                    resources,
                                    R.drawable.station_marker
                                )
                            )
                        )
                        .title(station.id.toString())
                ) // 注意title中的id是唯一标识
            }
        }

    }

    // 将mapCenterMarker设置到屏幕中心
    private fun setCenterMarkerPosition(latLng: LatLng) {
        if (mapCenterMarker == null) {
            mapCenterMarker = aMap.addMarker(
                MarkerOptions().position(latLng)
                    .draggable(false)
                    .visible(true)
                    .icon(
                        BitmapDescriptorFactory.fromBitmap(
                            BitmapFactory.decodeResource(
                                resources,
                                R.drawable.map_center_marker
                            )
                        )
                    )
            )
            mapCenterMarker?.isClickable = false
        } else {
            mapCenterMarker?.position = latLng
        }
        Log.i("CenterMarker", "纬度latitude:${latLng.latitude}, 经度longitude:${latLng.longitude}")
        viewModel.mapCenterPos = latLng
    }


    private fun navigationToDetailFragment(id: Int) {
        val navController = findNavController(R.id.bottom_sheet_fragment_container_view)
        val action = StationListFragmentDirections.actionStationListFragmentToStationDetailFragment(id)
        navController.navigate(action)
        // 将底部BottomSheet放上来
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // mapView保存状态 TODO: 把这个整合到MapViewObserver中
        mapView.onSaveInstanceState(outState)
    }

    companion object {
        const val TAG="MAPACTIVITY"
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
    