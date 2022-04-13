package com.lijiahao.sharechargingpile2.ui.QRCodeModule

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.lijiahao.sharechargingpile2.data.OpenTime
import com.lijiahao.sharechargingpile2.databinding.FragmentPileUsingBinding
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.network.service.UserService
import com.lijiahao.sharechargingpile2.ui.view.OpenTimeWithChargeFeeLayout
import com.lijiahao.sharechargingpile2.utils.TimeUtils
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
import java.time.LocalDateTime
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

    private val viewModel: GenerateOrderViewModel by activityViewModels() {
        GenerateOrderViewModel.getGenerateOrderViewModelFactory(
            stationId,
            pileId,
            chargingPileStationService,
            userService
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.getData() // 刷新数据
        loadData()
        calTimeAndMoney()

        return binding.root
    }

    private fun loadData() {
        viewModel.stationInfo.observe(viewLifecycleOwner) { stationInfo ->
            binding.itStationName.text = stationInfo.station.name
            binding.tvStationParkFee.text = stationInfo.station.parkFee.toString()
            setOpenTime(stationInfo.openTimeList)
        }

        viewModel.pileInfo.observe(viewLifecycleOwner) { pile ->
            binding.tvPileState.text = pile.state
            binding.tvElectricType.text = pile.electricType
            binding.tvPowerRate.text = pile.powerRate.toString()
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
        val order = viewModel.order.value
        order?.let {
            binding.tvOrderCreateTime.text = TimeUtils.getFormatTimeStr(it.beginChargeTime)
            val beginChargeTime = LocalDateTime.parse(order.beginChargeTime)
            binding.tvBeginChargeTime.text = TimeUtils.getFormatTimeStr(beginChargeTime)

            val parkFee = viewModel.stationInfo.value?.station?.parkFee?.toDouble() ?: 0.0
            val openTimeList = viewModel.stationInfo.value?.openTimeList
            val powerRate = viewModel.pileInfo.value?.powerRate?:0f
            val chargeFee = openTimeList?.let { list ->
                list.sumOf { openTime ->
                    openTime.electricCharge.toDouble()
                } / openTimeList.size
            } ?: 0.0

            val moneyPerHour = parkFee + chargeFee * powerRate

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


            val timer = Timer()
            // 计算使用时间
            val calTimeTask = object : TimerTask() {
                override fun run() {
                    val duration = Duration.between(beginChargeTime, LocalDateTime.now())
                    val hour = String.format("%02d", duration.toHours())
                    val minute = String.format("%02d", duration.toMinutes())
                    val second = String.format("%02d", duration.toMillis() / 1000 % 60)
                    val time = "$hour:$minute:$second"
                    handler.obtainMessage(TIME_UPDATE, time).sendToTarget()
                }
            }
            // 计算预计金钱
            val calMoneyTask = object : TimerTask() {
                override fun run() {
                    val now = LocalDateTime.now()
                    val duration = Duration.between(beginChargeTime, now)
                    val minute = duration.toMinutes()
                    val hour = minute / 60.0
                    val money = hour * moneyPerHour
                    val moneyStr = String.format("%.2f", money)
                    handler.obtainMessage(MONEY_UPDATE, moneyStr).sendToTarget()
                    // TODO: 在完成对OpenTime的限制后（开始时间小于结束时间、为在营业时间的收费）完成分时间段收费的计算
                }
            }

            timer.schedule(calTimeTask, 0, 1000)
            timer.schedule(calMoneyTask, 0, 1000)
        }
    }

    private fun setOpenTime(openTimes: List<OpenTime>) {
        val linearLayout = binding.openTimeLinearLayout
        linearLayout.removeAllViews()
        context?.let {
            openTimes.forEach { openTime ->
                val content = OpenTimeWithChargeFeeLayout(it)
                content.setOpenTime(openTime)
                linearLayout.addView(content)
            }
        }

    }

    companion object {
        const val TAG = "PileUsingFragment"
        const val TIME_UPDATE: Int = 10086
        const val MONEY_UPDATE: Int = 10087
    }

}