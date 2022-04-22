package com.lijiahao.sharechargingpile2.ui.mainModule.notifications

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.ChargingPileStation
import com.lijiahao.sharechargingpile2.data.OpenTime
import com.lijiahao.sharechargingpile2.databinding.FragmentOrderDetailBinding
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.network.service.UserService
import com.lijiahao.sharechargingpile2.ui.QRCodeModule.GenerateOrderViewModel
import com.lijiahao.sharechargingpile2.ui.QRCodeModule.OrderPayFragmentArgs
import com.lijiahao.sharechargingpile2.ui.view.OpenTimeWithChargeFeeLayout
import com.lijiahao.sharechargingpile2.utils.TimeUtils
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class OrderDetailFragment : Fragment() {

    private val binding:FragmentOrderDetailBinding by lazy {
        FragmentOrderDetailBinding.inflate(layoutInflater)
    }

    private val args: OrderPayFragmentArgs by navArgs()
    private val stationId: String by lazy { args.stationId }
    private val pileId: String by lazy { args.pileId }
    @Inject lateinit var chargingPileStationService:ChargingPileStationService
    @Inject lateinit var userService:UserService

    private val viewModel: GenerateOrderViewModel by activityViewModels() {
        GenerateOrderViewModel.getGenerateOrderViewModelFactory(stationId, pileId, chargingPileStationService, userService)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.getData()
        loadData()
        initUIListener()
        return binding.root
    }

    private fun initUIListener() {
        binding.close.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnComment.setOnClickListener {
            // 跳转评论界面
            val action = OrderDetailFragmentDirections.actionOrderDetailFragmentToCommentFragment(stationId, pileId)
            findNavController().navigate(action)
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
            val curUsedTime = String.format("%02d", duration.toHours()) + ":" + String.format("%02d", duration.toMinutes()) + ":" + String.format("%02d", duration.toMillis()/1000)
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

}