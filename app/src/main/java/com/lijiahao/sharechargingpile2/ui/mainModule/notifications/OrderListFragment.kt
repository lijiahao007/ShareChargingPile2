package com.lijiahao.sharechargingpile2.ui.mainModule.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.lijiahao.sharechargingpile2.databinding.FragmentOrderListBinding
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.network.service.UserService
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.adapter.OrderListViewPaperAdapter
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.viemodel.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OrderListFragment : Fragment() {

    private val binding: FragmentOrderListBinding by lazy {
        FragmentOrderListBinding.inflate(layoutInflater)
    }

    private val viewModel: OrderViewModel by activityViewModels()

    // 提供给子类使用
    @Inject lateinit var chargingPileStationService:ChargingPileStationService
    @Inject lateinit var userService:UserService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel
        initUI()
        return binding.root
    }

    private fun initUI() {
        // 1. 初始化 ViewPaper & TabLayout
        val viewPaper = binding.viewPaper
        val tabLayout = binding.tabLayout
        viewPaper.adapter = OrderListViewPaperAdapter(childFragmentManager, lifecycle)
        viewPaper.isSaveEnabled = false // 修复跳转返回后，ViewPaper中Fragment被销毁的问题
        val textMap = mapOf<Int, String>(
            OrderListViewPaperAdapter.MY_ORDER_FRAGMENT_INDEX to "使用订单",
            OrderListViewPaperAdapter.SERVICE_ORDER_FRAGMENT_INDEX to "服务订单",
            OrderListViewPaperAdapter.APPOINTMENT_LIST_FRAGMENT_INDEX to "预约"
        )
        TabLayoutMediator(tabLayout, viewPaper) { tab, position ->
            tab.text = textMap[position]
        }.attach()


        // 2. 设置返回按钮
        binding.close.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    companion object {
        const val TAG = "OrderListFragment"
    }
}