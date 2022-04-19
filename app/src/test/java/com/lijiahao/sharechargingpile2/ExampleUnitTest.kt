package com.lijiahao.sharechargingpile2

import com.lijiahao.sharechargingpile2.data.ElectricChargePeriod
import com.lijiahao.sharechargingpile2.data.OpenTime
import com.lijiahao.sharechargingpile2.data.TokenInfo
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun sumOfTest() {
        val list = listOf<Double>(1.0, 2.0, 3.0, 4.0)
        println(list.sumOf { it })
    }

    @Test
    fun openTimeTest() {
        val openTime = OpenTime(0, "00:00", "10:00", 1)
        val electricChargePeriods = ArrayList<ElectricChargePeriod>()
        electricChargePeriods.add(ElectricChargePeriod(0, "00:00", "03:00", 1, 1.0f))
        electricChargePeriods.add(ElectricChargePeriod(0, "03:00", "06:00", 1, 2.0f))
        electricChargePeriods.add(ElectricChargePeriod(0, "06:00", "09:00", 1, 3.0f))
        electricChargePeriods.add(ElectricChargePeriod(0, "09:00", "12:00", 1, 4f))
        val res = openTime.toElectricChargePeriods(electricChargePeriods)
        res.forEach {
            println(it)
        }
    }

    @Test
    fun tokenTest() {
        val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIxIiwiZXhwIjoxNjUwNDQyMzE0LCJpYXQiOjE2NTAzNTU5MTR9.XPazDDvOGJhRFFKzzypqSYEzQZXVpeW7wRlJiV4jRss";
        val info = TokenInfo.getTokenInfoFromToken(token)
        println(info)
    }

}