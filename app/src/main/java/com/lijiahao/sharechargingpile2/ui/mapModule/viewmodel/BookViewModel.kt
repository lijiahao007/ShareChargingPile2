package com.lijiahao.sharechargingpile2.ui.mapModule.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.lijiahao.sharechargingpile2.data.Appointment
import com.lijiahao.sharechargingpile2.network.response.StationInfo
import com.lijiahao.sharechargingpile2.network.service.AppointmentService
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val chargingPileStationService: ChargingPileStationService,
    private val appointmentService: AppointmentService
) : ViewModel() {
    private val stationId = savedStateHandle.get<Int>("stationId")

    val appointments: ArrayList<Appointment> = ArrayList()
    val stationInfo: MutableLiveData<StationInfo> = MutableLiveData()

    init {
        getData()
    }

    fun getData() {
        stationId?.let { stationId ->
            viewModelScope.launch (Dispatchers.IO) {
                try {
                    // 1. 获取未完成的预约信息
                    val curAppointments = appointmentService.getAppointmentByStationId(stationId)
                    appointments.addAll(curAppointments)

                    // 2. 获取充电站点信息
                    val stationInfo = chargingPileStationService.getStationInfoByStationId(stationId.toString())
                    this@BookViewModel.stationInfo.postValue(stationInfo)

                } catch (e: Exception) {
                    Log.e(TAG, "网络出错")
                }
            }
        }
    }

    companion object {
        const val TAG = "BookViewModel"
    }

}