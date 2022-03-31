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


        binding.deleteStation.setOnClickListener {
            deleteMode()
        }

        binding.ibtnConfirm.setOnClickListener {
            checkMode()
            // TODO: 上传删除逻辑，将除了当前列表以外的充电站从数据库中删除。
            val stationIds = ArrayList<Int>()
            adapter.currentList.forEach {
                stationIds.add(it.id)
            }
            Log.i(TAG, "remain stationIds = $stationIds")
        }

    }

    private fun deleteMode() {
        // 删除模式
        binding.deleteStation.visibility = View.GONE
        binding.addStation.visibility = View.GONE
        binding.ibtnConfirm.visibility = View.VISIBLE
        adapter.deleteMode = true
        adapter.notifyDataSetChanged()
    }

    private fun checkMode() {
        // 查看模式
        binding.deleteStation.visibility = View.VISIBLE
        binding.addStation.visibility = View.VISIBLE
        binding.ibtnConfirm.visibility = View.GONE
        adapter.deleteMode = false
        adapter.notifyDataSetChanged()
    }

    companion object {
        const val TAG = "StationManagerFragment"
        const val CHANGE_STATION = "ChangeStation"
        const val IS_CHANGE = "IsChange"
        const val STATION_ID = "StationId"
    }
}