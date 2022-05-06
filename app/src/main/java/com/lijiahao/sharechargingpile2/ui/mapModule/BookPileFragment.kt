package com.lijiahao.sharechargingpile2.ui.mapModule

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.ArrayMap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.*
import androidx.core.view.WindowCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.gson.Gson
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.*
import com.lijiahao.sharechargingpile2.databinding.FragmentBookPileBinding
import com.lijiahao.sharechargingpile2.network.request.AddAppointmentRequest
import com.lijiahao.sharechargingpile2.network.response.AddAppointmentResponse
import com.lijiahao.sharechargingpile2.network.service.AppointmentService
import com.lijiahao.sharechargingpile2.ui.mapModule.adapter.AppointmentInMapAdapter
import com.lijiahao.sharechargingpile2.ui.mapModule.viewmodel.BookViewModel
import com.lijiahao.sharechargingpile2.ui.view.OpenTimeWithChargeFeeLayout
import com.lijiahao.sharechargingpile2.ui.view.TimeBarLinearLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@AndroidEntryPoint
class BookPileFragment : Fragment() {

    private val binding: FragmentBookPileBinding by lazy {
        FragmentBookPileBinding.inflate(layoutInflater)
    }

    private val bookViewModel: BookViewModel by viewModels()

    private val args: BookPileFragmentArgs by navArgs()
    private val stationId: Int by lazy {
        args.stationId
    }
    private var pileId = -1
    private lateinit var popWindow: PopupWindow
    private val adapter = AppointmentInMapAdapter()

