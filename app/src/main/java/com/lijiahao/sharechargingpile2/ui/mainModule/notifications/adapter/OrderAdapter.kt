package com.lijiahao.sharechargingpile2.ui.mainModule.notifications.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.Order
import com.lijiahao.sharechargingpile2.databinding.ItemOrderDividerBinding
import com.lijiahao.sharechargingpile2.databinding.ItemOrderListBinding

class OrderAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 下面两个列表的开头都是分割线
    private val processingOrderList = ArrayList<Order>() // 0 ~ processingOrderList.size - 1
    private val finishOrderList = ArrayList<Order>() // processingOrderList.size ~ (processingOrderList.size+finishOrderList.size+1)

    init {
        // 用id存放分割线的信息
        processingOrderList.add(Order("进行中订单", "", "", "", "", "", 0f, "", "", ""))
        finishOrderList.add(Order("历史订单", "", "", "", "", "", 0f, "", "", ""))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == DIVIDER_TYPE) {
            DividerViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_order_divider,
                    parent,
                    false
                )
            )
        } else {
            OrderViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_order_list,
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 || position == processingOrderList.size) {
            DIVIDER_TYPE
        } else {
            ORDER_TYPE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is OrderViewHolder) {
            holder.bind(getItem(position))
        } else if (holder is DividerViewHolder) {
            holder.bind(getItem(position).id)
        }
    }

    fun getItem(position: Int): Order {
        val processSize = processingOrderList.size
        return if (position >= 0 && position <= processSize - 1) {
            processingOrderList[position]
        } else {
            finishOrderList[position - processSize]
        }
    }

    override fun getItemCount() = finishOrderList.size + processingOrderList.size

    @SuppressLint("NotifyDataSetChanged")
    fun setProcessingOrder(orders:List<Order>) {
        val divider = processingOrderList[0]
        processingOrderList.clear()
        processingOrderList.add(divider)
        processingOrderList.addAll(orders)
        notifyDataSetChanged()
    }


    @SuppressLint("NotifyDataSetChanged")
    fun setFinishOrder(orders:List<Order>) {
        val divider = finishOrderList[0]
        finishOrderList.clear()
        finishOrderList.add(divider)
        finishOrderList.addAll(orders)
        notifyDataSetChanged()
    }


    class OrderViewHolder(val binding: ItemOrderListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            binding.tvStationName.text = order.pileId
            binding.tvState.text = order.state
            binding.tvCreateTime.text = order.createTime
        }
    }

    class DividerViewHolder(val binding: ItemOrderDividerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: String) {
            binding.dividerMessage.text = message
        }
    }


    companion object {
        const val DIVIDER_TYPE = 1
        const val ORDER_TYPE = 2
    }


}