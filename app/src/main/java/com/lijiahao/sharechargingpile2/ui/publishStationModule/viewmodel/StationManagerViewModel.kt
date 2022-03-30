package com.lijiahao.sharechargingpile2.ui.publishStationModule.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import com.lijiahao.sharechargingpile2.data.StationAllInfo
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
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
            userStationInfo.postValue(info)
        }
    }
}