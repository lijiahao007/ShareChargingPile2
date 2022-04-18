package com.lijiahao.sharechargingpile2.ui.mapModule.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.ElectricChargePeriod
import com.lijiahao.sharechargingpile2.databinding.ItemElectricChargeInStationDetailBinding

class ElectricChargeAdapter: RecyclerView.Adapter<ElectricChargeAdapter.ElectricChargeViewHolder>() {

    private val list: ArrayList<ElectricChargePeriod> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<ElectricChargePeriod>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElectricChargeViewHolder {
        return ElectricChargeViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_electric_charge_in_station_detail,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ElectricChargeViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ElectricChargeViewHolder(val binding:ItemElectricChargeInStationDetailBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(electricChargePeriod: ElectricChargePeriod) {
            binding.tvPeriod.text = "${electricChargePeriod.beginTime}~${electricChargePeriod.endTime}"
            binding.tvElectricCharge.text = String.format("%.2f", electricChargePeriod.electricCharge)
        }
    }



}