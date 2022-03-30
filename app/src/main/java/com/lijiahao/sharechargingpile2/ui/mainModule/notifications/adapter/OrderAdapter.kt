package com.lijiahao.sharechargingpile2.ui.mainModule.notifications.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.Order
import com.lijiahao.sharechargingpile2.databinding.ItemOrderListBinding

class OrderAdapter:ListAdapter<Order, OrderAdapter.OrderViewHolder>(OrderDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_order_list,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    class OrderViewHolder(val binding: ItemOrderListBinding):RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            binding.tvStationName.text = order.stationId
            binding.tvState.text = order.state
            binding.tvCreateTime.text = order.createTime
        }
    }

    class OrderDiffItemCallback:DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }

    }



}