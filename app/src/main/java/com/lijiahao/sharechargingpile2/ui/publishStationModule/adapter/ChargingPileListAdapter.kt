package com.lijiahao.sharechargingpile2.ui.publishStationModule.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.lijiahao.sharechargingpile2.data.ChargingPile
import com.lijiahao.sharechargingpile2.databinding.PileListItemForAddPileBinding
import com.lijiahao.sharechargingpile2.ui.publishStationModule.AddPileFragment
import com.lijiahao.sharechargingpile2.ui.publishStationModule.AddPileFragmentDirections

class ChargingPileListAdapter(val fragment: AddPileFragment):ListAdapter<ChargingPile, ChargingPileListAdapter.MyViewHolder>(ChargingPileDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = PileListItemForAddPileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding, this)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.binding.pileItem.setOnClickListener {
            val chargingPile = getItem(position)
            if (chargingPile.qrcodeUrl != null) {
                val action = AddPileFragmentDirections.actionAddPileFragmentToPileQRCodeGenerateFragment(chargingPile.qrcodeUrl)
                fragment.findNavController().navigate(action)
            } else {
                Snackbar.make(holder.binding.pileItem, "上传该新充电桩后，获取二维码", Snackbar.LENGTH_SHORT).show()
            }
        }

    }

    class MyViewHolder(val binding: PileListItemForAddPileBinding, private val adapter: ChargingPileListAdapter): RecyclerView.ViewHolder(binding.root) {
        fun bind(chargingPile: ChargingPile) {
            val pileId = chargingPile.id.toString()
            if (pileId == "0") {
                binding.pileId.text = "new "
            } else {
                binding.pileId.text = "$pileId "
            }

            binding.electricType.text = chargingPile.electricType
            binding.powerRate1.text =  chargingPile.powerRate.toString()
            when (chargingPile.state) {
                "使用中" -> {binding.pileState.text = "使用中"}
                "暂停使用" -> {binding.pileState.text = "暂停使用"}
                else -> {binding.pileState.text = "空闲"}
            }
            binding.imClose.setOnClickListener {
                val list = ArrayList<ChargingPile>(adapter.currentList)
                list.removeIf {
                    var flag = false
                    if (it.id == 0 && it.state == chargingPile.state) {
                        flag = true
                    } else if (it.id != 0 && it.id == chargingPile.id) {
                        flag = true
                    }
                    flag
                }
                adapter.submitList(list)
            }

        }

    }

    class ChargingPileDiffItemCallback:DiffUtil.ItemCallback<ChargingPile>() {
        override fun areItemsTheSame(oldItem: ChargingPile, newItem: ChargingPile): Boolean {
            return oldItem.id == newItem.id && oldItem.state == newItem.state
        }

        override fun areContentsTheSame(oldItem: ChargingPile, newItem: ChargingPile): Boolean {
            return oldItem == newItem
        }
    }
}

