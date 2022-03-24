package com.lijiahao.sharechargingpile2.ui.ChatModule.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.Message
import com.lijiahao.sharechargingpile2.data.MsgType
import com.lijiahao.sharechargingpile2.databinding.ItemImageReceiveBinding
import com.lijiahao.sharechargingpile2.databinding.ItemImageSendBinding
import com.lijiahao.sharechargingpile2.databinding.ItemTextReceiveBinding
import com.lijiahao.sharechargingpile2.databinding.ItemTextSendBinding

class ChatAdapter(private val userId: String) :
    ListAdapter<Message, MessageViewHolder>(MessageDiffItemCallback()) {

    // 根据每个消息设置Item类型
    override fun getItemViewType(position: Int): Int {
        val msg = getItem(position)
        return when (msg.msgType) {
            MsgType.TEXT -> {
                if (msg.sendId == userId) TYPE_SEND_TEXT else TYPE_RECEIVE_TEXT
            }
            MsgType.IMAGE -> {
                if (msg.sendId == userId) TYPE_SEND_IMAGE else TYPE_RECEIVE_IMAGE
            }
        }
    }

    // 根据Item类型设置ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return when (viewType) {
            TYPE_SEND_TEXT -> {
                SendTextViewHolder(ItemTextSendBinding.inflate(LayoutInflater.from(parent.context)))
            }
            TYPE_RECEIVE_TEXT -> {
                ReceiveTextViewHolder(ItemTextReceiveBinding.inflate(LayoutInflater.from(parent.context)))
            }
            TYPE_SEND_IMAGE -> {
                SendImageViewHolder(ItemImageSendBinding.inflate(LayoutInflater.from(parent.context)))
            }
            TYPE_RECEIVE_IMAGE -> {
                ReceiveImageViewHolder(ItemImageReceiveBinding.inflate(LayoutInflater.from(parent.context)))
            }
            else -> {
                SendTextViewHolder(ItemTextSendBinding.inflate(LayoutInflater.from(parent.context)))
            }
        }

    }


    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    class MessageDiffItemCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.uuid == newItem.uuid
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }


    companion object {
        private const val TYPE_SEND_TEXT = 1
        private const val TYPE_RECEIVE_TEXT = 2
        private const val TYPE_SEND_IMAGE = 3
        private const val TYPE_RECEIVE_IMAGE = 4
    }

}