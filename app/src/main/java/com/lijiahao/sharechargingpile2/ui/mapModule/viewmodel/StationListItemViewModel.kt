package com.lijiahao.sharechargingpile2.ui.mapModule.viewmodel

import androidx.lifecycle.ViewModel
import com.lijiahao.sharechargingpile2.data.ChargingPile
import com.lijiahao.sharechargingpile2.data.ChargingPileStation
import com.lijiahao.sharechargingpile2.data.OpenTime
import com.lijiahao.sharechargingpile2.data.Tags
import com.lijiahao.sharechargingpile2.utils.INVISIBLE
import com.lijiahao.sharechargingpile2.utils.VISIBLE
import java.math.BigDecimal
import java.math.RoundingMode

class StationListItemViewModel(
    val station: ChargingPileStation,
    val tags: List<Tags>,
    val piles: List<ChargingPile>,
    val openTimes: List<OpenTime>,
    var distance:Float
    ) : ViewModel() {
    val stationId = station.id
    val acNum = piles.count { it.electricType == "交流" }
    val acNumStr = acNum.toString()
    val dcNum = piles.count { it.electricType == "直流" }
    val dcNumStr = dcNum.toString()
    val stationName = station.name.toString()
    val tag1Name = if (tags.isNotEmpty()) tags[0].text else ""
    val tag1Visible = if (tag1Name == "") VISIBLE else INVISIBLE
    val tag2Name = if (tags.size >= 2) tags[1].text else ""
    val tag2Visible = if (tag2Name == "") VISIBLE else INVISIBLE
    val tag3Name = if (tags.size >= 3) tags[2].text else ""
    val tag3Visible = if (tag3Name == "") VISIBLE else INVISIBLE
    val tag4Name = if (tags.size >= 4) tags[3].text else ""
    val tag4Visible = if (tag4Name == "") VISIBLE else INVISIBLE
    val electricCharge = BigDecimal((if (openTimes.isEmpty()) 0.0 else openTimes.sumOf { it.electricCharge.toDouble() } / openTimes.size)).setScale(2, RoundingMode.HALF_UP).toDouble().toString()
    val acVisible = if (acNum > 0) VISIBLE else INVISIBLE
    val dcVisible = if (dcNum > 0) VISIBLE else INVISIBLE
    val parkFeeStr = station.parkFee.toString()

    fun getDistanceStr():String {
        return BigDecimal((distance/1000).toDouble()).setScale(1, RoundingMode.HALF_UP).toString()// km单位
    }




    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StationListItemViewModel

        if (station != other.station) return false
        if (tags != other.tags) return false
        if (piles != other.piles) return false
        if (openTimes != other.openTimes) return false
        if (stationId != other.stationId) return false
        if (acNum != other.acNum) return false
        if (dcNum != other.dcNum) return false
        if (stationName != other.stationName) return false
        if (tag1Name != other.tag1Name) return false
        if (tag1Visible != other.tag1Visible) return false
        if (tag2Name != other.tag2Name) return false
        if (tag2Visible != other.tag2Visible) return false
        if (tag3Name != other.tag3Name) return false
        if (tag3Visible != other.tag3Visible) return false
        if (tag4Name != other.tag4Name) return false
        if (tag4Visible != other.tag4Visible) return false
        if (electricCharge != other.electricCharge) return false

        return true
    }

    override fun hashCode(): Int {
        var result = station.hashCode()
        result = 31 * result + tags.hashCode()
        result = 31 * result + piles.hashCode()
        result = 31 * result + openTimes.hashCode()
        result = 31 * result + stationId
        result = 31 * result + acNum.hashCode()
        result = 31 * result + dcNum.hashCode()
        result = 31 * result + stationName.hashCode()
        result = 31 * result + tag1Name.hashCode()
        result = 31 * result + tag1Visible
        result = 31 * result + tag2Name.hashCode()
        result = 31 * result + tag2Visible
        result = 31 * result + tag3Name.hashCode()
        result = 31 * result + tag3Visible
        result = 31 * result + tag4Name.hashCode()
        result = 31 * result + tag4Visible
        result = 31 * result + electricCharge.hashCode()
        return result
    }
}