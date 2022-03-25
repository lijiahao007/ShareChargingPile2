package com.lijiahao.sharechargingpile2.ui.chatModule.adapter

import android.view.View
import com.lijiahao.sharechargingpile2.data.ImageMsgBody
import com.lijiahao.sharechargingpile2.data.Message
import com.lijiahao.sharechargingpile2.data.MsgState
import com.lijiahao.sharechargingpile2.databinding.ItemImageSendBinding
import com.lijiahao.sharechargingpile2.di.GlideApp
import java.io.File


class SendImageViewHolder(val binding: ItemImageSendBinding) :
    MessageViewHolder(binding.root){
    override fun bind(message: Message) {
        val msgBody = message.msgBody as ImageMsgBody
        GlideApp.with(binding.root).load(File(msgBody.localPath)).into(binding.bivPic)
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