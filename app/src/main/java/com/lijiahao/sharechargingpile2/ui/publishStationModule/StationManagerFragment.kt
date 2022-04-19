package com.lijiahao.sharechargingpile2.ui.publishStationModule

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.SharedPreferenceData
import com.lijiahao.sharechargingpile2.databinding.FragmentStationManagerBinding
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.repository.ChargingPileStationRepository
import com.lijiahao.sharechargingpile2.ui.publishStationModule.adapter.StationListAdapter
import com.lijiahao.sharechargingpile2.ui.publishStationModule.viewmodel.StationManagerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class StationManagerFragment : Fragment() {

    private val binding: FragmentStationManagerBinding by lazy {
        FragmentStationManagerBinding.inflate(layoutInflater)
    }

    private val viewModel: StationManagerViewModel by activityViewModels()
    private val adapter = StationListAdapter(this)

    @Inject
    lateinit var chargingPileStationService: ChargingPileStationService

    @Inject
    lateinit var sharedPreferenceData: SharedPreferenceData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initUI()
        viewModel.getData()
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
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
            val stationIds = ArrayList<Int>()
            adapter.currentList.forEach {
                stationIds.add(it.id)
            }
            Log.i(TAG, "remain stationIds = $stationIds")
            val userId = sharedPreferenceData.userId.toInt()
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val res = chargingPileStationService.uploadRemainStationIds(stationIds, userId)
                    if (res == "success") {
                        withContext(Dispatchers.Main) {
                            // 删除ViewModel中删除了的Station信息
                            val allStationInfo = viewModel.userStationInfo.value
                            allStationInfo?.deleteStationNotIn(stationIds)
                            Snackbar.make(binding.root, "修改成功", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        // 恢复列表
                        val allStationInfo = viewModel.userStationInfo.value
                        allStationInfo?.let {
                            adapter.submitList(it.stations)
                            adapter.notifyDataSetChanged()
                        }
                        Snackbar.make(binding.root, "网络异常", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.close.setOnClickListener {
            findNavController().navigateUp()
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
        const val TAG = "StationManagerFragemnt"
        const val STATION_ID = "StationId"
    }
}