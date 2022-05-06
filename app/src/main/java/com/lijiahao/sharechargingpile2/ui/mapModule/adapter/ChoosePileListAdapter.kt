package com.lijiahao.sharechargingpile2.ui.mapModule.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.ChargingPile
import com.lijiahao.sharechargingpile2.databinding.ItemPileInMapBinding
import com.lijiahao.sharechargingpile2.ui.mapModule.BookPileFragment
import com.lijiahao.sharechargingpile2.ui.mapModule.ChoosePileFragment

class ChoosePileListAdapter(
    val fragment: ChoosePileFragment
) : RecyclerView.Adapter<ChoosePileListAdapter.ChoosePileViewHolder>() {

    private val list = ArrayList<ChargingPile>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<ChargingPile>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoosePileViewHolder {
        return ChoosePileViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_pile_in_map,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ChoosePileViewHolder, position: Int) {
        holder.binding.pileLayout.setOnClickListener {
            fragment.setFragmentResult(
                BookPileFragment.CHOOSE_PILE_ID_RESULT_KEY,
                bundleOf(BookPileFragment.CHOOSE_PILE_ID_BUNDLE_KEY to getItem(position).id)
            )
            fragment.findNavController().navigateUp()
        }

        holder.bind(getItem(position))
    }

    private fun getItem(pos: Int): ChargingPile {
        return list[pos]
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ChoosePileViewHolder(val binding: ItemPileInMapBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chargingPile: ChargingPile) {
            binding.tvElectricType.text = chargingPile.electricType
            binding.tvPowerRate.text = chargingPile.powerRate.toString()
            binding.tvState1.text = chargingPile.state
        }
    }
}