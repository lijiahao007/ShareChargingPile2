package com.lijiahao.sharechargingpile2.ui.mainModule.notifications

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import com.lijiahao.sharechargingpile2.databinding.FragmentAppointmentListBinding
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.adapter.AppointmentAdapter
import com.lijiahao.sharechargingpile2.ui.mainModule.notifications.viemodel.AppointmentListViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppointmentListFragment : Fragment() {

    private val binding: FragmentAppointmentListBinding by lazy {
        FragmentAppointmentListBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: AppointmentAdapter

    private val viewModel: AppointmentListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (parentFragment is OrderListFragment) {
            initUI()
        }

        return binding.root
    }

    private fun initUI() {
        adapter = AppointmentAdapter(parentFragment as OrderListFragment)
        binding.recyclerView.adapter = adapter

        viewModel.appointmentInfoList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }
    }

}