package com.lijiahao.sharechargingpile2.ui.publishStationModule

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.databinding.FragmentStationManagerBinding
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.repository.ChargingPileStationRepository
import com.lijiahao.sharechargingpile2.ui.publishStationModule.adapter.StationListAdapter
import com.lijiahao.sharechargingpile2.ui.publishStationModule.viewmodel.StationManagerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StationManagerFragment : Fragment() {

    private val binding: FragmentStationManagerBinding by lazy {
        FragmentStationManagerBinding.inflate(layoutInflater)
    }

    private val viewModel: StationManagerViewModel by activityViewModels()
    private val adapter = StationListAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initUI()
        viewModel.getData()
        return binding.root
    }

    private fun initUI() {
        binding.addStation.setOnClickListener {
            val action =
                StationManagerFragmentDirections.actionStationManagerFragmentToAddStationFragment()
            findNavController().navigate(action)
        }

        binding.stationRecyclerview.adapter = adapter
        viewModel.userStationInfo.observe(this) {
            adapter.submitList(it.stations)
        }

        // TODO: 充电桩删除功能

    }

    companion object {
        const val TAG = "StationManagerFragment"
        const val CHANGE_STATION = "ChangeStation"
        const val IS_CHANGE = "IsChange"
        const val STATION_ID = "StationId"
    }
}