    @Inject
    lateinit var sharedPreferenceData: SharedPreferenceData

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var appointmentService: AppointmentService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true) // 取消全屏显示
        binding.chargingPileInfo.visibility = View.GONE
        binding.cardChooseBookTime.visibility = View.GONE
        loadData()
        initUIListener()

        return binding.root
    }

    private fun loadData() {
        bookViewModel.stationInfo.observe(viewLifecycleOwner) { stationInfo ->
            // 1. 设置充电站信息
            binding.itStationName.text = stationInfo.station.name
            binding.tvStationParkFee.text = stationInfo.station.parkFee.toString()
            setOpenTime(stationInfo.openTimeList, stationInfo.chargePeriodList)
        }
    }

    private fun initUIListener() {
        binding.tvToChoosePile.setOnClickListener {
            bookViewModel.stationInfo.value?.let {
                val pileArray = it.pileList.toTypedArray()
                val action =
                    BookPileFragmentDirections.actionBookPileFragmentToChoosePileFragment(pileArray)
                findNavController().navigate(action)
            }
        }

        setFragmentResultListener(CHOOSE_PILE_ID_RESULT_KEY) { _, bundle ->
            pileId = bundle.getInt(CHOOSE_PILE_ID_BUNDLE_KEY)
            bookViewModel.stationInfo.observe(viewLifecycleOwner) {
                showPileInfoAndTimeBar()
            }
        }

    }

    // 在选择充电桩后显示TimeBar
    private fun showPileInfoAndTimeBar() {

        // 1. 设置pileInfo
        val pile = bookViewModel.stationInfo.value?.let { stationInfo ->
            stationInfo.pileList.find { it.id == pileId }
        } ?: return

        binding.chargingPileInfo.visibility = View.VISIBLE
        binding.cardChooseBookTime.visibility = View.VISIBLE

        binding.tvPileState.text = pile.state
        binding.tvElectricType.text = pile.electricType
        binding.tvPowerRate.text = pile.powerRate.toString()

        // 2. 获取时间表数据
        val openTimeList =
            (bookViewModel.stationInfo.value?.openTimeList?.sortedWith(Comparator<OpenTime> { o1, o2 ->
                val o1BeginTime = LocalTime.parse(o1.beginTime)
                val o2BeginTime = LocalTime.parse(o2.beginTime)
                if (o1BeginTime.isBefore(o2BeginTime)) {
                    -1
                } else if (o1BeginTime.isAfter(o2BeginTime)) {
                    1
                } else {
                    0
                }
            })) ?: return

        val appointments = bookViewModel.appointments.filter { appointment ->
            appointment.pileId == pileId
        }

        val nowDate = LocalDate.now()
        val timeBarDataMap = ArrayMap<LocalDate, ArrayList<TimeBarData>>()

        // 2.1 显示未来7天的可预约时间
        for (i in 0 until 7) { // [0,7)
            val list = ArrayList<TimeBarData>()
            val date = nowDate.plusDays(i.toLong())
            timeBarDataMap[date] = list
        }

        // 2.2 预约切割营业时间
        for (entry in timeBarDataMap) {
            val date = entry.key
            val list = entry.value

            // （1） 获取今天的预约
            val todayAppointment = appointments.filter { appointment ->
                val beginDateTime = LocalDateTime.parse(appointment.beginDateTime)
                val endDateTime = LocalDateTime.parse(appointment.endDateTime)
                val beginDate = beginDateTime.toLocalDate()
                val endDate = endDateTime.toLocalDate()
                beginDate == date || endDate == date
            }.sortedWith(Comparator<Appointment> { o1, o2 ->
                val o1BeginDateTime = LocalDateTime.parse(o1.beginDateTime)
                val o2BeginDateTime = LocalDateTime.parse(o2.beginDateTime)
                if (o1BeginDateTime.isBefore(o2BeginDateTime)) {
                    -1
                } else if (o1BeginDateTime.isAfter(o2BeginDateTime)) {
                    1
                } else {
                    0
                }
            })

            // （2） 获取预约
            val midAppointList = ArrayList<TimeBarData>()
            todayAppointment.forEachIndexed { _, appointment ->
                val beginTime = LocalDateTime.parse(appointment.beginDateTime).toLocalTime()
                val endTime = LocalDateTime.parse(appointment.endDateTime).toLocalTime()
                if (appointment.userId != sharedPreferenceData.userId.toInt()) {
                    midAppointList.add(TimeBarData(beginTime, endTime, TimeBarData.APPOINTMENT))
                } else {
                    midAppointList.add(TimeBarData(beginTime, endTime, TimeBarData.MY_APPOINTMENT))
                }
            }

            // （3） 将Appointment放入OpenTime中，获得切割后的时间段
            val res = splitTimeBarData(openTimeList, midAppointList)
            list.clear()
            list.addAll(res)
        }

        // 3. 设置时间表UI
        val dayTextView = arrayOf(
            binding.day1,
            binding.day2,
            binding.day3,
            binding.day4,
            binding.day5,
            binding.day6,
            binding.day7
        )
        val dayInWeekStrMap =
            mapOf(1 to "一", 2 to "二", 3 to "三", 4 to "四", 5 to "五", 6 to "六", 7 to "日")
        val timeBars = arrayOf(
            binding.day1TimeBar,
            binding.day2TimeBar,
            binding.day3TimeBar,
            binding.day4TimeBar,
            binding.day5TimeBar,
            binding.day6TimeBar,
            binding.day7TimeBar
        )

        for (i in 0 until 7) { // [0,7)
            val date = nowDate.plusDays(i.toLong())
            val timeBarDataList = timeBarDataMap[date]
            dayTextView[i].text = dayInWeekStrMap[date.dayOfWeek.value]
            timeBarDataList?.let {
                timeBars[i].setTime(timeBarDataList, date)

                // 设置点击时间
                setTimeBarViewClickListener(timeBars[i])
            }
        }

        // 4. 设置日期范围
        val curDate = nowDate.format(DateTimeFormatter.ofPattern("M月d日"))
        val lastDate = nowDate.plusDays(6).format(DateTimeFormatter.ofPattern("M月d日"))
        binding.dateRange.text = "$curDate~$lastDate"

        // 5. 设置recyclerView
        binding.recyclerView.adapter = adapter
        val list = bookViewModel.appointments.filter {
            it.userId == sharedPreferenceData.userId.toInt()
        }
        adapter.submitList(list.sortedByDescending { it.getBeginDateTime() })
    }


    // 设置TimaBarView的点击时间，点击后弹出PopUpWindow
    private fun setTimeBarViewClickListener(timeBar: TimeBarLinearLayout) {
        val childViews = timeBar.children
        val date = timeBar.date
        childViews.forEach { view ->
            view.setOnClickListener {
                val timeBarData = gson.fromJson(view.tag as String, TimeBarData::class.java)
                Log.e(TAG, "view:${it}, tag=${it.tag}, timeBarData=${timeBarData}")
                showPopUpWindow(timeBarData, it, date)
            }
        }

    }

    // 参考 https://github.com/PopFisher/SmartPopupWindow
    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    private fun showPopUpWindow(timeBarData: TimeBarData, anchorView: View, date: LocalDate) {
        if (::popWindow.isInitialized) {
            popWindow.dismiss()
        }

        // 1. 加载布局
        val popupView = LayoutInflater.from(requireContext())
            .inflate(R.layout.popup_content_top_arrow_layout, null)
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        // 2. 创建PopupWindow
        popWindow = PopupWindow(
            popupView,
            popupView.measuredWidth,
            popupView.measuredHeight,
            true
        )
        popWindow.animationStyle = R.style.anim_pop
        popWindow.isTouchable = true
        popWindow.setBackgroundDrawable(ColorDrawable())
        popWindow.isOutsideTouchable = true

        // 3. 动态设置箭头位置
        popupView.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 自动调整箭头的位置
                autoAdjustArrowPos(popWindow, popupView, anchorView)
                popupView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        // 4. 设置UI
        val tvTime: TextView = popupView.findViewById(R.id.tv_time)
        val tvDate: TextView = popupView.findViewById(R.id.tv_date)
        val tvState: TextView = popupView.findViewById(R.id.tv_state)
        val btnBook: Button = popupView.findViewById(R.id.btn_book)
        val btnCancel: Button = popupView.findViewById(R.id.btn_cancel)
        tvDate.text = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        tvTime.text = timeBarData.beginTime.format(DateTimeFormatter.ofPattern("HH:mm")) +
                "~${timeBarData.endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        tvState.text = timeBarData.state

        btnBook.setOnClickListener {
            bookPile(timeBarData, date)
        }

        btnCancel.setOnClickListener {
            popWindow.dismiss()
        }

        // 5. 显示PopupWindow
        popWindow.showAsDropDown(anchorView)
    }

    // 根据anchorView 和 popupWindow的位置 自动调整PopUpWindow箭头的位置
    private fun autoAdjustArrowPos(popupWindow: PopupWindow, contentView: View, anchorView: View) {
        val upArrow = contentView.findViewById<View>(R.id.up_arrow)
        val downArrow = contentView.findViewById<View>(R.id.down_arrow)

        val pos = IntArray(2)
        contentView.getLocationOnScreen(pos) // 弹窗的位置
        val popLeftPos = pos[0]
        anchorView.getLocationOnScreen(pos) // 目标View的位置
        val anchorLeftPos = pos[0]

        val arrowLeftMargin = anchorLeftPos - popLeftPos + anchorView.width / 2 - upArrow.width / 2

        upArrow.visibility = if (popupWindow.isAboveAnchor) View.INVISIBLE else View.VISIBLE
        downArrow.visibility = if (popupWindow.isAboveAnchor) View.VISIBLE else View.INVISIBLE
        val upArrowParams = upArrow.layoutParams as RelativeLayout.LayoutParams
        upArrowParams.marginStart = arrowLeftMargin
        upArrow.layoutParams = upArrowParams
        val downArrowParams = downArrow.layoutParams as RelativeLayout.LayoutParams
        downArrowParams.marginStart = arrowLeftMargin
        downArrow.layoutParams = downArrowParams
    }

    // 点击 PopupWindow 中的预约按钮后弹出时间选择器，选择预约时间
    private fun bookPile(timeBarData: TimeBarData, date: LocalDate) {
        val pickBeginTime = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(timeBarData.beginTime.hour)
            .setMinute(timeBarData.beginTime.minute)
            .setTitleText("选择开始时间")
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .build()
        pickBeginTime.show(parentFragmentManager, "beginTimePicker")
        pickBeginTime.addOnPositiveButtonClickListener {
            val pickEndTime = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(timeBarData.endTime.hour)
                .setMinute(timeBarData.endTime.minute)
                .setTitleText("选择结束时间")
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build()
            pickEndTime.show(parentFragmentManager, "endTimePicker")
            pickEndTime.addOnPositiveButtonClickListener {
                val beginTime = LocalTime.of(pickBeginTime.hour, pickBeginTime.minute)
                val endTime = LocalTime.of(pickEndTime.hour, pickEndTime.minute)

                // verify
                if (beginTime.isBefore(endTime) &&
                    !beginTime.isBefore(timeBarData.beginTime) && !beginTime.isAfter(timeBarData.endTime) &&
                    !endTime.isBefore(timeBarData.beginTime) && !endTime.isAfter(timeBarData.endTime)
                ) {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                            val request = AddAppointmentRequest(
                                0,
                                date.format(dateFormatter),
                                beginTime.format(timeFormatter),
                                endTime.format(timeFormatter),
                                pileId,
                                sharedPreferenceData.userId.toInt(),
                                stationId,
                                Appointment.STATE_WAITING
                            )
                            val res = appointmentService.addAppointment(request)
                            withContext(Dispatchers.Main) {
                                when (res) {
                                    AddAppointmentResponse.SUCCESS -> {
                                        Toast.makeText(context, "预约成功", Toast.LENGTH_SHORT).show()
                                    }
                                    AddAppointmentResponse.CONFLICT -> {
                                        Toast.makeText(context, "预约时间冲突", Toast.LENGTH_SHORT).show()
                                    }
                                    AddAppointmentResponse.FAIL -> {
                                        Toast.makeText(context, "预约信息错误", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                bookViewModel.getData()
                                if (::popWindow.isInitialized) {
                                    popWindow.dismiss()
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                            Log.e(TAG, "网络异常")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "请选择范围内时间", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    // 根据某一天的营业时间、预约时间获取当前的TimeBarData
    private fun splitTimeBarData(
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


    private fun setOpenTime(
        openTimes: List<OpenTime>,
        electricChargePeriods: List<ElectricChargePeriod>
    ) {
        val linearLayout = binding.openTimeLinearLayout
        linearLayout.removeAllViews()
        context?.let {
            openTimes.forEach { openTime ->
                openTime.toElectricChargePeriods(electricChargePeriods).forEach { period ->
                    val content = OpenTimeWithChargeFeeLayout(it)
                    content.setOpenTime(period)
                    linearLayout.addView(content)
                }
            }
        }
    }

    companion object {
        const val CHOOSE_PILE_ID_RESULT_KEY = "CHOOSE_PILE_ID_RESULT_KEY"
        const val CHOOSE_PILE_ID_BUNDLE_KEY = "CHOOSE_PILE_ID_BUNDLE_KEY"
        const val ALL_DAY_SECONDS = 24 * 60 * 60
        const val TAG = "BookPileFragment"
    }
}