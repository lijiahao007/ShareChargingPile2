package com.lijiahao.sharechargingpile2.ui.chatModule.adapter

import android.content.Context
import android.view.View
import com.lijiahao.sharechargingpile2.data.Message
import com.lijiahao.sharechargingpile2.data.MsgState
import com.lijiahao.sharechargingpile2.data.TextMsgBody
import com.lijiahao.sharechargingpile2.databinding.ItemTextSendBinding
import com.lijiahao.sharechargingpile2.di.GlideApp

class SendTextViewHolder(
    val binding: ItemTextSendBinding,
    private val avatarUrl: String
    ): MessageViewHolder(binding.root) {
    override fun bind(message: Message) {
        val msgBody = message.msgBody as TextMsgBody
        binding.chatItemContentText.text = msgBody.message
        GlideApp.with(binding.root).load(avatarUrl).into(binding.chatItemHeaderSend)
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