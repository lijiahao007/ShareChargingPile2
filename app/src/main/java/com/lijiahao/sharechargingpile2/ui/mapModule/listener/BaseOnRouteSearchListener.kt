package com.lijiahao.sharechargingpile2.ui.mapModule.listener

import com.amap.api.services.route.*

open class BaseOnRouteSearchListener:RouteSearch.OnRouteSearchListener {
    override fun onBusRouteSearched(p0: BusRouteResult?, p1: Int) {
    }

    override fun onDriveRouteSearched(p0: DriveRouteResult?, p1: Int) {
    }

    override fun onWalkRouteSearched(p0: WalkRouteResult?, p1: Int) {
    }

    override fun onRideRouteSearched(p0: RideRouteResult?, p1: Int) {
    }
}