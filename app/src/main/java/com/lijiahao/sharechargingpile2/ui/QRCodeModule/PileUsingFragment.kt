package com.lijiahao.sharechargingpile2.ui.QRCodeModule

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.lijiahao.sharechargingpile2.data.ChargingPile
import com.lijiahao.sharechargingpile2.data.ElectricChargePeriod
import com.lijiahao.sharechargingpile2.data.OpenTime
import com.lijiahao.sharechargingpile2.databinding.FragmentPileUsingBinding
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.network.service.OrderService
import com.lijiahao.sharechargingpile2.network.service.UserService
import com.lijiahao.sharechargingpile2.ui.view.OpenTimeWithChargeFeeLayout
import com.lijiahao.sharechargingpile2.utils.TimeUtils
import com.lijiahao.sharechargingpile2.utils.TimeUtils.Companion.isBetween
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class PileUsingFragment : Fragment() {

    private val binding: FragmentPileUsingBinding by lazy {
        FragmentPileUsingBinding.inflate(layoutInflater)
    }

    private val args: PileUsingFragmentArgs by navArgs()
    private val stationId: String by lazy { args.stationId }
    private val pileId: String by lazy { args.pileId }

    @Inject
    lateinit var chargingPileStationService: ChargingPileStationService

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var orderService: OrderService

    private var timer:Timer? = null

    private val viewModel: GenerateOrderViewModel by activityViewModels() {
        GenerateOrderViewModel.getGenerateOrderViewModelFactory(
            stationId,
            pileId,
            chargingPileStationService,
            userService
        )
    }

    private val orderId: String by lazy {
        viewModel.order.value?.id.toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.getData() // 刷新数据
        loadData()
        initUI()
        return binding.root
    }

    private fun loadData() {
        viewModel.stationInfo.observe(viewLifecycleOwner) { stationInfo ->
            binding.itStationName.text = stationInfo.station.name
            binding.tvStationParkFee.text = stationInfo.station.parkFee.toString()
            setOpenTime(stationInfo.openTimeList)
            calTimeAndMoney() // 计算时间和花费
        }

        viewModel.pileInfo.observe(viewLifecycleOwner) { pile ->
            binding.tvPileState.text = pile.state
            binding.tvElectricType.text = pile.electricType
            binding.tvPowerRate.text = pile.powerRate.toString()

            if (viewModel.isPileNowAppointment(pile.id)) {
                binding.tvPileState.text = ChargingPile.STATE_APPOINTMENT
            }
        }

        viewModel.userInfo.observe(viewLifecycleOwner) { userInfo ->
            binding.tvUserName.text = userInfo.name
            binding.tvUserPhone.text = userInfo.phone
        }

        viewModel.order.observe(viewLifecycleOwner) { order ->
            binding.tvOrderId.text = order.uuid.substring(0, 24)
            binding.tvOrderCreateTime.text = TimeUtils.getFormatTimeStr(order.createTime)
        }

    }

    private fun calTimeAndMoney() {
        timer?.cancel()
        timer = Timer() // 创建新的timer
        val order = viewModel.order.value
        order?.let {
            binding.tvOrderCreateTime.text = TimeUtils.getFormatTimeStr(it.beginChargeTime)
            val beginChargeTime = LocalDateTime.parse(order.beginChargeTime)
            binding.tvBeginChargeTime.text = TimeUtils.getFormatTimeStr(beginChargeTime)

            val parkFee = viewModel.stationInfo.value?.station?.parkFee?.toDouble() ?: 0.0
            val powerRate = viewModel.pileInfo.value?.powerRate ?: 0f
            val electricChargePeriodList =
                viewModel.stationInfo.value?.chargePeriodList ?: ArrayList()


            val handler = object : Handler(Looper.getMainLooper()) {
                override fun handleMessage(msg: Message) {
                    when (msg.what) {
                        TIME_UPDATE -> {
                            val time = msg.obj as String
                            binding.curUsedTime.text = time
                        }
                        MONEY_UPDATE -> {
                            val money = msg.obj as String
                            binding.predictFee.text = money
                        }
                    }
                }
            }

            // 计算使用时间
            val calTimeTask = object : TimerTask() {
                override fun run() {
                    val duration = Duration.between(beginChargeTime, LocalDateTime.now())
                    val hour = String.format("%d", duration.toHours())
                    val minute = String.format("%02d", duration.toMinutes() % 60)
                    val second = String.format("%02d", (duration.toMillis() / 1000) % 60)
                    val time = "$hour:$minute:$second"
                    handler.obtainMessage(TIME_UPDATE, time).sendToTarget()
                }
            }
            // 计算预计金钱
            val calMoneyTask = object : TimerTask() {
                override fun run() {
                    val now = LocalDateTime.now()
                    val electricCharge = calChargingFee(
                        electricChargePeriodList,
                        beginChargeTime,
                        now,
                        powerRate.toDouble()
                    )
                    val duration = Duration.between(beginChargeTime, now)
                    val minute = duration.toMinutes()
                    val parkCharge = parkFee * minute / 60.0
                    val totalPrice = String.format("%.2f", electricCharge + parkCharge)
                    handler.obtainMessage(MONEY_UPDATE, totalPrice).sendToTarget()
                }
            }
            timer?.schedule(calTimeTask, 0, 1000)
            timer?.schedule(calMoneyTask, 0, 3000) // 3秒更新一次价格
        }
    }

    private fun initUI() {
        binding.btnFinish.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val finishOrderResponse = orderService.finishOrder(orderId)
                if (finishOrderResponse.code == "success") {
                    withContext(Dispatchers.Main) {
                        viewModel.setOrder(finishOrderResponse.order)
                        val action =
                            PileUsingFragmentDirections.actionPileUsingFragmentToOrderPayFragment(stationId, pileId)
                        findNavController().navigate(action)
                    }
                }
            }
        }

        binding.close.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    // 设置返回操作（返回主页）
    private fun setNavigateUpBehavior() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish() //结束当前Activity，回到MainActivity
        }
    }

    private fun setOpenTime(openTimes: List<OpenTime>) {
        val linearLayout = binding.openTimeLinearLayout
        linearLayout.removeAllViews()
        context?.let {
            viewModel.stationInfo.value?.let { stationInfo ->
                openTimes.forEach { openTime ->
                    openTime.toElectricChargePeriods(stationInfo.chargePeriodList)
                        .forEach { period ->
                            val content = OpenTimeWithChargeFeeLayout(it)
                            content.setOpenTime(period)
                            linearLayout.addView(content)
                        }
                }
            }
        }

    }


    /**
     * 计算同一天 beginTime ~ endTime 的充电费用
     * @param electricChargePeriod 充电桩营业时间
     * @param beginTime 充电开始时间
     * @param endTime   充电结束时间
     * @param powerRate 充电桩功率
     * @return
     */
    private fun calChargingFee(
        electricChargePeriod: List<ElectricChargePeriod>,
        beginTime: LocalTime,
        endTime: LocalTime,
        powerRate: Double
    ): Double {

        if (electricChargePeriod.isEmpty()) {
            Log.e(TAG, "PileUsingFragment.calChargingFee 中 electricChargePeriods.size == 0")
            return 0.0
        }

        var sumPrice = 0.0
        // 1. 订单在同一天开始和结束
        var beginIndex = 0 // 开始时间段
        var endIndex = 0 // 结束时间段
        for (i in electricChargePeriod.indices) {
            val curBeginTime = LocalTime.parse(electricChargePeriod[i].beginTime)
            val curEndTime = LocalTime.parse(electricChargePeriod[i].endTime)
            if (isBetween(beginTime, curBeginTime, curEndTime)) {
                beginIndex = i
            }
            if (isBetween(endTime, curBeginTime, curEndTime)) {
                endIndex = i
            }
        }

        if (beginIndex != endIndex) {
            // 1.1 计算第一个时间段的价格
            val firstEndTime = LocalTime.parse(electricChargePeriod[beginIndex].endTime)
            val firstCharge = electricChargePeriod[beginIndex].electricCharge
            val firstPeriod = Duration.between(beginTime, firstEndTime)
            val firstSecond = firstPeriod.toMillis() / 1000
            val firstPrice =
                (firstSecond / 3600.0) * powerRate * firstCharge // hour * kw/h * 元/kw = 该时间段的
            sumPrice += firstPrice

            // 1.2 计算最后一个时间段的价格
            val lastBeginTime = LocalTime.parse(electricChargePeriod[endIndex].beginTime)
            val lastCharge = electricChargePeriod[endIndex].electricCharge
            val lastPeriod = Duration.between(lastBeginTime, endTime)
            val lastSecond = lastPeriod.toMillis() / 1000
            val lastPrice = (lastSecond / 3600.0) * powerRate * lastCharge
            sumPrice += lastPrice

            // 1.3 计算中间时间段的价格
            for (i in beginIndex + 1 until endIndex) {
                val midBeginTime = LocalTime.parse(electricChargePeriod[i].beginTime)
                val midEndTime = LocalTime.parse(electricChargePeriod[i].endTime)
                val midPeriod = Duration.between(midBeginTime, midEndTime)
                val midSecond = midPeriod.toMillis() / 1000
                val midCharge = electricChargePeriod[i].electricCharge
                val midPrice = midSecond / 3600.0 * powerRate * midCharge
                sumPrice += midPrice
            }
        } else {
            val charge = electricChargePeriod[beginIndex].electricCharge
            val duration = Duration.between(beginTime, endTime)
            val second = duration.toMillis() / 1000
            val price = second / 3600.0 * powerRate * charge
            sumPrice += price
        }
        return sumPrice
    }

    /**
     * 计算从 beginDateTime, endDateTime 时间段内充电费用
     * @param electricChargePeriods 时间段收费情况
     * @param beginDateTime 开始时间
     * @param endDateTime 结束时间
     * @param powerRate 功率
     * @return 费用
     */
    private fun calChargingFee(
        electricChargePeriods: List<ElectricChargePeriod>,
        beginDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        powerRate: Double
    ): Double {

        if (electricChargePeriods.isEmpty()) {
            Log.e(TAG, "PileUsingFragment.calChargingFee 中 electricChargePeriods.size == 0")
            return 0.0
        }

        var sumPrice = 0.0
        val beginTime: LocalTime = beginDateTime.toLocalTime()
        val endTime: LocalTime = endDateTime.toLocalTime()
        if (beginDateTime.dayOfMonth == endDateTime.dayOfMonth) {
            sumPrice += calChargingFee(electricChargePeriods, beginTime, endTime, powerRate)
        } else {
            // 2. 订单不在同一天(前提是该充电站营业时间是连续的)
            // 2.1 计算第一天的价格
            sumPrice += calChargingFee(
                electricChargePeriods,
                beginTime,
                LocalTime.of(23, 59, 59),
                powerRate
            )

            // 2.2 计算最后一天的价格
            sumPrice += calChargingFee(
                electricChargePeriods,
                LocalTime.of(0, 0, 0),
                endTime,
                powerRate
            )

            // 2.3 计算中间天的价格
            val tmpBegin = LocalDateTime.of(
                beginDateTime.year,
                beginDateTime.month,
                beginDateTime.dayOfMonth,
                0,
                0,
                0
            )
            val tmpEnd = LocalDateTime.of(
                endDateTime.year,
                endDateTime.month,
                endDateTime.dayOfMonth,
                0,
                0,
                0
            )
            val duration = Duration.between(tmpBegin, tmpEnd)
            val days = duration.toDays() - 1
            for (i in 0 until days) {
                sumPrice += calChargingFee(
                    electricChargePeriods,
                    LocalTime.of(0, 0, 0),
                    LocalTime.of(23, 59, 59),
                    powerRate
                )
            }
        }
        return sumPrice
    }


    private fun isBetween(target: LocalTime, begin: LocalTime, end: LocalTime): Boolean {
        return (target == begin || target.isAfter(begin)) && (target == end || target.isBefore(end))
    }


    companion object {
        const val TAG = "PileUsingFragment"
        const val TIME_UPDATE: Int = 10086
        const val MONEY_UPDATE: Int = 10087
        const val SET_ORDER_FRAGMENT_RESULT = "SET_ORDER"
        const val ORDER_BUNDLE = "ORDER"
    }

}