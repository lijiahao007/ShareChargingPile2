package com.lijiahao.sharechargingpile2.ui.mapModule.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lijiahao.sharechargingpile2.data.ChargingPile
import com.lijiahao.sharechargingpile2.data.OpenTime
import com.lijiahao.sharechargingpile2.databinding.ItemOpenTimeBinding
import com.lijiahao.sharechargingpile2.databinding.PileListItemForAddPileBinding

class OpenTimeListAdapter :
    ListAdapter<OpenTime, OpenTimeListAdapter.MyViewHolder>(OpenTimeDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemOpenTimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding, this)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    class MyViewHolder(val binding: ItemOpenTimeBinding, private val adapter: OpenTimeListAdapter) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(openTime: OpenTime) {
            val time = openTime.beginTime.substring(
                0,
                5
            ) + "~" + openTime.endTime.substring(0, 5)
            binding.tvTime.text = time
            binding.tvCharge.text = openTime.electricCharge.toString()
        }

    }

    class OpenTimeDiffItemCallback : DiffUtil.ItemCallback<OpenTime>() {
        override fun areItemsTheSame(oldItem: OpenTime, newItem: OpenTime): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: OpenTime, newItem: OpenTime): Boolean {
            return oldItem == newItem
        }

    }
}

