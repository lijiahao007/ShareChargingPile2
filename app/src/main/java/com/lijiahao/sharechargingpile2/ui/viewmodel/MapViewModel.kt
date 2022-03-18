package com.lijiahao.sharechargingpile2.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(): ViewModel() {

    var mapCenterPos: LatLng = LatLng(0.0, 0.0) // 屏幕中心位置
    var bluePointPos: LatLng = LatLng(0.0, 0.0) // 定位蓝点位置（当前位置, 在addOnMyLocationChangeListener 中赋予值）

}