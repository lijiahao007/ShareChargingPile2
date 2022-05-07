package com.lijiahao.sharechargingpile2.ui.mapModule

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.databinding.FragmentStationListBinding
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.ui.mapModule.adapter.StationListAdapter
import com.lijiahao.sharechargingpile2.ui.mapModule.viewmodel.MapViewModel
import com.lijiahao.sharechargingpile2.ui.mapModule.viewmodel.StationListItemViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.notify
import javax.inject.Inject

@AndroidEntryPoint
class StationListFragment : Fragment() {

    private val mapViewModel: MapViewModel by activityViewModels()

    private val binding: FragmentStationListBinding by lazy {
        FragmentStationListBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var chargingPileStationService: ChargingPileStationService
    private val adapter = StationListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initUI()
        return binding.root;
    }

    private fun initUI() {
        initRecyclerView()

        registerForContextMenu(binding.sortMenu)
        binding.sortMenu.setOnClickListener {
            it.performLongClick()
        }

        binding.tvToScreen.setOnClickListener {
            val action = StationListFragmentDirections.actionStationListFragmentToScreenFragment()
            findNavController().navigate(action)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRecyclerView() {
        binding.stationRecyclerview.adapter = adapter

        adapter.submitList(ArrayList<StationListItemViewModel>())

        mapViewModel.stationInfoMapInProjection.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }


    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater? = activity?.menuInflater
        inflater?.inflate(R.menu.station_list_sored_strategy, menu)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.opt_dis -> {
                // 按距离
                val list = adapter.currentList.sortedBy { it.distance }
                adapter.submitList(list) {
                    binding.stationRecyclerview.smoothScrollToPosition(0)
                    binding.tvSortedWay.text = "按距离排序"
                }
                true
            }
            R.id.opt_score -> {
                // 按评分
                val list = adapter.currentList.sortedByDescending { it.station.score }
                adapter.submitList(list) {
                    binding.stationRecyclerview.smoothScrollToPosition(0)
                    binding.tvSortedWay.text = "按评分排序"
                }
                true
            }
            R.id.opt_used_time -> {
                // 按使用次数
                val list = adapter.currentList.sortedByDescending { it.station.usedTime }
                adapter.submitList(list) {
                    binding.stationRecyclerview.smoothScrollToPosition(0)
                    binding.tvSortedWay.text = "按使用次数排序"
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun navigateToPileDetail(pileId: Int) {
        val action =
            StationListFragmentDirections.actionStationListFragmentToStationDetailFragment(pileId)
        findNavController().navigate(action)
    }


    companion object {
        const val TAG = "PILE_LIST_FRAGMENT"
    }


}