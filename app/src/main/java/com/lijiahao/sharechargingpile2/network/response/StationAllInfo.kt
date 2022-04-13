package com.lijiahao.sharechargingpile2.network.response

import com.lijiahao.sharechargingpile2.data.*

data class StationAllInfo(
    // 该类时获取符合条件的所有充电站数据
    val stations:List<ChargingPileStation>,
    val tagMap: Map<String, List<Tags>>,
    val pileMap: Map<String, List<ChargingPile>>,
    val openTimeMap: Map<String, List<OpenTime>>,
    val openDayMap: Map<String, List<OpenDayInWeek>>,
    val picMap: Map<String, List<String>>
)
