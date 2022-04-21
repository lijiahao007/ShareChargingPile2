package com.lijiahao.sharechargingpile2.network.response

import com.lijiahao.sharechargingpile2.data.ChargingPileStation
import com.lijiahao.sharechargingpile2.data.Order

data class QueryOrderResponse(
    val processingOrder: List<Order>,
    val finishOrder: List<Order>,
    val serviceOrder: Map<String, Map<String, List<Order>>>,
    val stationInfoMap:  Map<String, ChargingPileStation>,
    val pileStationMap: Map<String, String>
)
