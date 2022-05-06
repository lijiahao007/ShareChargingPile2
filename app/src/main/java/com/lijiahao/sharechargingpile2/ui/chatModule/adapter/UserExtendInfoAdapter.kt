package com.lijiahao.sharechargingpile2.ui.chatModule.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.UserExtendInfo
import com.lijiahao.sharechargingpile2.databinding.ItemUserExtendInfoOnlyReadBinding

class UserExtendInfoAdapter :
    ListAdapter<UserExtendInfo, UserExtendInfoAdapter.UserExtendInfoViewHolder>(
        UserExtendInfoItemDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserExtendInfoViewHolder {
        return UserExtendInfoViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_user_extend_info_only_read,
                parent,
                false
            ), this
        )
    }

    override fun onBindViewHolder(holder: UserExtendInfoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    class UserExtendInfoViewHolder(val binding: ItemUserExtendInfoOnlyReadBinding, val adapter: UserExtendInfoAdapter) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(info : UserExtendInfo) {
            binding.field.text = info.field
            binding.value.text = info.value
        }
    }


    class UserExtendInfoItemDiffCallback : DiffUtil.ItemCallback<UserExtendInfo>() {
        override fun areItemsTheSame(oldItem: UserExtendInfo, newItem: UserExtendInfo): Boolean {
            return oldItem.field == newItem.field
        }

        override fun areContentsTheSame(oldItem: UserExtendInfo, newItem: UserExtendInfo): Boolean {
            return oldItem == newItem
        }

    }


}