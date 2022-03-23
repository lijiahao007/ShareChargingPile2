package com.lijiahao.sharechargingpile2.ui.mapModule.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lijiahao.sharechargingpile2.databinding.StationListItemBinding
import com.lijiahao.sharechargingpile2.ui.mapModule.StationListFragmentDirections
import com.lijiahao.sharechargingpile2.ui.mapModule.viewmodel.StationListItemViewModel

class StationListAdapter() :
    ListAdapter<StationListItemViewModel, StationListAdapter.StationItemViewHolder>(
        StationListItemViewModelDiffItemCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationItemViewHolder {
        return StationItemViewHolder(
            StationListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: StationItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    class StationItemViewHolder(private val binding: StationListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: StationListItemViewModel) {
            binding.viewModel = viewModel
            binding.executePendingBindings()
            binding.stationItemCardView.setOnClickListener {
                try {
                    val action = StationListFragmentDirections.actionStationListFragmentToStationDetailFragment(viewModel.stationId)
                    binding.stationItemCardView.findNavController().navigate(action)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.i("StationListAdapter", "从StationListItem跳转StationDetailFragment失败了")
                }

            }
        }
    }
}

