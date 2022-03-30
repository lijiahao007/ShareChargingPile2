package com.lijiahao.sharechargingpile2.ui.mainModule.notifications

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.lijiahao.sharechargingpile2.data.Order
import com.lijiahao.sharechargingpile2.databinding.FragmentOrderListBinding
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.adapter.OrderAdapter


class OrderListFragment : Fragment() {

    private val binding: FragmentOrderListBinding by lazy {
        FragmentOrderListBinding.inflate(layoutInflater)
    }

    private val adapter = OrderAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        initUI()
        return binding.root
    }

    private fun initUI() {

        binding.close.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.orderRecyclerview.adapter = adapter
        val list = ArrayList<Order>()
        list.apply {
            add(Order("1", "16:56", "已完成", "熊本熊的充电桩"))
            add(Order("2", "19:32", "进行中", "唐老鸭的充电桩"))
        }
        adapter.submitList(list)



    }
}