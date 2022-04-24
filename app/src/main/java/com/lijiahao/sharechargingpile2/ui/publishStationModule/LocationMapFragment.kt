package com.lijiahao.sharechargingpile2.ui.publishStationModule

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethod
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps.*
import com.amap.api.maps.model.*
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.ServiceSettings
import com.amap.api.services.geocoder.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.databinding.FragmentLocationMapBinding
import com.lijiahao.sharechargingpile2.ui.mapModule.observer.MapViewObserver
import com.lijiahao.sharechargingpile2.ui.publishStationModule.adapter.AddressListAdapter
import com.lijiahao.sharechargingpile2.ui.publishStationModule.viewmodel.AddStationViewModel
import com.lijiahao.sharechargingpile2.utils.SoftKeyBoardUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LocationMapFragment : Fragment() {

    private val binding: FragmentLocationMapBinding by lazy {
        FragmentLocationMapBinding.inflate(layoutInflater)
    }
    private val bottomSheetBehavior: BottomSheetBehavior<View> by lazy {
        BottomSheetBehavior.from(
            binding.bottomSheetRecyclerView
        )
    }
    private val viewModel: AddStationViewModel by activityViewModels()

    private lateinit var mapView: MapView
    lateinit var aMap: AMap
    private lateinit var uiSettings: UiSettings // 地图控件控制器
    private var mapCenterMarker: Marker? = null // 屏幕中心标点
    private var curPos: LatLng = LatLng(0.0, 0.0)
    private var centerPos: LatLng = LatLng(0.0, 0.0)
    private lateinit var geocodeSearch: GeocodeSearch // 地址 <-> 经纬度 转换工具
    private lateinit var adapter: AddressListAdapter
    private var firstFlag = true
    private lateinit var mapViewObserver: MapViewObserver

    // AMap 隐私相关授权
    private fun privacyInit() {
        // 基本的初始化操作，需要在所有AMap库相关操作之前调用
        // 1. 更新隐私合规状态。
        // 2. 更新同意隐私状态
        MapsInitializer.updatePrivacyShow(context, true, true)
        MapsInitializer.updatePrivacyAgree(context, true)
        AMapLocationClient.updatePrivacyShow(context, true, true)
        AMapLocationClient.updatePrivacyAgree(context, true)
        ServiceSettings.updatePrivacyShow(context, true, true)
        ServiceSettings.updatePrivacyAgree(context, true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        privacyInit()
        mapView = binding.mapViewForPublishStation
        mapView.onCreate(savedInstanceState)
        firstFlag = true
        mapViewObserver = MapViewObserver(mapView)
        viewLifecycleOwner.lifecycle.addObserver(mapViewObserver)
        viewModel // 加载一下viewModel
        initUI()
        return binding.root
    }


    private fun initUI() {
        initMap()
        initIfChange()

        adapter = AddressListAdapter(curPos, aMap, bottomSheetBehavior)
        binding.bottomSheetRecyclerView.adapter = adapter
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        binding.btnLocation.setOnClickListener {
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curPos, 12f))
        }

        binding.btnChoosePosition.setOnClickListener {
            // 将信息存入viewModel中，并返回AddStationFragment
            viewModel.latitude = centerPos.latitude
            viewModel.longitude = centerPos.longitude

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val regeocodeAddress = geocodeSearch.getFromLocation(
                        RegeocodeQuery(
                            LatLonPoint(
                                centerPos.latitude,
                                centerPos.longitude
                            ), 10f, GeocodeSearch.AMAP
                        )
                    )
                    viewModel.posDescription = regeocodeAddress.formatAddress
                    setFragmentResult(
                        AddStationFragment.LOCATION_MAP_TO_ADD_STATION_BUNDLE,
                        bundleOf("isDescription" to true)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.i(
                        "btnChoosePosition",
                        "找不到${centerPos.latitude}, ${centerPos.longitude}的地址描述"
                    )
                    setFragmentResult(
                        AddStationFragment.LOCATION_MAP_TO_ADD_STATION_BUNDLE,
                        bundleOf("isDescription" to false)
                    )
                } finally {
                    withContext(Dispatchers.Main) {
                        navigationUp()
                    }
                }
            }

        }

        // 搜索地方逻辑
        // 1. 在文本框输入地名，并点击末尾图标时，弹出BottomSheet，并显示搜索结果
        // 2. 当点击后，将Camera移动到目标位置，并且折叠BottomSheet。（这部分在Adapter中完成）
        binding.outlinedTextField.setEndIconOnClickListener {
            hideKeyBoard()
            val address = binding.outlinedTextField.editText?.text.toString()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            Log.i("LocationMapFragment", "address:$address")
            geocodeSearch.getFromLocationNameAsyn(GeocodeQuery(address, "shantou"))
        }

        geocodeSearch.setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {
            override fun onRegeocodeSearched(p0: RegeocodeResult?, p1: Int) {
            }

            override fun onGeocodeSearched(result: GeocodeResult?, code: Int) {
                if (code == 1000) {
                    val list = result?.geocodeAddressList
                    Log.i("GeocodeSearch", "list: $list")
                    list?.forEach {
                        Log.i("GeocodeSearch", "${it.formatAddress}")
                    }

                    if (list!!.size > 1) {
                        adapter.submitList(list)
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                    } else {
                        // 跳转到目标位置
                        val address = list[0]
                        aMap.animateCamera(
                            CameraUpdateFactory.newLatLng(
                                LatLng(
                                    address.latLonPoint.latitude,
                                    address.latLonPoint.longitude
                                )
                            )
                        )
                    }
                } else {
                    Snackbar.make(binding.outlinedTextField, "未找到地点", Snackbar.LENGTH_SHORT).show()
                }
            }

        })


    }

    private fun initIfChange() {
        // 如果当前AddStationFragment是修改状态，那么就将当前屏幕中心定位顶到目标中。
        setFragmentResultListener(ModifyStationFragment.CHANGE_STATION) { _, bundle ->
            aMap.animateCamera(
                CameraUpdateFactory.newLatLng(
                    LatLng(
                        viewModel.latitude,
                        viewModel.longitude
                    )
                )
            )
        }
    }

    private fun initMap() {
        aMap = mapView.map
        uiSettings = aMap.uiSettings
        uiSettings.isZoomControlsEnabled = false
        val locationStyle = MyLocationStyle().apply {
            interval(2000) // 2秒获取一次位置
            myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER) //连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
        }
        aMap.myLocationStyle = locationStyle
        aMap.isMyLocationEnabled = true
        setAMapListener()
        geocodeSearch = GeocodeSearch(context)
    }

    private fun setAMapListener() {
        // 1. 通过监听器回调，获取定位蓝点信息
        aMap.addOnMyLocationChangeListener { location ->
            // 获取当前定位蓝点位置信息
            curPos = LatLng(location.latitude, location.longitude)
            adapter.curPos = curPos
            if (firstFlag) {
                aMap.animateCamera(CameraUpdateFactory.newLatLng(curPos))
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
            }
        })
    }

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
        centerPos = latLng
    }

    private fun hideKeyBoard() {
        SoftKeyBoardUtils.hideKeyBoard(requireActivity())
    }


    private fun navigationUp() {
        findNavController().navigateUp()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
        viewLifecycleOwner.lifecycle.removeObserver(mapViewObserver)
    }
}