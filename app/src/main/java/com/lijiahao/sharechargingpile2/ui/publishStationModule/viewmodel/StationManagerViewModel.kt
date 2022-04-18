package com.lijiahao.sharechargingpile2.ui.publishStationModule.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import com.lijiahao.sharechargingpile2.network.response.StationAllInfo
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
// 该ViewModel中存放着当前用户所拥有的充电站信息
class StationManagerViewModel @Inject constructor(
    private val sharedPreferenceData: SharedPreferenceData,
    private val chargingPileStationService: ChargingPileStationService
):ViewModel() {

    val userStationInfo = MutableLiveData<StationAllInfo>()

    init {
        getData()
    }

    fun getData() {
        viewModelScope.launch(Dispatchers.IO) {
            val info = chargingPileStationService.getStationInfoByUserId(sharedPreferenceData.userId)
            preProcess(info)
            userStationInfo.postValue(info)
            Log.i("StationManagerViewModel", "当前用户充电站信息:$info")
        }
    }

    // 预处理信息，来符合应用数据格式要求
    private fun preProcess (info: StationAllInfo) {
        // 1. LocalTime 采取 00:00 这种格式
        info.electricChargePeriodMap.forEach { (_, value) ->
            value.forEach { electricChargePeriod ->
                electricChargePeriod.beginTime = electricChargePeriod.beginTime.substring(0, 5)
                electricChargePeriod.endTime = electricChargePeriod.endTime.substring(0, 5)
            }
        }
    }


}