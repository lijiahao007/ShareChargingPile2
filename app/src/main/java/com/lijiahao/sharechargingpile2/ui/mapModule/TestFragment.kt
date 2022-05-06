package com.lijiahao.sharechargingpile2.ui.mapModule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.TimeBarData
import com.lijiahao.sharechargingpile2.databinding.FragmentTestBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import kotlin.random.Random


@AndroidEntryPoint
class TestFragment : Fragment() {
    private val binding: FragmentTestBinding by lazy {
        FragmentTestBinding.inflate(layoutInflater)
    }

    private val colorArr: Array<Int> = arrayOf(R.color.green, R.color.gray, R.color.red)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding.btnFresh.setOnClickListener {
            val linearLayout = binding.linearLayout
            linearLayout.removeAllViews()
            for (i in 0 until 20) {
                val view = View(context)
                val randomHeight = Random.nextInt(100, 200)
                val layoutParams = LinearLayout.LayoutParams(
                    context!!.resources.getDimension(R.dimen.book_time_width).toInt(),
                    randomHeight
                )
                val randomColor = Random.nextInt(0, 3)
                view.setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        colorArr[randomColor],
                        null
                    )
                )
                view.layoutParams = layoutParams
                linearLayout.addView(view)
            }


            val randomFirst = Random.nextInt(0, 13)
            val randomSecond = Random.nextInt(13, 24)
            val timeBarDataList = listOf(
                TimeBarData(
                    LocalTime.of(0, 0, 0),
                    LocalTime.of(randomFirst, 0, 0),
                    TimeBarData.STATE_FREE
                ),
                TimeBarData(
                    LocalTime.of(randomFirst, 0, 0),
                    LocalTime.of(randomSecond, 0, 0),
                    TimeBarData.APPOINTMENT
                ),
                TimeBarData(
                    LocalTime.of(randomSecond, 0, 0),
                    LocalTime.of(23, 59, 59),
                    TimeBarData.STATE_SUSPEND
                ),
            )

            binding.timeBar.setTime(timeBarDataList, LocalDate.now())

        }
        return binding.root
    }
}