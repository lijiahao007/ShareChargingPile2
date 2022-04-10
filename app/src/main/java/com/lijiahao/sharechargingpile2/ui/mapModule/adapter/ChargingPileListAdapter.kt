package com.lijiahao.sharechargingpile2.ui.mapModule.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lijiahao.sharechargingpile2.data.ChargingPile
import com.lijiahao.sharechargingpile2.databinding.ItemPileInMapBinding

class ChargingPileListAdapter:ListAdapter<ChargingPile, ChargingPileListAdapter.MyViewHolder>(ChargingPileDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemPileInMapBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding, this)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.binding.icNum.text = (position+1).toString()
    }


    class MyViewHolder(val binding: ItemPileInMapBinding, private val adapter: ChargingPileListAdapter): RecyclerView.ViewHolder(binding.root) {
        fun bind(chargingPile: ChargingPile) {
            binding.tvElectricType.text = chargingPile.electricType
            binding.tvPowerRate.text = chargingPile.powerRate.toString()
            binding.tvState1.text = chargingPile.state
        }
    }

    class ChargingPileDiffItemCallback: DiffUtil.ItemCallback<ChargingPile>() {
        override fun areItemsTheSame(oldItem: ChargingPile, newItem: ChargingPile): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChargingPile, newItem: ChargingPile): Boolean {
            return oldItem == newItem
        }
    }
}

