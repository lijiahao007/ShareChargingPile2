package com.lijiahao.sharechargingpile2.ui.mapModule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.databinding.FragmentElectricChargeBinding
import com.lijiahao.sharechargingpile2.ui.mapModule.adapter.ElectricChargeAdapter
import com.lijiahao.sharechargingpile2.ui.mapModule.viewmodel.MapViewModel

class ElectricChargeFragment : Fragment() {

    private val binding:FragmentElectricChargeBinding by lazy {
        FragmentElectricChargeBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: ElectricChargeAdapter
    private val mapViewModel: MapViewModel by activityViewModels()

    private val args: ElectricChargeFragmentArgs by navArgs()
    private val stationId:Int by lazy {
        args.stationId
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        adapter = ElectricChargeAdapter()
        binding.chargeRecyclerView.adapter = adapter
        val electricChargePeriods = mapViewModel.stationElectricChargePeriodMap[stationId.toString()]
        electricChargePeriods?.let {
            adapter.submitList(it)
        }
        return binding.root
    }


}