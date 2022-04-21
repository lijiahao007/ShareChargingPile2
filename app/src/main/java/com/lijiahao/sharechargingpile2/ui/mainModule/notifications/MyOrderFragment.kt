package com.lijiahao.sharechargingpile2.ui.mainModule.notifications

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.lijiahao.sharechargingpile2.databinding.FragmentMyOrderBinding
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.network.service.UserService
import com.lijiahao.sharechargingpile2.ui.QRCodeModule.GenerateOrderViewModel
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.adapter.OrderAdapter
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.viemodel.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class MyOrderFragment : Fragment() {

    private val binding: FragmentMyOrderBinding by lazy {
        FragmentMyOrderBinding.inflate(layoutInflater)
    }

    lateinit var adapter: OrderAdapter

    private val viewModel: OrderViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        adapter = OrderAdapter()
        binding.orderRecyclerview.adapter = adapter
        viewModel.getData()
        // 等收到数据之后显示出来
        viewModel.queryOrderResponse.observe(viewLifecycleOwner) {
            val processOrders = it.processingOrder
            val finishOrders = it.finishOrder
            val relateStationInfo = it.stationInfoMap
            val pileStationMap = it.pileStationMap

            val sortedProcessOrder = processOrders.sortedWith { o1, o2 ->
                val o1CreateTime = LocalDateTime.parse(o1.createTime)
                val o2CreateTime = LocalDateTime.parse(o2.createTime)
                when {
                    o1CreateTime.isBefore(o2CreateTime) -> {
                        1
                    }
                    o1CreateTime == o2CreateTime -> {
                        0
                    }
                    else -> {
                        -1
                    }
                }
            }

            val sortedFinishOrder = finishOrders.sortedWith { o1, o2 ->
                val o1CreateTime = LocalDateTime.parse(o1.createTime)
                val o2CreateTime = LocalDateTime.parse(o2.createTime)
                if (o1CreateTime.isBefore(o2CreateTime)) {
                    1
                } else if (o1CreateTime == o2CreateTime) {
                    0
                } else {
                    -1
                }
            }

            adapter.setInitData(
                pileStationMap,
                relateStationInfo,
                sortedProcessOrder,
                sortedFinishOrder,
                parentFragment
            )
        }
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        const val TAG = "MyOrderFragment"
    }
}