package com.lijiahao.sharechargingpile2.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.lijiahao.sharechargingpile2.data.ChargingPileStation
import com.lijiahao.sharechargingpile2.databinding.FragmentStationDetailBinding
import com.lijiahao.sharechargingpile2.di.GlideApp
import com.lijiahao.sharechargingpile2.network.service.ChargingPileStationService
import com.lijiahao.sharechargingpile2.ui.viewmodel.MapViewModel
import com.lijiahao.sharechargingpile2.ui.viewmodel.StationDetailViewModel
import com.lijiahao.sharechargingpile2.utils.INVISIBLE
import com.lijiahao.sharechargingpile2.utils.VISIBLE
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
    private val pileId: Int by lazy {
        args.pileId
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
            navigationBack()
        }

        val viewModel = mapViewModel.stationInfoMap[pileId.toString()]
        viewModel?.let {
            binding.viewModel = viewModel
            binding.executePendingBindings()
        }

        loadImg()

        binding.cbCollection.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 添加收藏
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    chargingPileStationService.addStationCollection(pileId)
                }
            } else {
                // 删除收藏
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    chargingPileStationService.subtractStationCollection(pileId)
                }
            }
        }



    }


    private fun loadImg() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val urlList = chargingPileStationService.getStationPicUrl(pileId)
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

}