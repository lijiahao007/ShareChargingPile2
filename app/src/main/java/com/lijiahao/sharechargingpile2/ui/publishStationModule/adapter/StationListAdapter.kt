package com.lijiahao.sharechargingpile2.ui.publishStationModule.adapter

import android.content.Context
import android.nfc.Tag
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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

    var deleteMode: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        return StationViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_station_manager,
                parent,
                false
            ), this
        )
    }

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        holder.bind(getItem(position))

        holder.binding.itemStationLayout.setOnClickListener {
            val action =
                StationManagerFragmentDirections.actionStationManagerFragmentToModifyStationFragment(getItem(position).id.toString())
            fragment.findNavController().navigate(action)
        }
        if (deleteMode) {
            holder.binding.btnDelete.visibility = View.VISIBLE
        } else {
            holder.binding.btnDelete.visibility = View.GONE
        }
    }

    class StationViewHolder(val binding: ItemStationManagerBinding, val adapter:StationListAdapter) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(station: ChargingPileStation) {
            binding.tvStationNameManager.text = station.name
            binding.tvLocation.text = station.posDescription

            binding.btnDelete.setOnClickListener {
                val list = ArrayList(adapter.currentList)
                list.removeIf {
                    it.id == station.id
                }
                adapter.submitList(list)
            }
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

    companion object {
        const val TAG = "StationListAdapter"
    }
}