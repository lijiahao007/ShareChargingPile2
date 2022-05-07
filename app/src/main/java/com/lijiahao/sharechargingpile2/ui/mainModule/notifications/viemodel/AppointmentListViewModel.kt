package com.lijiahao.sharechargingpile2.ui.mainModule.notifications.viemodel

import android.util.Log
import android.util.SparseArray
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lijiahao.sharechargingpile2.data.Appointment
import com.lijiahao.sharechargingpile2.data.AppointmentInfo
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import com.lijiahao.sharechargingpile2.network.service.AppointmentService
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception
import java.time.LocalDateTime
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

@HiltViewModel
class AppointmentListViewModel @Inject constructor(
    sharedPreferenceData: SharedPreferenceData,
    private val appointmentService: AppointmentService,
    private val stationService: ChargingPileStationService,
) : ViewModel() {

    val userId = sharedPreferenceData.userId.toInt()

    val appointmentInfoList = MutableLiveData<List<AppointmentInfo>>()

    init {
        getData()
    }

    private fun getData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val appointments = appointmentService.getAppointmentByUserId(userId)
                val appointmentInfoList = ArrayList<AppointmentInfo>()
                val tasks = ArrayList<Deferred<AppointmentInfo>>()
                appointments.forEach { appointment ->
                    val stationId = appointment.stationId
                    val pileId = appointment.pileId
                    val task = async {
                        val stationInfo =
                            stationService.getStationInfoByStationId(stationId.toString())
                        val station = stationInfo.station
                        val pile = stationInfo.pileList.find { it.id == pileId }
                        val appointmentInfo = AppointmentInfo(
                            station,
                            pile,
                            appointment
                        )
                        appointmentInfo
                    }
                    tasks.add(task)
                }
                tasks.forEach {
                    appointmentInfoList.add(it.await())
                }

                val nowAppointment = appointmentInfoList.filter { it.appointment.state == Appointment.STATE_WAITING }
                val historyAppointment = appointmentInfoList.filter { it.appointment.state != Appointment.STATE_WAITING }
                val res = ArrayList(nowAppointment)
                res.addAll(historyAppointment)
                this@AppointmentListViewModel.appointmentInfoList.postValue(res)

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "网络错误")
            }

        }
    }

    companion object {
        const val TAG = "AppointmentListViewModel"
    }

}