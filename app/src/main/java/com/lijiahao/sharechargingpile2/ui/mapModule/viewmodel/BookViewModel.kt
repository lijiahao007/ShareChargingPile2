package com.lijiahao.sharechargingpile2.ui.mapModule.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.lijiahao.sharechargingpile2.data.Appointment
import com.lijiahao.sharechargingpile2.data.OpenTime
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import com.lijiahao.sharechargingpile2.data.TimeBarData
import com.lijiahao.sharechargingpile2.network.response.StationInfo
import com.lijiahao.sharechargingpile2.network.service.AppointmentService
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.utils.TimeUtils.Companion.isBetween
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val chargingPileStationService: ChargingPileStationService,
    private val appointmentService: AppointmentService,
    private val sharedPreferenceData: SharedPreferenceData
) : ViewModel() {
    private val stationId = savedStateHandle.get<Int>("stationId")

    val appointments: ArrayList<Appointment> = ArrayList()
    var stationInfo:StationInfo?= null
    val pileDateTimeBarDataMap: MutableLiveData<HashMap<Int, HashMap<LocalDate, List<TimeBarData>>>> = MutableLiveData() // <pileId, <date, timeBarList>>

    init {
        getData()
    }

    fun getData() {
        stationId?.let { stationId ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    // 1. 获取未完成的预约信息
                    val curAppointments = appointmentService.getAppointmentByStationId(stationId)
                    appointments.clear()
                    appointments.addAll(curAppointments)

                    // 2. 获取充电站点信息
                    val stationInfo =
                        chargingPileStationService.getStationInfoByStationId(stationId.toString())
                    this@BookViewModel.stationInfo = stationInfo

                    // 3. 计算TimeBarData
                    val pileDateTimeBarDataMap: HashMap<Int, HashMap<LocalDate, List<TimeBarData>>> = HashMap() // <pileId, <date, timeBarList>>
                    val pileIds = stationInfo.pileList.map { it.id }
                    val nowDate = LocalDate.now()
                    val openTimeList = stationInfo.openTimeList.sortedWith(Comparator<OpenTime> { o1, o2 ->
                        val o1BeginTime = LocalTime.parse(o1.beginTime)
                        val o2BeginTime = LocalTime.parse(o2.beginTime)
                        if (o1BeginTime.isBefore(o2BeginTime)) {
                            -1
                        } else if (o1BeginTime.isAfter(o2BeginTime)) {
                            1
                        } else {
                            0
                        }
                    })

                    pileIds.forEach { pileId ->
                        val timeBarDataMap = HashMap<LocalDate, List<TimeBarData>>()
                        val pileAppointment = appointments.filter { it.pileId == pileId }
                        // 3.1 计算每天的TimeBarData
                        for (i in 0 until 7) {
                            val date = nowDate.plusDays(i.toLong())
                            // 3.2 获取预约时间段
                            val midAppointList = ArrayList<TimeBarData>()
                            val todayAppointment = pileAppointment.filter { it.getDate() == date }
                                .sortedWith { o1, o2 ->
                                    val o1BeginDateTime = o1.getBeginDateTime()
                                    val o2BeginDateTime = o2.getBeginDateTime()
                                    if (o1BeginDateTime.isBefore(o2BeginDateTime)) {
                                        -1
                                    } else if (o1BeginDateTime.isAfter(o2BeginDateTime)) {
                                        1
                                    } else {
                                        0
                                    }
                                }

                            todayAppointment.forEach { appointment ->
                                val beginTime = appointment.getBeginTime()
                                val endTime = appointment.getEndTime()
                                if (appointment.userId != sharedPreferenceData.userId.toInt()) {
                                    midAppointList.add(
                                        TimeBarData(
                                            beginTime,
                                            endTime,
                                            TimeBarData.APPOINTMENT
                                        )
                                    )
                                } else {
                                    midAppointList.add(
                                        TimeBarData(
                                            beginTime,
                                            endTime,
                                            TimeBarData.MY_APPOINTMENT
                                        )
                                    )
                                }
                            }

                            // 3.3 获取整天TimeBarData数据
                            val finalTimeBarData = splitTimeBarData(openTimeList, midAppointList)

                            // 3.4 将今天现在之前的时间段标注为灰色
                            if(date == nowDate) {
                                val nowTime = LocalTime.now()
                                val index = finalTimeBarData.indexOfFirst { nowTime.isBetween(it.beginTime, it.endTime) }
                                if (index != -1) {
                                    for (j in 0 until index) {
                                        finalTimeBarData[j].state = TimeBarData.STATE_SUSPEND
                                    }
                                    val timeBarData = finalTimeBarData[index]
                                    if (timeBarData.beginTime != nowTime) {
                                        finalTimeBarData.add(index, TimeBarData(timeBarData.beginTime, nowTime, TimeBarData.STATE_SUSPEND))
                                        finalTimeBarData.add(index+1, TimeBarData(nowTime, timeBarData.endTime, timeBarData.state))
                                        finalTimeBarData.removeAt(index+2)
                                    }
                                }
                            }
                            timeBarDataMap[date] = finalTimeBarData
                        }
                        pileDateTimeBarDataMap[pileId] = timeBarDataMap
                    }
                    this@BookViewModel.pileDateTimeBarDataMap.postValue(pileDateTimeBarDataMap)

                } catch (e: Exception) {
                    Log.e(TAG, "网络出错")
                }
            }
        }
    }


    // 根据某一天的营业时间、预约时间获取当前的TimeBarData
    private fun splitTimeBarData(
        openTimeList: List<OpenTime>,
        midAppointList: List<TimeBarData>
    ): ArrayList<TimeBarData> {

        val freeTime = ArrayList(splitTimeSegment(openTimeList, midAppointList))
        freeTime.addAll(midAppointList)

        for (i in 0 until openTimeList.lastIndex) {
            val prevEndTime = LocalTime.parse(openTimeList[i].endTime)
            val nextBeginTime = LocalTime.parse(openTimeList[i + 1].beginTime)
            if (prevEndTime != nextBeginTime) {
                freeTime.add(TimeBarData(prevEndTime, nextBeginTime, TimeBarData.STATE_SUSPEND))
            }
        }

        if (LocalTime.of(0, 0, 0) != LocalTime.parse(openTimeList[0].beginTime)) {
            freeTime.add(
                TimeBarData(
                    LocalTime.of(0, 0, 0),
                    LocalTime.parse(openTimeList[0].beginTime),
                    TimeBarData.STATE_SUSPEND
                )
            )
        }

        if (LocalTime.of(23, 59, 59) != LocalTime.parse(openTimeList.last().endTime)) {
            freeTime.add(
                TimeBarData(
                    LocalTime.parse(openTimeList.last().endTime),
                    LocalTime.of(23, 59, 59),
                    TimeBarData.STATE_SUSPEND
                )
            )
        }

        freeTime.sortWith { o1, o2 ->
            if (o1.beginTime.isBefore(o2.beginTime)) {
                -1
            } else if (o1.beginTime.isAfter(o1.beginTime)) {
                1
            } else {
                0
            }
        }
        return freeTime
    }

    private fun splitTimeSegment(
        openTimeList: List<OpenTime>,
        midAppointList: List<TimeBarData>
    ): List<TimeBarData> {
        val midOpenTimeList = ArrayList<TimeBarData>()

        if (midAppointList.isEmpty()) {
            openTimeList.forEach { openTime ->
                val openTimeBeginTime = LocalTime.parse(openTime.beginTime)
                val openTimeEndTime = LocalTime.parse(openTime.endTime)
                midOpenTimeList.add(
                    TimeBarData(
                        openTimeBeginTime,
                        openTimeEndTime,
                        TimeBarData.STATE_FREE
                    )
                )
            }
        } else {
            // 如果不为空，则需要切割营业时间
            var appointIndex = 0
            openTimeList.forEach { openTime ->
                openTime.beginTime
                val openTimeBeginTime = LocalTime.parse(openTime.beginTime)
                val openTimeEndTime = LocalTime.parse(openTime.endTime)
                var appointBeginTime = midAppointList[appointIndex].beginTime
                var appointEndTime = midAppointList[appointIndex].endTime
                var curBeginTime = openTimeBeginTime


                while (curBeginTime.isBefore(openTimeEndTime) &&  // 预约结束时间在营业时间之前
                    appointBeginTime.isBefore(openTimeEndTime) && // 预约开始时间在营业结束时间之前
                    appointIndex < midAppointList.size // appoint 未遍历完
                ) {
                    if (curBeginTime != appointBeginTime) {
                        midOpenTimeList.add(
                            TimeBarData(
                                curBeginTime,
                                appointBeginTime,
                                TimeBarData.STATE_FREE
                            )
                        )
                    }

                    curBeginTime = appointEndTime
                    appointIndex++
                    if (appointIndex < midAppointList.size) {
                        appointBeginTime = midAppointList[appointIndex].beginTime
                        appointEndTime = midAppointList[appointIndex].endTime
                    }
                }

                if (curBeginTime.isBefore(openTimeEndTime) && (appointIndex == midAppointList.size ||   // appoint 遍历完了
                            !appointBeginTime.isBefore(openTimeEndTime))
                ) {  // 当下一次预约开始时间在这段营业时间之后
                    midOpenTimeList.add(
                        TimeBarData(
                            curBeginTime,
                            openTimeEndTime,
                            TimeBarData.STATE_FREE
                        )
                    )
                }
            }
        }
        return midOpenTimeList
    }


    companion object {
        const val TAG = "BookViewModel"
    }

}