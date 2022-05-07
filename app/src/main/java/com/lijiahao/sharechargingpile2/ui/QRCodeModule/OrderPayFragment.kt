package com.lijiahao.sharechargingpile2.ui.QRCodeModule

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.lijiahao.sharechargingpile2.data.OpenTime
import com.lijiahao.sharechargingpile2.databinding.FragmentOrderPayBinding
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.network.service.OrderService
import com.lijiahao.sharechargingpile2.network.service.UserService
import com.lijiahao.sharechargingpile2.ui.view.OpenTimeWithChargeFeeLayout
import com.lijiahao.sharechargingpile2.utils.TimeUtils
import com.lijiahao.sharechargingpile2.utils.TimeUtils.Companion.isBetween
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class OrderPayFragment : Fragment() {

    private val binding:FragmentOrderPayBinding by lazy {
        FragmentOrderPayBinding.inflate(layoutInflater)
    }

    private val args: OrderPayFragmentArgs by navArgs()
    private val stationId: String by lazy { args.stationId }
    private val pileId: String by lazy { args.pileId }

    @Inject
    lateinit var chargingPileStationService: ChargingPileStationService

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var orderService: OrderService


    private val viewModel:GenerateOrderViewModel by activityViewModels() {
        GenerateOrderViewModel.getGenerateOrderViewModelFactory(stationId, pileId, chargingPileStationService, userService)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.getData() // 刷新数据
        loadData()
        initUI()
        setBackPress()
        return binding.root
    }

    private fun setBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Log.e(TAG, "requireActivity().finish() invoke!!")
            requireActivity().finish()
        }
    }

    private fun initUI() {
        binding.btnPay.setOnClickListener {
            // 跳转微信支付。
            viewModel.order.value?.let {
                val price = it.price
                val action = OrderPayFragmentDirections.actionOrderPayFragmentToPayFragment(price, it.id)
                findNavController().navigate(action)
            }
        }

        binding.close.setOnClickListener {
            requireActivity().onBackPressed()
        }
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

            viewModel.stationInfo.value?.run {
                val appointments = appointmentList.filter { it.pileId == pile.id }
                val isBooked = appointments.find { LocalDateTime.now().isBetween(it.getBeginDateTime(), it.getEndDateTime()) } == null
                if (isBooked) {
                    binding.tvPileState.text = "被预约"
                }
            }
        }

        viewModel.userInfo.observe(viewLifecycleOwner) { userInfo ->
            binding.tvUserName.text = userInfo.name
            binding.tvUserPhone.text = userInfo.phone
        }

        viewModel.order.observe(viewLifecycleOwner) { order ->
            binding.tvOrderId.text = order.uuid.substring(0, 24)
            binding.tvOrderCreateTime.text = TimeUtils.getFormatTimeStr(order.createTime)
            binding.totalFee.text = order.price.toString()
            binding.tvBeginChargeTime.text = TimeUtils.getFormatTimeStr(order.beginChargeTime)
            val beginTime = LocalDateTime.parse(order.beginChargeTime)
            val completeTime = LocalDateTime.parse(order.completeTime)
            val duration = Duration.between(beginTime, completeTime)
            val curUsedTime = String.format("%02d", duration.toHours()) + ":" + String.format("%02d", duration.toMinutes()%60) + ":" + String.format("%02d", (duration.toMillis()/1000)%60)
            binding.curUsedTime.text = curUsedTime
            binding.totalFee.text = String.format("%.2f", order.price)
        }
    }

    private fun setOpenTime(openTimes: List<OpenTime>) {
        val linearLayout = binding.openTimeLinearLayout
        linearLayout.removeAllViews()
        context?.let {
            viewModel.stationInfo.value?.let { stationInfo ->
                openTimes.forEach { openTime ->
                    openTime.toElectricChargePeriods(stationInfo.chargePeriodList).forEach { period ->
                        val content = OpenTimeWithChargeFeeLayout(it)
                        content.setOpenTime(period)
                        linearLayout.addView(content)
                    }
                }
            }
        }
    }

    companion object {
        const val TAG: String = "OrderPayFragment"
    }

}