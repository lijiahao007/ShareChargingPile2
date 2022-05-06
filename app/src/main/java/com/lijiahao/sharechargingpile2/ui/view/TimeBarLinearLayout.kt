package com.lijiahao.sharechargingpile2.ui.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.google.gson.Gson
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.TimeBarData
import com.lijiahao.sharechargingpile2.di.MyAppGlideModule
import com.lijiahao.sharechargingpile2.di.annotation.HttpConnect
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.time.Duration
import java.time.LocalDate

class TimeBarLinearLayout(
    private val mContext: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(mContext, attrs) {

    private lateinit var timeList: List<TimeBarData>
    private val gson: Gson by lazy {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            TimeBarLinearLayoutEntryPoint::class.java
        ).gson()
    }

    lateinit var date: LocalDate

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TimeBarLinearLayoutEntryPoint {
        fun gson(): Gson
    }


    init {
        LayoutInflater.from(mContext).inflate(R.layout.view_timebar_linear_layout, this, true)
    }

    fun setTime(timeList: List<TimeBarData>, date: LocalDate) {
        this.date = date
        this.timeList = timeList
        val totalHeight = mContext.resources.getDimension(R.dimen.book_time_height)

        removeAllViews()

        val lp = layoutParams
        orientation = VERTICAL
        lp.height = mContext.resources.getDimension(R.dimen.book_time_height).toInt()
        lp.width = mContext.resources.getDimension(R.dimen.book_time_width).toInt()
        layoutParams = lp

        timeList.forEach { timeBarData ->
            val duration = Duration.between(timeBarData.beginTime, timeBarData.endTime)
            val height = duration.seconds / ALL_DAY_SECONDS * totalHeight
            val colorId = when (timeBarData.state) {
                TimeBarData.STATE_FREE -> {
                    R.color.green
                }
                TimeBarData.APPOINTMENT, TimeBarData.STATE_USING -> {
                    R.color.red
                }
                TimeBarData.MY_APPOINTMENT -> {
                    R.color.blue
                }
                else -> {
                    R.color.gray
                }
            }

            val view = View(context)
            val layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                height.toInt()
            )
            view.layoutParams = layoutParams
            view.setBackgroundColor(ResourcesCompat.getColor(resources, colorId, null))
            val json = gson.toJson(timeBarData)
            view.tag = json
            Log.e("TimeBarLinearLayout", "json = $json  timeBarData=$timeBarData")
            addView(view)
        }
    }

    companion object {
        const val ALL_DAY_SECONDS: Double = 24.0 * 60.0 * 60.0
    }

}