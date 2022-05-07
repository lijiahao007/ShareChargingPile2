package com.lijiahao.sharechargingpile2

import com.lijiahao.sharechargingpile2.data.Appointment
import com.lijiahao.sharechargingpile2.data.TimeBarData
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

class TimeRangeTest {


    @Test
    fun getMaxTime() {
        val list = object : ArrayList<TimeBarData>() {
            init {
                add(
                    TimeBarData(
                        LocalTime.of(0, 0, 0),
                        LocalTime.of(8, 0, 0),
                        TimeBarData.STATE_FREE
                    )
                )
                add(
                    TimeBarData(
                        LocalTime.of(8, 0, 0),
                        LocalTime.of(9, 0, 0),
                        TimeBarData.APPOINTMENT
                    )
                )
                add(
                    TimeBarData(
                        LocalTime.of(9, 0, 0),
                        LocalTime.of(12, 0, 0),
                        TimeBarData.STATE_FREE
                    )
                )
                add(
                    TimeBarData(
                        LocalTime.of(12, 0, 0),
                        LocalTime.of(15, 0, 0),
                        TimeBarData.APPOINTMENT
                    )
                )
                add(
                    TimeBarData(
                        LocalTime.of(15, 0, 0),
                        LocalTime.of(18, 0, 0),
                        TimeBarData.STATE_FREE
                    )
                )
                add(
                    TimeBarData(
                        LocalTime.of(18, 0, 0),
                        LocalTime.of(23, 59, 59),
                        TimeBarData.STATE_SUSPEND
                    )
                )
            }
        }

        val appointment = Appointment(
            0,
            LocalDateTime.of(2022, 5, 7, 12, 0, 0).toString(),
            LocalDateTime.of(2022, 5, 7, 15, 0, 0).toString(),
            0,
            0,
            0,
            Appointment.STATE_WAITING
        )

        val res = getMaxTimeInTimeBarList(list, appointment)
        println(res)
    }

    fun getMaxTimeInTimeBarList(
        timeBarDataList: List<TimeBarData>,
        appointment: Appointment
    ): Pair<LocalTime, LocalTime>? {
        val indexOfAppointment =
            timeBarDataList.indexOfFirst { it.beginTime == appointment.getBeginTime() && it.endTime == appointment.getEndTime() }
        if (indexOfAppointment == -1) return null

        var curIndex = indexOfAppointment
        var beginTime: LocalTime = appointment.getBeginTime()
        var endTime: LocalTime = appointment.getEndTime()
        while (curIndex > 0) {
            val timeBarData = timeBarDataList[curIndex - 1]
            if (timeBarData.state == TimeBarData.STATE_FREE) {
                beginTime = timeBarData.beginTime
            } else {
                break
            }
            curIndex--
        }
        curIndex = indexOfAppointment
        while (curIndex < timeBarDataList.lastIndex) {
            val timeBarData = timeBarDataList[curIndex + 1]
            if (timeBarData.state == TimeBarData.STATE_FREE) {
                endTime = timeBarData.endTime
            } else {
                break
            }
            curIndex++
        }
        return Pair(beginTime, endTime)
    }

}