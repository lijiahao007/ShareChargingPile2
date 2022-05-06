package com.lijiahao.sharechargingpile2.network.response

import com.lijiahao.sharechargingpile2.data.*

data class StationAllInfo(
    // 该类时获取符合条件的所有充电站数据
    val stations:List<ChargingPileStation>,
    val tagMap: Map<String, List<Tags>>,
    val pileMap: Map<String, List<ChargingPile>>,
    val openTimeMap: Map<String, List<OpenTime>>,
    val openDayMap: Map<String, List<OpenDayInWeek>>,
    val picMap: Map<String, List<String>>,
    val electricChargePeriodMap: Map<String, List<ElectricChargePeriod>>
) {

    // 删除不在stationIds中的信息
    fun deleteStationNotIn(stationIds: List<Int>):StationAllInfo {
        val deleteKeySet = stations.filterNot {
            stationIds.contains(it.id)
        }.map {
            it.id.toString()
        }

        val newStationList = stations.filter { stationIds.contains(it.id) }
        val newTagMap = HashMap(tagMap)
        val newPileMap = HashMap(pileMap)
        val newOpenTimeMap = HashMap(openTimeMap)
        val newOpenDayMap = HashMap(openDayMap)
        val newPicMap = HashMap(picMap)
        val newElectricChargePeriodMap = HashMap(electricChargePeriodMap)

        deleteKeySet.forEach { key ->
            newTagMap.remove(key)
            newPileMap.remove(key)
            newOpenDayMap.remove(key)
            newOpenTimeMap.remove(key)
            newPicMap.remove(key)
            newElectricChargePeriodMap.remove(key)
        }
        return StationAllInfo(newStationList, newTagMap, newPileMap, newOpenTimeMap, newOpenDayMap, newPicMap, newElectricChargePeriodMap)
    }

}
