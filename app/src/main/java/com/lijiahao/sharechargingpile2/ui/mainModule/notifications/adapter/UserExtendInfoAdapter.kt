package com.lijiahao.sharechargingpile2.ui.mainModule.notifications.adapter

import android.text.InputType
import android.text.method.KeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.UserExtendInfo
import com.lijiahao.sharechargingpile2.databinding.ItemUserExtendInfoBinding

class UserExtendInfoAdapter :
    ListAdapter<UserExtendInfo, UserExtendInfoAdapter.UserExtendInfoViewHolder>(
        UserExtendInfoItemDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserExtendInfoViewHolder {
        return UserExtendInfoViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_user_extend_info,
                parent,
                false
            ), this
        )
    }

    override fun onBindViewHolder(holder: UserExtendInfoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    class UserExtendInfoViewHolder(val binding: ItemUserExtendInfoBinding, val adapter: UserExtendInfoAdapter) :
        RecyclerView.ViewHolder(binding.root) {
        val field = binding.field
        val value = binding.value
        val editor = binding.editor
        val finish = binding.finish
        val delete = binding.delete

        fun writeFieldModel() {
            field.keyListener = field.tag as KeyListener?
            writeValueModel()
        }

        fun writeValueModel() {
            value.keyListener = value.tag as KeyListener
            editor.visibility = View.GONE
            finish.visibility = View.VISIBLE
            delete.visibility = View.VISIBLE
        }

        fun readModel() {
            value.keyListener = null
            field.keyListener = null
            editor.visibility = View.VISIBLE
            finish.visibility = View.GONE
            delete.visibility = View.GONE
        }


        fun bind(info: UserExtendInfo) {


            field.setText(info.field)
            field.tag = field.keyListener
            field.keyListener = null



            value.setText(info.value)
            value.tag = value.keyListener
            value.keyListener = null // 设置为不可编辑

            editor.setOnClickListener {
               writeValueModel()
            }



            finish.setOnClickListener {
                readModel()

                val list = ArrayList(adapter.currentList)
                var pos = -1;
                list.forEachIndexed { index, userExtendInfo ->
                    if (userExtendInfo.field == info.field) {
                        pos = index;
                        userExtendInfo.value = value.text.toString()
                        userExtendInfo.field = field.text.toString()
                        return@forEachIndexed
                    }
                }
                if (pos != -1) {
                    adapter.submitList(list)
                    adapter.notifyItemChanged(pos)
                }
            }


            delete.setOnClickListener {
                val list = ArrayList(adapter.currentList)
                var pos = -1;
                list.forEachIndexed { index, userExtendInfo ->
                    if (userExtendInfo.field == info.field) {
                        pos = index;
                        return@forEachIndexed
                    }
                }
                if (pos != -1) {
                    list.removeAt(pos)
                    adapter.submitList(list)
                    adapter.notifyItemRemoved(pos)
                }
            }

            if (field.text.toString() == "TODO") {
                writeFieldModel()
            }

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