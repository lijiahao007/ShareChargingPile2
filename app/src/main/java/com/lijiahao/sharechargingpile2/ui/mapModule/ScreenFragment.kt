package com.lijiahao.sharechargingpile2.ui.mapModule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.ChargingPile
import com.lijiahao.sharechargingpile2.databinding.FragmentScreenBinding
import com.lijiahao.sharechargingpile2.ui.mapModule.viewmodel.MapViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ScreenFragment : Fragment() {
    private val binding: FragmentScreenBinding by lazy {
        FragmentScreenBinding.inflate(layoutInflater)
    }

    val mapViewModel: MapViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initUI()
        return binding.root
    }

    private fun initUI() {

        val checkIds = mapViewModel.getConditionCheckId()
        checkIds.forEach {
            binding.condition.check(it)
        }

        binding.confirm.setOnClickListener {
            binding.condition.checkedChipIds.forEach { chipId ->
                when (chipId) {
                    R.id.chip_free -> {
                        mapViewModel.addCondition(Pair(R.id.chip_free) { stationListItemViewModel ->
                            val res = (stationListItemViewModel.piles.find { it.state == ChargingPile.STATE_FREE } != null)
                            res
                        })
                    }
                    R.id.chip_ac -> {
                        mapViewModel.addCondition(Pair(R.id.chip_ac) { stationListItemViewModel ->
                            val pile = stationListItemViewModel.piles.find { it.electricType == "交流" }
                            pile != null
                        })
                    }
                    R.id.chip_dc -> {
                        mapViewModel.addCondition(Pair(R.id.chip_dc) { stationListItemViewModel ->
                            val res = (stationListItemViewModel.piles.find { it.electricType == "直流" } != null)
                            res
                        })
                    }
                }
            }
            binding.condition.children.forEach { view ->
                val chip = view as Chip
                if (!chip.isChecked) {
                    mapViewModel.removeCondition(view.id)
                }
            }

            mapViewModel.projection.value = mapViewModel.projection.value

            viewLifecycleOwner.lifecycleScope.launch {
                Snackbar.make(binding.root, "筛选成功", Snackbar.LENGTH_SHORT).show()
                delay(500)
                findNavController().navigateUp()
            }
        }

    }

}