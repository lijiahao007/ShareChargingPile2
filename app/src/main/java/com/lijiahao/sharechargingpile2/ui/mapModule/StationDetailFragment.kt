package com.lijiahao.sharechargingpile2.ui.mapModule

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.amap.api.maps.model.LatLng
import com.lijiahao.sharechargingpile2.databinding.FragmentStationDetailBinding
import com.lijiahao.sharechargingpile2.di.GlideApp
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.ui.mapModule.viewmodel.MapViewModel
import com.lijiahao.sharechargingpile2.ui.mapModule.viewmodel.StationDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class StationDetailFragment : Fragment() {

    private val args: StationDetailFragmentArgs by navArgs()
    private val binding: FragmentStationDetailBinding by lazy {
        FragmentStationDetailBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var chargingPileStationService: ChargingPileStationService

    private val viewModel: StationDetailViewModel by viewModels()
    private val mapViewModel: MapViewModel by activityViewModels()
    private val stationId: Int by lazy {
        args.stationId
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initUi()
        return binding.root
    }

    private fun initUi() {
        binding.ivBack.setOnClickListener {
            mapViewModel.finishNavi.value = true
            navigationBack()
        }

        val viewModel = mapViewModel.stationInfoMap[stationId.toString()]
        viewModel?.let {
            binding.viewModel = viewModel
            binding.executePendingBindings()
        }

        loadImg()

        binding.cbCollection.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 添加收藏
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    chargingPileStationService.addStationCollection(stationId)
                }
            } else {
                // 删除收藏
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    chargingPileStationService.subtractStationCollection(stationId)
                }
            }
        }

        binding.ivNavigation.setOnClickListener {
            Log.i(TAG, "发送了FragmentResult")
            var endPoint = LatLng(0.0, 0.0)
            mapViewModel.stationInfoMap[stationId.toString()]?.let {
                it.station.apply {
                    endPoint = LatLng(latitude, longitude)
                }
            }
            mapViewModel.naviEndPoint.value = endPoint
        }

        binding.ivToPileInfo.setOnClickListener {
            val action = StationDetailFragmentDirections.actionStationDetailFragmentToChargingPileListFragment(stationId)
            findNavController().navigate(action)
        }

        binding.ivToOpentimeInfo.setOnClickListener {
            val action = StationDetailFragmentDirections.actionStationDetailFragmentToOpenTimeFragment(stationId)
            findNavController().navigate(action)
        }
    }


    private fun loadImg() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val urlList = chargingPileStationService.getStationPicUrl(stationId)
            val urlNum = urlList.size
            withContext(Dispatchers.Main) {
                // 处理图片
                val ivPic1 = binding.ivStationPic1
                val ivPic2 = binding.ivStationPic2
                if (urlNum <= 0) {
                    ivPic1.visibility = View.GONE
                    ivPic2.visibility = View.GONE
                }
                if (urlNum >= 1) {
                    ivPic1.visibility = View.VISIBLE
                    context?.let {
                        GlideApp.with(it).load(urlList[0]).into(ivPic1)
                    }
                }
                if (urlNum >= 2) {
                    ivPic2.visibility = View.VISIBLE
                    context?.let {
                        GlideApp.with(it).load(urlList[1]).into(ivPic2)
                    }
                }

            }
        }
    }

    private fun navigationBack() {
        findNavController().navigateUp()
    }

    companion object {
        const val DETAIL_FRAGMENT_TO_MAP_ACTIVITY_NAVIGATION = "Navigation"
        const val STATION_ID = "stationId"
        const val TAG = "StationDetailFragment"
    }

}