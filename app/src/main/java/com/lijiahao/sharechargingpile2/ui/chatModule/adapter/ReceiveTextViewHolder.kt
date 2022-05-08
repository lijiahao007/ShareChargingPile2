package com.lijiahao.sharechargingpile2.ui.chatModule.adapter

import com.lijiahao.sharechargingpile2.data.Message
import com.lijiahao.sharechargingpile2.data.TextMsgBody
import com.lijiahao.sharechargingpile2.databinding.ItemTextReceiveBinding
import com.lijiahao.sharechargingpile2.di.GlideApp


class ReceiveTextViewHolder(
    val binding: ItemTextReceiveBinding,
    val avatarUrl: String
    ) :
    MessageViewHolder(binding.root) {
    override fun bind(message: Message) {
        val msgBody = message.msgBody as TextMsgBody
        binding.chatItemContentText.text = msgBody.message
        GlideApp.with(binding.root).load(avatarUrl).into(binding.chatItemHeader)
    }

}