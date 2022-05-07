package com.lijiahao.sharechargingpile2.network.response

import com.lijiahao.sharechargingpile2.data.*

data class StationInfo(
    // 该类是某个充电站相关所有数据
    val station:ChargingPileStation,
    val tagList: List<Tags>,
    val pileList: List<ChargingPile>,
    val openTimeList: List<OpenTime>,
    val openDayList: List<OpenDayInWeek>,
    val picList: List<String>,
    val chargePeriodList: List<ElectricChargePeriod>,
    val appointmentList: List<Appointment>
)
