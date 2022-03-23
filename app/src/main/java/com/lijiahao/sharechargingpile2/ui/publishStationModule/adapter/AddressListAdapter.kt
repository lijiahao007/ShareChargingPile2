package com.lijiahao.sharechargingpile2.ui.publishStationModule.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.services.geocoder.GeocodeAddress
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.lijiahao.sharechargingpile2.databinding.AddressListItemBinding

class AddressListAdapter(var curPos: LatLng, val aMap: AMap, val behavior: BottomSheetBehavior<View>) :
    ListAdapter<GeocodeAddress, AddressListAdapter.AddressViewHolder>(AddressDiffItemCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {

        return AddressViewHolder(
            AddressListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), this)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AddressViewHolder(
        val binding: AddressListItemBinding,
        private val adapter: AddressListAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(address: GeocodeAddress) {
            binding.tvAddress.text = address.formatAddress
            val addressLatLng = LatLng(address.latLonPoint.latitude, address.latLonPoint.longitude)
            binding.tvDistance.text =
                AMapUtils.calculateLineDistance(adapter.curPos, addressLatLng).toString()
            binding.addressItemLayout.setOnClickListener {
                adapter.aMap.animateCamera(CameraUpdateFactory.newLatLng(addressLatLng))
                adapter.behavior.state = BottomSheetBehavior.STATE_HIDDEN
            }

        }
    }

}