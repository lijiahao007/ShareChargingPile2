package com.lijiahao.sharechargingpile2.ui.observer

import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.amap.api.maps.MapView

class MapViewObserver(private val mapView: MapView) :
    DefaultLifecycleObserver {
    // 会根据Activity各个生命周期，来对mapView进行一些配置操作

    // onCreate 、onSaveInstanceState 在MapActivity中

    override fun onResume(owner: LifecycleOwner) {
        mapView.onResume()
    }

    override fun onPause(owner: LifecycleOwner) {
        mapView.onPause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        mapView.onDestroy()
    }


}