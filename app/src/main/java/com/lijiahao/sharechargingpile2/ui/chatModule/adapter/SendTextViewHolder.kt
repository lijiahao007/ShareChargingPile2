package com.lijiahao.sharechargingpile2.ui.chatModule.adapter

import android.view.View
import com.lijiahao.sharechargingpile2.data.Message
import com.lijiahao.sharechargingpile2.data.MsgState
import com.lijiahao.sharechargingpile2.data.TextMsgBody
import com.lijiahao.sharechargingpile2.databinding.ItemTextSendBinding

class SendTextViewHolder(val binding: ItemTextSendBinding): MessageViewHolder(binding.root) {
    override fun bind(message: Message) {
        val msgBody = message.msgBody as TextMsgBody
        binding.chatItemContentText.text = msgBody.message
        when (message.state) {
            MsgState.SENDING -> {
                binding.chatItemProgress.visibility = View.VISIBLE
                binding.chatItemFail.visibility = View.GONE
            }
            MsgState.FAILED -> {
                binding.chatItemProgress.visibility = View.GONE
                binding.chatItemFail.visibility = View.VISIBLE
            }
            MsgState.SENT -> {
                binding.chatItemProgress.visibility = View.GONE
                binding.chatItemFail.visibility = View.GONE
            }
        }
    }

}