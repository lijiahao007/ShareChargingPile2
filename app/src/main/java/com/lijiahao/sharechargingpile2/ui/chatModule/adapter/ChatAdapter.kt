package com.lijiahao.sharechargingpile2.ui.chatModule.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.lijiahao.sharechargingpile2.data.Message
import com.lijiahao.sharechargingpile2.data.MsgType
import com.lijiahao.sharechargingpile2.databinding.ItemImageReceiveBinding
import com.lijiahao.sharechargingpile2.databinding.ItemImageSendBinding
import com.lijiahao.sharechargingpile2.databinding.ItemTextReceiveBinding
import com.lijiahao.sharechargingpile2.databinding.ItemTextSendBinding
import com.lijiahao.sharechargingpile2.network.response.UserInfoResponse

class ChatAdapter(
    private val userId: String,
    private val curUserInfo: UserInfoResponse,
    private val targetInfo: UserInfoResponse
) :
    ListAdapter<Message, MessageViewHolder>(MessageDiffItemCallback()) {

    private val curUserAvatarUrl = curUserInfo.avatarUrl
    private val targetUserAvatarUrl = targetInfo.avatarUrl

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
                SendTextViewHolder(ItemTextSendBinding.inflate(LayoutInflater.from(parent.context)), curUserAvatarUrl)
            }
            TYPE_RECEIVE_TEXT -> {
                ReceiveTextViewHolder(ItemTextReceiveBinding.inflate(LayoutInflater.from(parent.context)), targetUserAvatarUrl)
            }
            TYPE_SEND_IMAGE -> {
                SendImageViewHolder(ItemImageSendBinding.inflate(LayoutInflater.from(parent.context)), curUserAvatarUrl)
            }
            TYPE_RECEIVE_IMAGE -> {
                ReceiveImageViewHolder(ItemImageReceiveBinding.inflate(LayoutInflater.from(parent.context)), targetUserAvatarUrl)
            }
            else -> {
                SendTextViewHolder(ItemTextSendBinding.inflate(LayoutInflater.from(parent.context)), curUserAvatarUrl)
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