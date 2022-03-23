package com.lijiahao.sharechargingpile2.ui.publishStationModule

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.room.util.UUIDUtil
import com.google.android.material.snackbar.Snackbar
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.ChargingPile
import com.lijiahao.sharechargingpile2.databinding.FragmentAddPileBinding
import com.lijiahao.sharechargingpile2.ui.publishStationModule.adapter.ChargingPileListAdapter
import com.lijiahao.sharechargingpile2.ui.publishStationModule.viewmodel.AddStationViewModel
import com.lijiahao.sharechargingpile2.utils.SoftKeyBoardUtils
import java.lang.NumberFormatException
import java.util.*
import kotlin.collections.ArrayList


class AddPileFragment : Fragment() {

    val binding: FragmentAddPileBinding by lazy {
        FragmentAddPileBinding.inflate(layoutInflater)
    }

    val viewModel: AddStationViewModel by activityViewModels()

    val adapter: ChargingPileListAdapter = ChargingPileListAdapter()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initUI()


        return binding.root
    }

    private fun initUI() {
        val items = arrayOf("直流", "交流")

        val electricTypeAdapter = ArrayAdapter(requireContext(), R.layout.electric_type_item, items)
        (binding.electricTypeSelector.editText as? AutoCompleteTextView)?.setAdapter(
            electricTypeAdapter
        )

        binding.pileRecyclerview.adapter = adapter

        adapter.submitList(viewModel.pileList)

        binding.btnAdd.setOnClickListener {
            try {
                SoftKeyBoardUtils.hideKeyBoard(requireActivity())

                val electricType = binding.electricTypeSelector.editText?.text.toString()
                val pileNum = binding.pileNum.editText?.text.toString().toInt()
                val powerRate = binding.powerRate.editText?.text.toString().toFloat()

                var list = ArrayList<ChargingPile>()
                val listSize = adapter.currentList.size
                if (listSize != 0) {
                    list = ArrayList<ChargingPile>(adapter.currentList)
                }
                var flag = false;
                var pos = 0;
                for (i in 0 until list.size) {
                    if (list[i].electricType == electricType && list[i].powerRate == powerRate) {
                        list[i].pileNum += pileNum
                        flag = true
                        pos = i
                        Log.i("AddPileFragment", "$i , ${list[i].pileNum}")
                    }
                }
                if (!flag) {
                    list.add(
                        0,
                        ChargingPile(
                            0,
                            electricType,
                            powerRate,
                            pileNum,
                            0,
                            UUID.randomUUID()!!.toString()
                        )
                    )
                }
                adapter.submitList(list)
                adapter.notifyItemChanged(pos)
                binding.pileRecyclerview.smoothScrollToPosition(pos)
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                Snackbar.make(binding.root, "输入格式错误，添加失败", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.btnConfirm.setOnClickListener {
            viewModel.pileList = ArrayList<ChargingPile>(adapter.currentList)
            if (viewModel.pileList.size == 0) {
                setFragmentResult(
                    AddStationFragment.ADD_PILE_TO_ADD_STATION_BUNDLE,
                    bundleOf("IS_OK" to false)
                )
            } else {
                setFragmentResult(
                    AddStationFragment.ADD_PILE_TO_ADD_STATION_BUNDLE,
                    bundleOf("IS_OK" to true)
                )
            }
            navigateUp()
        }

    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }


}