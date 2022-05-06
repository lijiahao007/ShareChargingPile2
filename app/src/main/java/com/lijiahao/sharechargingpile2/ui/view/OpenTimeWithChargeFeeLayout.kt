package com.lijiahao.sharechargingpile2.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.ElectricChargePeriod
import com.lijiahao.sharechargingpile2.utils.TimeUtils

class OpenTimeWithChargeFeeLayout(
    private val mContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr:Int = 0 // 0 表示使用Style
) : LinearLayout(mContext, attrs, defStyleAttr) {



    init{
        inflate()
        initView()
    }

    private fun inflate() {
        LayoutInflater.from(mContext).inflate(R.layout.linear_layout_opentime_chargefee, this, true)
    }

    private fun initView(openTime:String="", chargeFee:String="") {
        val tvOpenTime =  findViewById<TextView>(R.id.tv_open_time)
        val tvChargeFee = findViewById<TextView>(R.id.tv_current_electric_fee)
        tvOpenTime.text = openTime
        tvChargeFee.text = chargeFee
    }

    fun setOpenTime(openTime:ElectricChargePeriod) {
        initView(TimeUtils.getOpenTimeString(openTime), openTime.electricCharge.toString())
    }


}