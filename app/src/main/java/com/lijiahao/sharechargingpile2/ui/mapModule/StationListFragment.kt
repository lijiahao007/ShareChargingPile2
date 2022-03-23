package com.lijiahao.sharechargingpile2.ui.mapModule

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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

        binding.btnNavigation.setOnClickListener {
            navigateToPileDetail(1)
        }
        registerForContextMenu(binding.sortMenu)
        binding.sortMenu.setOnClickListener{
            it.performLongClick()
        }
    }

    private fun initRecyclerView() {
        binding.stationRecyclerview.adapter = adapter
        adapter.submitList(ArrayList<StationListItemViewModel>())

        mapViewModel.isReady.observe(viewLifecycleOwner) {
            val viewModelList = ArrayList<StationListItemViewModel>();
            mapViewModel.stationInfoMap.forEach{ (_, viewModel) ->
                val curPos = LatLng(viewModel.station.latitude, viewModel.station.longitude)
                val distance = AMapUtils.calculateLineDistance(curPos, mapViewModel.bluePointPos)
                viewModel.distance = distance
                viewModelList.add(viewModel)
            }
            adapter.submitList(viewModelList)
        }
    }


    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater? =  activity?.menuInflater
        inflater?.inflate(R.menu.station_list_sored_strategy, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.opt_dis -> {
                val list = adapter.currentList.sortedBy { it.distance }
                adapter.submitList(list)
                true
            }
            R.id.opt_score -> {
                TODO()
                true
            }
            R.id.opt_used_time -> {
                TODO()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun navigateToPileDetail(pileId: Int) {
        val action = StationListFragmentDirections.actionStationListFragmentToStationDetailFragment(pileId)
        findNavController().navigate(action)
    }


    companion object {
        const val TAG = "PILE_LIST_FRAGMENT"
    }


}