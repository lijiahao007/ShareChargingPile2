package com.lijiahao.sharechargingpile2.ui.mapModule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.ChargingPile
import com.lijiahao.sharechargingpile2.databinding.FragmentChargingPileListBinding
import com.lijiahao.sharechargingpile2.ui.mapModule.adapter.ChargingPileListAdapter
import com.lijiahao.sharechargingpile2.ui.mapModule.viewmodel.MapViewModel

class ChargingPileListFragment : Fragment() {

    private val binding: FragmentChargingPileListBinding by lazy {
        FragmentChargingPileListBinding.inflate(layoutInflater)
    }

    private val mapViewModel: MapViewModel by activityViewModels()

    private val adapter = ChargingPileListAdapter()
    private val args: StationDetailFragmentArgs by navArgs()
    private val stationId: Int by lazy {
        args.stationId
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        initUI()
        return binding.root
    }

    private fun initUI() {
        binding.pileRecyclerview.adapter = adapter
        val piles = mapViewModel.stationPileMap[stationId.toString()]
        val singlePiles =
            ArrayList<ChargingPile>() // 每个ChargingPile分来，例如2个直流12kw/h 变成 1个直流12kw/h, 1个直流12kw/h
        piles?.forEach {
            val pile = ChargingPile(it.id, it.electricType, it.powerRate, it.stationId, it.state, null)
            singlePiles.add(pile)
        }
        adapter.submitList(singlePiles)
    }

}