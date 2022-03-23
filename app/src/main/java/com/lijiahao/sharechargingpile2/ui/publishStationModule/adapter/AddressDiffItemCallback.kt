package com.lijiahao.sharechargingpile2.ui.publishStationModule.adapter

import androidx.recyclerview.widget.DiffUtil
import com.amap.api.services.geocoder.GeocodeAddress

class AddressDiffItemCallback: DiffUtil.ItemCallback<GeocodeAddress> (){
    override fun areItemsTheSame(oldItem: GeocodeAddress, newItem: GeocodeAddress): Boolean {
        return oldItem.latLonPoint == newItem.latLonPoint
    }

    override fun areContentsTheSame(oldItem: GeocodeAddress, newItem: GeocodeAddress): Boolean {
        return oldItem == newItem
    }
}