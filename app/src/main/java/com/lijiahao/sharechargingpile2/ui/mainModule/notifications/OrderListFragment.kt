package com.lijiahao.sharechargingpile2.ui.mainModule.notifications

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.lijiahao.sharechargingpile2.data.Order
import com.lijiahao.sharechargingpile2.databinding.FragmentOrderListBinding
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.adapter.OrderAdapter
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.adapter.OrderListViewPaperAdapter


class OrderListFragment : Fragment() {

    private val binding: FragmentOrderListBinding by lazy {
        FragmentOrderListBinding.inflate(layoutInflater)
    }

    private val viewModel: OrderViewModel by activityViewModels()

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
        val textMap = mapOf<Int, String>(
            OrderListViewPaperAdapter.MY_ORDER_FRAGMENT_INDEX to "使用订单",
            OrderListViewPaperAdapter.SERVICE_ORDER_FRAGMENT_INDEX to "服务订单"
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