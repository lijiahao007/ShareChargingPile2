package com.lijiahao.sharechargingpile2.data

import com.lijiahao.sharechargingpile2.utils.TimeUtils.Companion.isBetween
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class OpenTime(
    val id: Int,
    var beginTime: String,
    var endTime: String,
    val stationId: Int
) {
    // 根据ElectricChargePeriod将当前OpenTime划分为若干个时间段
    fun toElectricChargePeriods(electricChargePeriods: List<ElectricChargePeriod>): List<ElectricChargePeriod> {
        val begin = LocalTime.parse(beginTime)
        val end = LocalTime.parse(endTime)
        var beginIndex = -1
        var endIndex = -1

        electricChargePeriods.forEachIndexed { index, electricChargePeriod ->
            val curBegin = LocalTime.parse(electricChargePeriod.beginTime)
            val curEnd = LocalTime.parse(electricChargePeriod.endTime)
            if (begin.isBetween(curBegin, curEnd)) {
                beginIndex = index
            }
            if (end.isBetween(curBegin, curEnd)) {
                endIndex = index
            }
        }

        val res = ArrayList<ElectricChargePeriod>()
        if (beginIndex == -1 || endIndex == -1) return res
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        if (beginIndex == endIndex) {
            res.add(
                ElectricChargePeriod(
                    0,
                    begin.format(timeFormatter),
                    end.format(timeFormatter),
                    stationId,
                    electricChargePeriods[beginIndex].electricCharge
                )
            )
            return res
        } else {
            // 1. 开始时间段
            res.add(
                ElectricChargePeriod(
                    0,
                    begin.format(timeFormatter),
                    electricChargePeriods[beginIndex].endTime,
                    stationId,
                    electricChargePeriods[beginIndex].electricCharge
                )
            )

            // 3. 中间时间段
            for (i in beginIndex + 1 until endIndex) {
                res.add(
                    ElectricChargePeriod(
                        0,
                        electricChargePeriods[i].beginTime,
                        electricChargePeriods[i].endTime,
                        stationId,
                        electricChargePeriods[i].electricCharge
                    )
                )
            }


            // 3. 最后时间段
            res.add(
                ElectricChargePeriod(
                    0,
                    electricChargePeriods[endIndex].beginTime,
                    end.format(timeFormatter),
                    stationId,
                    electricChargePeriods[endIndex].electricCharge
                )
            )
        }
        return res
    }
}
