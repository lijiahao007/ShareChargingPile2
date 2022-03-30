package com.lijiahao.sharechargingpile2.ui.publishStationModule.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.ChargingPileStation
import com.lijiahao.sharechargingpile2.databinding.FragmentStationManagerBinding
import com.lijiahao.sharechargingpile2.databinding.ItemStationManagerBinding
import com.lijiahao.sharechargingpile2.ui.publishStationModule.StationManagerFragment
import com.lijiahao.sharechargingpile2.ui.publishStationModule.StationManagerFragmentDirections

class StationListAdapter(val fragment: Fragment) :
    ListAdapter<ChargingPileStation, StationListAdapter.StationViewHolder>(StationDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        return StationViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_station_manager,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.binding.itemStationLayout.setOnClickListener {
            val action =
                StationManagerFragmentDirections.actionStationManagerFragmentToAddStationFragment()
            // 点击跳转，并通过FragmentResult带上数据。
            fragment.setFragmentResult(
                StationManagerFragment.CHANGE_STATION,
                bundleOf(
                    StationManagerFragment.IS_CHANGE to true,
                    StationManagerFragment.STATION_ID to getItem(position).id
                )
            )
            fragment.findNavController().navigate(action)
        }
    }

    class StationViewHolder(val binding: ItemStationManagerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(station: ChargingPileStation) {
            binding.tvStationNameManager.text = station.name
            binding.tvLocation.text = station.posDescription
        }

    }

    class StationDiffItemCallback : DiffUtil.ItemCallback<ChargingPileStation>() {
        override fun areItemsTheSame(
            oldItem: ChargingPileStation,
            newItem: ChargingPileStation
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ChargingPileStation,
            newItem: ChargingPileStation
        ): Boolean {
            return oldItem == newItem
        }

    }
}