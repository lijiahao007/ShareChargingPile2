package com.lijiahao.sharechargingpile2.ui.mainModule.notifications.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.ChargingPileStation
import com.lijiahao.sharechargingpile2.data.Order
import com.lijiahao.sharechargingpile2.databinding.ItemServiceOrderBinding

class ServiceOrderAdapter(val fragment: Fragment?=null) : RecyclerView.Adapter<ServiceOrderAdapter.ServiceOrderViewHolder>() {

    private val stationOrderList = ArrayList<Pair<ChargingPileStation, List<Order>>>()

    @SuppressLint("NotifyDataSetChanged")
    fun setServiceOrderList(stationPileOrderMap: List<Pair<String, Map<String, List<Order>>>>, stationMap: Map<String, ChargingPileStation>) {
        stationOrderList.clear()
        stationPileOrderMap.forEach { (stationId, pileOrderMap) ->
            val station = stationMap[stationId]
            val orderList = ArrayList<Order>()
            pileOrderMap.forEach { (_, orders) ->
                orderList.addAll(orders)
            }
            station?.let {
                stationOrderList.add(Pair(station, orderList))
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceOrderViewHolder {
        return ServiceOrderViewHolder(
            fragment,
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_service_order,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ServiceOrderViewHolder, position: Int) {
        holder.bind(stationOrderList[position])
    }

    override fun getItemCount(): Int {
        return stationOrderList.size
    }

    class ServiceOrderViewHolder(
        val fragment: Fragment?,
        val binding: ItemServiceOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pair: Pair<ChargingPileStation, List<Order>>) {
            val station = pair.first
            val orders = pair.second
            val stationOrderExpandView = binding.expandView

            // 将订单转换为各种数据
            val pileIdSet = HashSet<String>()
            val processingOrderList = ArrayList<Order>()
            val finishOrderList = ArrayList<Order>()
            orders.forEach {
                pileIdSet.add(it.pileId)
                if (it.state == Order.STATE_USING) {
                    processingOrderList.add(it)
                } else {
                    finishOrderList.add(it)
                }
            }
            val pileIds = pileIdSet.toList()

            stationOrderExpandView.setData(station, pileIds, processingOrderList, finishOrderList, fragment)
        }
    }

}