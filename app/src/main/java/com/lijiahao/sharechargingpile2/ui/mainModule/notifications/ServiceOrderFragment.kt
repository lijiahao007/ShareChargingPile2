package com.lijiahao.sharechargingpile2.ui.mainModule.notifications

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.lijiahao.sharechargingpile2.databinding.FragmentServiceOrderBinding
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.adapter.ServiceOrderAdapter
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.viemodel.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ServiceOrderFragment : Fragment() {

    private val binding: FragmentServiceOrderBinding by lazy {
        FragmentServiceOrderBinding.inflate(layoutInflater)
    }

    private val viewModel: OrderViewModel by activityViewModels()
    private lateinit var adapter:ServiceOrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.getData()
        initUI()
        return binding.root
    }

    private fun initUI() {
        adapter = ServiceOrderAdapter(parentFragment)
        binding.recyclerView.adapter = adapter
        viewModel.queryOrderResponse.observe(viewLifecycleOwner) {
            var serviceOrderList = it.serviceOrder.toList()
            serviceOrderList = serviceOrderList.sortedBy { pair ->
                pair.first.toInt()
            }
            adapter.setServiceOrderList(serviceOrderList, it.stationInfoMap)
        }
    }
}