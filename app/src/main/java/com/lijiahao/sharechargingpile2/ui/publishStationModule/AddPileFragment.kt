package com.lijiahao.sharechargingpile2.ui.publishStationModule

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.ChargingPile
import com.lijiahao.sharechargingpile2.databinding.FragmentAddPileBinding
import com.lijiahao.sharechargingpile2.ui.publishStationModule.adapter.ChargingPileListAdapter
import com.lijiahao.sharechargingpile2.ui.publishStationModule.viewmodel.AddStationViewModel
import com.lijiahao.sharechargingpile2.utils.SoftKeyBoardUtils
import java.util.*
import kotlin.collections.ArrayList


class AddPileFragment : Fragment() {

    val binding: FragmentAddPileBinding by lazy {
        FragmentAddPileBinding.inflate(layoutInflater)
    }

    val viewModel: AddStationViewModel by activityViewModels()

    lateinit var adapter: ChargingPileListAdapter

    val stationId:Int by lazy {
        viewModel.stationId
    }

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
        adapter = ChargingPileListAdapter(this) // 每次开启都创建一个新的adapter
        binding.pileRecyclerview.adapter = adapter

        adapter.submitList(viewModel.pileList) // 如果是修改的，就将viewModel中的pile List添加到adapter中
        adapter.notifyDataSetChanged()

        binding.btnAdd.setOnClickListener {
            try {
                SoftKeyBoardUtils.hideKeyBoard(requireActivity())

                val electricType = binding.electricTypeSelector.editText?.text.toString()
                val pileNum = binding.pileNum.editText?.text.toString().toInt()
                val powerRate = binding.powerRate.editText?.text.toString().toFloat()

                val list = ArrayList(adapter.currentList)
                for (i in 0 until pileNum) {
                    list.add(0, ChargingPile(0, electricType, powerRate, stationId, UUID.randomUUID().toString(), null))
                }
                adapter.submitList(list)
                adapter.notifyItemRangeChanged(0, list.size)
                binding.pileRecyclerview.smoothScrollToPosition(0)
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                Snackbar.make(binding.root, "输入格式错误，添加失败", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.close.setOnClickListener {
            viewModel.pileList = ArrayList<ChargingPile>(adapter.currentList)
            setFragmentResult(
                AddStationFragment.ADD_PILE_TO_ADD_STATION_BUNDLE,
                bundleOf()
            )
            navigateUp()
        }

        binding.btnConfirm.setOnClickListener {
            viewModel.pileList = ArrayList<ChargingPile>(adapter.currentList)
            Log.i(TAG, "curList = ${viewModel.pileList}")
            setFragmentResult(
                AddStationFragment.ADD_PILE_TO_ADD_STATION_BUNDLE,
                bundleOf()
            )
        }
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }
    
    companion object {
        const val TAG = "AddPileFragment"
    }


}