package com.lijiahao.sharechargingpile2.utils

import android.util.Log
import com.lijiahao.sharechargingpile2.data.ElectricChargePeriod
import com.lijiahao.sharechargingpile2.data.OpenTime
import java.time.*
import java.time.format.DateTimeFormatter


class TimeUtils {
    companion object {
        // 获取时间戳相对于现在的文本描述
        @JvmStatic
        fun getSendTimeText(timestamp: Long): String {
            var res = ""
            val instant: Instant = Instant.ofEpochMilli(timestamp)
            val zone: ZoneId = ZoneId.of("Asia/Shanghai")
            val localDateTime = LocalDateTime.ofInstant(instant, zone)
            val nowDateTime = LocalDateTime.now()
            val duration = Duration.between(localDateTime, nowDateTime)
            val days = duration.toDays()
            Log.i("TimeUtils", "duration=$duration,  days=$days")
            if (days < 1) {
                res = localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            } else if (days == 1L) {
                res = "昨天"
            } else if (days < 365) {
                res = "${localDateTime.monthValue}月${localDateTime.dayOfMonth}日"
            } else {
                res = "${localDateTime.year}年${localDateTime.monthValue}月${localDateTime.dayOfMonth}日"
            }
            return res;
        }

        @JvmStatic
        fun getOpenTimeString(openTime: OpenTime): String {
            return openTime.beginTime.substring(
                0,
                5
            ) + "~" + openTime.endTime.substring(0, 5)
        }

        @JvmStatic
        fun getOpenTimeString(electricChargePeriod: ElectricChargePeriod): String {
            return electricChargePeriod.beginTime.substring(
                0,
                5
            ) + "~" + electricChargePeriod.endTime.substring(0, 5)
        }


        @JvmStatic
        fun getFormatTimeStr(dateTimeStr: String): String {
            val beginTime = LocalDateTime.parse(dateTimeStr)
            return beginTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"))
        }


        @JvmStatic
        fun getFormatTimeStr(localDateTime: LocalDateTime): String {
            return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"))
        }

        fun LocalTime.isBetween(begin:LocalTime, end:LocalTime):Boolean {
            return (!isBefore(begin) && !isAfter(end))
        }

        fun LocalDateTime.isBetween(begin:LocalDateTime, end:LocalDateTime):Boolean {
            return (!isBefore(begin) && !isAfter(end))
        }


    }
}