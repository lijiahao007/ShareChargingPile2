package com.lijiahao.sharechargingpile2.ui.chatModule.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lijiahao.sharechargingpile2.data.MessageListItem
import com.lijiahao.sharechargingpile2.data.MsgType
import com.lijiahao.sharechargingpile2.data.TextMsgBody
import com.lijiahao.sharechargingpile2.databinding.ItemMessageBinding
import com.lijiahao.sharechargingpile2.di.GlideApp

class MessageListAdapter():
    ListAdapter<MessageListItem, MessageListAdapter.MyViewHolder>(MessageItemDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding, this, parent.context)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    class MyViewHolder(val binding: ItemMessageBinding, private val adapter: MessageListAdapter, private val context:Context) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(messageListItem: MessageListItem) {
            // 加载用户图片
            GlideApp.with(context).load(messageListItem.user.avatarUrl).into(binding.messageItemPhoto)

            // 加载用户名
            binding.messageItemTitle.text = messageListItem.user.name

            // 加载信息
            val message = messageListItem.message
            when (message.msgType) {
                MsgType.TEXT -> {
                    val msgBody = message.msgBody as TextMsgBody
                    binding.messageItemInfo.text = msgBody.message
                }
                MsgType.IMAGE -> {
                    binding.messageItemInfo.text = "[图片]"
                }
            }

            // 加载时间
        }

    }

    class MessageItemDiffItemCallback : DiffUtil.ItemCallback<MessageListItem>() {

        override fun areItemsTheSame(oldItem: MessageListItem, newItem: MessageListItem): Boolean {
            return oldItem.user.userId == newItem.user.userId
        }

        override fun areContentsTheSame(oldItem: MessageListItem, newItem: MessageListItem): Boolean {
            return oldItem == newItem
        }

    }
}
