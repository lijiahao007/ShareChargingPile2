package com.lijiahao.sharechargingpile2.ui.mapModule

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.*
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
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
import com.lijiahao.sharechargingpile2.network.request.ModifyAppointmentRequest
import com.lijiahao.sharechargingpile2.network.response.AddAppointmentResponse
import com.lijiahao.sharechargingpile2.network.service.AppointmentService
import com.lijiahao.sharechargingpile2.ui.QRCodeModule.QRCodeActivity
import com.lijiahao.sharechargingpile2.ui.QRCodeModule.QRCodeScanFragment
import com.lijiahao.sharechargingpile2.ui.mapModule.adapter.AppointmentInMapAdapter
import com.lijiahao.sharechargingpile2.ui.mapModule.viewmodel.BookViewModel
import com.lijiahao.sharechargingpile2.ui.view.OpenTimeWithChargeFeeLayout
import com.lijiahao.sharechargingpile2.ui.view.TimeBarLinearLayout
import com.lijiahao.sharechargingpile2.utils.TimeUtils.Companion.isBetween
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
    private lateinit var timeBarPopupWindow: PopupWindow
    private val adapter = AppointmentInMapAdapter(this)
    private lateinit var updateAppointmentPopupWindow: PopupWindow

    @Inject
    lateinit var sharedPreferenceData: SharedPreferenceData

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var appointmentService: AppointmentService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true) // 取消全屏显示
        binding.chargingPileInfo.visibility = View.GONE
        binding.cardChooseBookTime.visibility = View.GONE
        binding.cardAppointment.visibility = View.GONE
        loadData()
        initUIListener()

        return binding.root
    }

    private fun loadData() {
        bookViewModel.pileDateTimeBarDataMap.observe(viewLifecycleOwner) {
            // 1. 设置充电站信息
            val stationInfo = bookViewModel.stationInfo
            stationInfo?.let {
                binding.itStationName.text = stationInfo.station.name
                binding.tvStationParkFee.text = stationInfo.station.parkFee.toString()
                setOpenTime(stationInfo.openTimeList, stationInfo.chargePeriodList)
            }
        }
    }

    private fun initUIListener() {
        binding.tvToChoosePile.setOnClickListener {
            bookViewModel.stationInfo?.let {
                val pileArray = it.pileList.toTypedArray()
                val action = BookPileFragmentDirections.actionBookPileFragmentToChoosePileFragment(pileArray)
                findNavController().navigate(action)
            }
        }

        setFragmentResultListener(CHOOSE_PILE_ID_RESULT_KEY) { _, bundle ->
            pileId = bundle.getInt(CHOOSE_PILE_ID_BUNDLE_KEY)
            bookViewModel.pileDateTimeBarDataMap.observe(viewLifecycleOwner) {
                // !!!! 监听ViewModel的变化，一旦变化就更新UI
                showOtherUI()
            }
        }

    }

    // 在选择pile之后，显示pile信息、TimeBar、用户预约。
    private fun showOtherUI() {

        // 1. 设置pileInfo
        val pile = bookViewModel.stationInfo?.let { stationInfo ->
            stationInfo.pileList.find { it.id == pileId }
        } ?: return

        binding.chargingPileInfo.visibility = View.VISIBLE
        binding.cardChooseBookTime.visibility = View.VISIBLE
        binding.cardAppointment.visibility = View.VISIBLE

        binding.tvPileState.text = pile.state
        binding.tvElectricType.text = pile.electricType
        binding.tvPowerRate.text = pile.powerRate.toString()
        if (bookViewModel.isPileBooked(pileId)) {
            binding.tvPileState.text = ChargingPile.STATE_APPOINTMENT
        }

        //2. 获取TimeBarData
        val nowDate = LocalDate.now()
        val timeBarDataMap = bookViewModel.pileDateTimeBarDataMap.value?.get(pileId) ?: HashMap()

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

    // 设置TimeBarView的点击时间，点击后弹出PopUpWindow
    private fun setTimeBarViewClickListener(timeBar: TimeBarLinearLayout) {
        val childViews = timeBar.children
        val date = timeBar.date
        childViews.forEach { view ->
            view.setOnClickListener {
                val timeBarData = gson.fromJson(view.tag as String, TimeBarData::class.java)
                Log.e(TAG, "view:${it}, tag=${it.tag}, timeBarData=${timeBarData}")
                showTimeBarPopUpWindow(timeBarData, it, date)
            }
        }

    }

    // 参考 https://github.com/PopFisher/SmartPopupWindow
    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    private fun showTimeBarPopUpWindow(
        timeBarData: TimeBarData,
        anchorView: View,
        date: LocalDate
    ) {
        if (::timeBarPopupWindow.isInitialized) {
            timeBarPopupWindow.dismiss()
        }

        // 1. 加载布局
        val popupView = LayoutInflater.from(requireContext())
            .inflate(R.layout.popup_content_top_arrow_layout, null)
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        // 2. 创建PopupWindow
        timeBarPopupWindow = PopupWindow(
            popupView,
            popupView.measuredWidth,
            popupView.measuredHeight,
            true
        )
        timeBarPopupWindow.animationStyle = R.style.anim_pop
        timeBarPopupWindow.isTouchable = true
        timeBarPopupWindow.setBackgroundDrawable(ColorDrawable())
        timeBarPopupWindow.isOutsideTouchable = true

        // 3. 动态设置箭头位置
        popupView.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 自动调整箭头的位置
                autoAdjustArrowPos(timeBarPopupWindow, popupView, anchorView)
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
            timeBarPopupWindow.dismiss()
        }

        // 5. 显示PopupWindow
        timeBarPopupWindow.showAsDropDown(anchorView)
    }


    @SuppressLint("SetTextI18n")
    fun showPileInfoPopupWindow(appointment: Appointment, anchorView: View) {
        if (::updateAppointmentPopupWindow.isInitialized) {
            updateAppointmentPopupWindow.dismiss()
        }

        // 1. 加载布局
        val popupView = LayoutInflater.from(requireContext())
            .inflate(R.layout.popup_content_top_arrow_layout_update, null)
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        // 2. 创建PopupWindow
        updateAppointmentPopupWindow = PopupWindow(
            popupView,
            popupView.measuredWidth,
            popupView.measuredHeight,
            true
        )
        updateAppointmentPopupWindow.animationStyle = R.style.anim_pop
        updateAppointmentPopupWindow.isTouchable = true
        updateAppointmentPopupWindow.setBackgroundDrawable(ColorDrawable())
        updateAppointmentPopupWindow.isOutsideTouchable = true

        // 3. 动态设置箭头位置
        popupView.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 自动调整箭头的位置
                autoAdjustArrowPos(updateAppointmentPopupWindow, popupView, anchorView)
                popupView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        // 4. 设置UI
        val tvDate: TextView = popupView.findViewById(R.id.tv_date)
        val tvTime: TextView = popupView.findViewById(R.id.tv_time)
        val tvState: TextView = popupView.findViewById(R.id.tv_state)
        val tvModifyTime: TextView = popupView.findViewById(R.id.tv_modify_time)
        val btnUse: Button = popupView.findViewById(R.id.btn_use)
        val btnModify: Button = popupView.findViewById(R.id.btn_modify)
        val btnDelete: Button = popupView.findViewById(R.id.btn_delete)
        val btnCancel: Button = popupView.findViewById(R.id.btn_cancel)
        tvDate.text = appointment.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        tvTime.text = "${appointment.getBeginTime().format(timeFormatter)}~${
            appointment.getEndTime().format(timeFormatter)
        }"
        tvState.text = appointment.state
        var isShowModifyTime = true
        var range: Pair<LocalTime, LocalTime>? = null
        // 4.1 获取可修改的最大范围
        bookViewModel.pileDateTimeBarDataMap.value?.let { map ->
            val timeBarList = map[pileId]?.get(appointment.getDate())
            timeBarList ?: run { isShowModifyTime = false }
            timeBarList?.let {
                range = getMaxTimeInTimeBarList(it, appointment)
                range?.let { range ->
                    val beginTime = range.first
                    val endTime = range.second
                    val formatter = DateTimeFormatter.ofPattern("HH:mm")
                    tvModifyTime.text =
                        "${beginTime.format(formatter)}~${endTime.format(formatter)}"
                }
            }
        }

        if (!isShowModifyTime) {
            popupView.findViewById<LinearLayout>(R.id.modify_range_layout).visibility = View.GONE
        } else {
            popupView.findViewById<LinearLayout>(R.id.modify_range_layout).visibility = View.VISIBLE
        }

        btnUse.setOnClickListener {
            updateAppointmentPopupWindow.dismiss()
            val activity = requireActivity()
            val intent = Intent(requireContext(), QRCodeActivity::class.java)
            intent.putExtra("pileId", pileId)
            activity.startActivity(intent)
        }

        btnModify.setOnClickListener {
            modifyPile(appointment, range)
        }

        btnDelete.setOnClickListener {
            deletePile(appointment)
        }

        btnCancel.setOnClickListener {
            updateAppointmentPopupWindow.dismiss()
        }


        // 5. 显示PopupWindow
        updateAppointmentPopupWindow.showAsDropDown(anchorView)
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
                                if (::timeBarPopupWindow.isInitialized) {
                                    timeBarPopupWindow.dismiss()
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

    private fun modifyPile(appointment: Appointment, range: Pair<LocalTime, LocalTime>?) {
        var timeRange = ""
        range?.let {
            timeRange = "(${range.first}~${range.second})"
        }

        val pickBeginTime = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(appointment.getBeginTime().hour)
            .setMinute(appointment.getBeginTime().minute)
            .setTitleText("选择开始时间: $timeRange")
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .build()
        pickBeginTime.show(parentFragmentManager, "beginTimePicker")
        pickBeginTime.addOnPositiveButtonClickListener {
            val pickEndTime = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(appointment.getEndTime().hour)
                .setMinute(appointment.getEndTime().minute)
                .setTitleText("选择结束时间: $timeRange")
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build()
            pickEndTime.show(parentFragmentManager, "endTimePicker")
            pickEndTime.addOnPositiveButtonClickListener {
                val beginTime = LocalTime.of(pickBeginTime.hour, pickBeginTime.minute)
                val endTime = LocalTime.of(pickEndTime.hour, pickEndTime.minute)
                val date = appointment.getDate()
                val appointmentBeginTime = appointment.getBeginTime()
                val appointmentEndTime = appointment.getEndTime()
                val beginDateTime = LocalDateTime.of(date, beginTime)
                val endDateTime = LocalDateTime.of(date, endTime)

                // verify
                if ((range != null && beginTime.isBetween(
                        range.first,
                        range.second
                    ) && endTime.isBetween(beginTime, range.second)) ||
                    (range == null && beginTime.isBetween(
                        appointmentBeginTime,
                        appointmentEndTime
                    ) && endTime.isBetween(beginTime, appointmentEndTime))
                ) {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val request =
                                ModifyAppointmentRequest(
                                    appointment.id,
                                    beginDateTime.toString(),
                                    endDateTime.toString()
                                )
                            val res = appointmentService.modifyAppointment(request)
                            withContext(Dispatchers.Main) {
                                var msg = ""
                                when (res) {
                                    AddAppointmentResponse.SUCCESS -> {
                                        msg = "修改成功"
                                    }
                                    AddAppointmentResponse.FAIL -> {
                                        msg = "修改失败"
                                    }
                                    AddAppointmentResponse.CONFLICT -> {
                                        msg = "时间发生冲突了"
                                    }
                                }
                                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                                bookViewModel.getData()
                                updateAppointmentPopupWindow.dismiss()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Log.e(TAG, "修改是发生网络错误")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "网络异常", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "时间不合法,请填写范围内时间", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    private fun deletePile(appointment: Appointment) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val res = appointmentService.deleteAppointment(appointment.id)
                withContext(Dispatchers.Main) {
                    val msg: String
                    msg = when (res) {
                        "success" -> {
                            "删除成功"
                        }
                        else -> {
                            "删除失败"
                        }
                    }
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    bookViewModel.getData()
                    updateAppointmentPopupWindow.dismiss()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Log.e(TAG, "删除预约发生网络异常")
            }
        }
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

    companion object {
        const val CHOOSE_PILE_ID_RESULT_KEY = "CHOOSE_PILE_ID_RESULT_KEY"
        const val CHOOSE_PILE_ID_BUNDLE_KEY = "CHOOSE_PILE_ID_BUNDLE_KEY"
        const val TAG = "BookPileFragment"
    }
}