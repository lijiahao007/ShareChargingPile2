package com.lijiahao.sharechargingpile2.ui.mapModule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.lijiahao.sharechargingpile2.databinding.FragmentOpenTimeBinding
import com.lijiahao.sharechargingpile2.ui.mapModule.adapter.OpenTimeListAdapter
import com.lijiahao.sharechargingpile2.ui.mapModule.viewmodel.MapViewModel


class OpenTimeFragment : Fragment() {

    private val binding:FragmentOpenTimeBinding by lazy {
        FragmentOpenTimeBinding.inflate(layoutInflater)
    }
    private val adapter: OpenTimeListAdapter = OpenTimeListAdapter()
    private val mapViewModel: MapViewModel by activityViewModels()

    private val args: StationDetailFragmentArgs by navArgs()
    private val stationId: Int by lazy {
        args.stationId
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding.openTimeRecyclerview.adapter = adapter
        val timeList = mapViewModel.stationOpenTimeMap[stationId.toString()]
        adapter.submitList(timeList)
        return binding.root
    }


}