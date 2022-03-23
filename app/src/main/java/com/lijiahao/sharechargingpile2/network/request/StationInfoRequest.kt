package com.lijiahao.sharechargingpile2.network.request

import com.lijiahao.sharechargingpile2.data.ChargingPile
import com.lijiahao.sharechargingpile2.data.ChargingPileStation
import okhttp3.MultipartBody

data class StationInfoRequest(
    val openDayInWeek: ArrayList<String>,
    val openTime: ArrayList<String>,
    val openTimeCharge: ArrayList<Float>,
    val station: ChargingPileStation,
    val chargingPiles: List<ChargingPile>,
    val userId: String,
)