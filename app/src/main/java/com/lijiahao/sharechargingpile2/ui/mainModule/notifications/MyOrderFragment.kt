package com.lijiahao.sharechargingpile2.ui.mainModule.notifications

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.databinding.FragmentMyOrderBinding
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.adapter.OrderAdapter


class MyOrderFragment : Fragment() {

    private val binding:FragmentMyOrderBinding by lazy {
        FragmentMyOrderBinding.inflate(layoutInflater)
    }

    lateinit var adapter: OrderAdapter

    private val viewModel:OrderViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        adapter = OrderAdapter()
        binding.orderRecyclerview.adapter = adapter
        viewModel.processingOrder.observe(viewLifecycleOwner) {
            adapter.setProcessingOrder(it)
        }

        viewModel.finishOrder.observe(viewLifecycleOwner) {
            adapter.setFinishOrder(it)
        }
        return binding.root
    }

    companion object {
        const val TAG = "MyOrderFragment"
    }
}