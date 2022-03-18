package com.lijiahao.sharechargingpile2.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.lijiahao.sharechargingpile2.databinding.PileListFragmentBinding
import com.lijiahao.sharechargingpile2.ui.viewmodel.PileListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PileListFragment : Fragment() {

    private val viewModel: PileListViewModel by viewModels()
    private val binding: PileListFragmentBinding by lazy {
        PileListFragmentBinding.inflate(layoutInflater)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding.btnNavigation.setOnClickListener {
            navigateToPileDetail(1)
        }

        return binding.root;
    }


    private fun navigateToPileDetail(pileId: Long) {
        val action = PileListFragmentDirections.actionPileListFragmentToPileDetailFragment(pileId)
        findNavController().navigate(action)
    }



}