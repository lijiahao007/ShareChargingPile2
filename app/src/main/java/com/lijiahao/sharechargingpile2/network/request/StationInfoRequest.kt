package com.lijiahao.sharechargingpile2.network.request

import com.lijiahao.sharechargingpile2.data.ChargingPile
import com.lijiahao.sharechargingpile2.data.ChargingPileStation
import com.lijiahao.sharechargingpile2.data.ElectricChargePeriod

data class StationInfoRequest(
    val openDayInWeek: ArrayList<String>,
    val openTime: ArrayList<String>,
    val station: ChargingPileStation,
    val chargingPiles: List<ChargingPile>,
    val electricChargePeriods: List<ElectricChargePeriod>,
    val userId: String,
)