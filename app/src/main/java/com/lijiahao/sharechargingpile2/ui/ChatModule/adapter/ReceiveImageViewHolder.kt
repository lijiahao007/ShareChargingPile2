package com.lijiahao.sharechargingpile2.ui.ChatModule.adapter

import androidx.recyclerview.widget.RecyclerView
import com.lijiahao.sharechargingpile2.data.ImageMsgBody
import com.lijiahao.sharechargingpile2.data.Message
import com.lijiahao.sharechargingpile2.databinding.ItemImageReceiveBinding
import com.lijiahao.sharechargingpile2.di.GlideApp
import java.io.File


class ReceiveImageViewHolder(val binding: ItemImageReceiveBinding) :
    MessageViewHolder(binding.root) {
    override fun bind(message: Message) {
        val msgBody = message.msgBody as ImageMsgBody
        GlideApp.with(binding.root).load(File(msgBody.localPath)).into(binding.bivPic)
    }

}
