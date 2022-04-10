package com.lijiahao.sharechargingpile2.network.response

import com.lijiahao.sharechargingpile2.data.ChargingPile

data class ModifyStationResponse(
    val curChargingPiles:List<ChargingPile>
)
