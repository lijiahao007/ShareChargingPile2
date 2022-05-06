package com.lijiahao.sharechargingpile2.ui.mapModule.observer

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.amap.api.location.AMapLocationClient

class LocationClientObserver(private val client : AMapLocationClient): DefaultLifecycleObserver {
    override fun onPause(owner: LifecycleOwner) {
        client.stopLocation() //停止定位后，本地定位服务并不会被销毁
    }

    override fun onDestroy(owner: LifecycleOwner) {
        client.onDestroy() //销毁定位客户端，同时销毁本地定位服务。(client.startLocation()会重新连接客户端)
    }
}