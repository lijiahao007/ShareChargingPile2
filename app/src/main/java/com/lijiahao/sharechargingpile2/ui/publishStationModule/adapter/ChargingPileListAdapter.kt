package com.lijiahao.sharechargingpile2.ui.publishStationModule.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lijiahao.sharechargingpile2.data.ChargingPile
import com.lijiahao.sharechargingpile2.databinding.PileListItemForAddPileBinding

class ChargingPileListAdapter:ListAdapter<ChargingPile, ChargingPileListAdapter.MyViewHolder>(ChargingPileDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = PileListItemForAddPileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding, this)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    class MyViewHolder(val binding: PileListItemForAddPileBinding, private val adapter: ChargingPileListAdapter): RecyclerView.ViewHolder(binding.root) {
        fun bind(chargingPile: ChargingPile) {
            binding.electricType.text = chargingPile.electricType
            binding.pileNum1.text = chargingPile.pileNum.toString()
            binding.powerRate1.text =  chargingPile.powerRate.toString()
            binding.imClose.setOnClickListener {
                val list = ArrayList<ChargingPile>(adapter.currentList)
                list.removeIf {
                    it.state == chargingPile.state
                }
                adapter.submitList(list)
            }

        }

    }

    class ChargingPileDiffItemCallback:DiffUtil.ItemCallback<ChargingPile>() {
        override fun areItemsTheSame(oldItem: ChargingPile, newItem: ChargingPile): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ChargingPile, newItem: ChargingPile): Boolean {
            return oldItem == newItem
        }

    }
}

