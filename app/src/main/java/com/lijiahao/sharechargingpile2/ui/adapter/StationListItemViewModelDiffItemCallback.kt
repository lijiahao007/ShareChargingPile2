package com.lijiahao.sharechargingpile2.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.lijiahao.sharechargingpile2.ui.viewmodel.StationListItemViewModel

class StationListItemViewModelDiffItemCallback: DiffUtil.ItemCallback<StationListItemViewModel>() {
    override fun areItemsTheSame(
        oldItem: StationListItemViewModel,
        newItem: StationListItemViewModel
    ): Boolean {
        return oldItem.stationId == newItem.stationId
    }

    override fun areContentsTheSame(
        oldItem: StationListItemViewModel,
        newItem: StationListItemViewModel
    ): Boolean {
        return oldItem == newItem
    }

}