package com.lijiahao.sharechargingpile2.ui.chatModule.adapter

import com.lijiahao.sharechargingpile2.data.Message
import com.lijiahao.sharechargingpile2.data.TextMsgBody
import com.lijiahao.sharechargingpile2.databinding.ItemTextReceiveBinding


class ReceiveTextViewHolder(val binding: ItemTextReceiveBinding) :
    MessageViewHolder(binding.root) {
    override fun bind(message: Message) {
        val msgBody = message.msgBody as TextMsgBody
        binding.chatItemContentText.text = msgBody.message
    }

}