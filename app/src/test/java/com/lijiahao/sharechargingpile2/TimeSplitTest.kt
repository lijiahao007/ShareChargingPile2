package com.lijiahao.sharechargingpile2

import com.google.gson.Gson
import com.lijiahao.sharechargingpile2.data.OpenTime
import com.lijiahao.sharechargingpile2.data.TimeBarData
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

class TimeSplitTest {


    @Test
    fun splitTimeTest() {

        val openTimeList = object : ArrayList<OpenTime>() {
            init {
                add(OpenTime(0, LocalTime.of(3, 0).toString(), LocalTime.of(12, 0).toString(), 0))
                add(OpenTime(0, LocalTime.of(16, 0).toString(), LocalTime.of(21, 0).toString(), 0))
            }
        }

        val midAppointList = object : ArrayList<TimeBarData>() {
            init {
                add(TimeBarData(LocalTime.of(4, 0), LocalTime.of(5, 0), TimeBarData.APPOINTMENT))
                add(TimeBarData(LocalTime.of(9, 0), LocalTime.of(11, 0), TimeBarData.APPOINTMENT))
                add(TimeBarData(LocalTime.of(16, 0), LocalTime.of(17, 0), TimeBarData.APPOINTMENT))
                add(TimeBarData(LocalTime.of(19, 0), LocalTime.of(20, 0), TimeBarData.APPOINTMENT))
            }
        }

        val res = splitTimeSegment(openTimeList, midAppointList)
        res.forEach {
            println(it)
        }
    }

    @Test
    fun test1() {
        val openTimeList = object : ArrayList<OpenTime>() {
            init {
                add(OpenTime(0, LocalTime.of(3, 0).toString(), LocalTime.of(12, 0).toString(), 0))
                add(OpenTime(0, LocalTime.of(16, 0).toString(), LocalTime.of(21, 0).toString(), 0))
            }
        }

        val midAppointList = object : ArrayList<TimeBarData>() {
            init {
                add(TimeBarData(LocalTime.of(4, 0), LocalTime.of(5, 0), TimeBarData.APPOINTMENT))
                add(TimeBarData(LocalTime.of(9, 0), LocalTime.of(11, 0), TimeBarData.APPOINTMENT))
                add(TimeBarData(LocalTime.of(16, 0), LocalTime.of(17, 0), TimeBarData.APPOINTMENT))
                add(TimeBarData(LocalTime.of(19, 0), LocalTime.of(20, 0), TimeBarData.APPOINTMENT))
            }
        }

        val res = getMidTimeSegment(openTimeList, midAppointList)
        res.forEach {
            println(it)
        }
    }

    @Test
    fun test2() {
        val openTimeList = object : ArrayList<OpenTime>() {
            init {
                add(OpenTime(0, LocalTime.of(0, 0).toString(), LocalTime.of(23, 59).toString(), 0))
            }
        }

        val midAppointList = object : ArrayList<TimeBarData>() {
            init {
                add(TimeBarData(LocalTime.of(8, 0), LocalTime.of(18, 0), TimeBarData.APPOINTMENT))
            }
        }

        val res = getMidTimeSegment(openTimeList, midAppointList)
        res.forEach {
            println(it)
        }

    }



    fun splitTimeSegment(
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

    fun getMidTimeSegment(
        openTimeList: List<OpenTime>,
        midAppointList: List<TimeBarData>
    ): List<TimeBarData> {

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

    @Test
    fun testTimeBarDataJson() {
        val gson = Gson()
        val timeBarData = TimeBarData(LocalTime.of(10, 0, 0), LocalTime.of(15, 0, 0), TimeBarData.STATE_FREE)
        val timeJson = gson.toJson(timeBarData)
        val timeBarData1 = gson.fromJson(timeJson, TimeBarData::class.java)
        println("timeJson: $timeJson")
        println("timeBarData1: $timeBarData1")
    }


    @Test
    fun testTimeSort() {
        val list = object:ArrayList<LocalDateTime>() {
            init {
                add(LocalDateTime.of(2022, 5, 5, 10, 10))
                add(LocalDateTime.of(2022, 5, 6, 10, 10))
                add(LocalDateTime.of(2022, 5, 7, 10, 10))
                add(LocalDateTime.of(2022, 5, 7, 13, 10))
            }
        }

        list.sortByDescending { it }
        list.forEach {
            println(it)
        }
    }


}