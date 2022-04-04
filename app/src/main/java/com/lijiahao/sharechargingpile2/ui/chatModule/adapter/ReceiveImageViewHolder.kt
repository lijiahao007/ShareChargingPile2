package com.lijiahao.sharechargingpile2.ui.chatModule.adapter

import com.lijiahao.sharechargingpile2.data.ImageMsgBody
import com.lijiahao.sharechargingpile2.data.Message
import com.lijiahao.sharechargingpile2.databinding.ItemImageReceiveBinding
import com.lijiahao.sharechargingpile2.di.GlideApp
import java.io.File


class ReceiveImageViewHolder(val binding: ItemImageReceiveBinding) :
    MessageViewHolder(binding.root) {
    override fun bind(message: Message) {
        val msgBody = message.msgBody as ImageMsgBody
        // 如果有本地地址，就用本地地址。没有就用远程地址
        if (msgBody.localPath == "") {
            GlideApp.with(binding.root).load(msgBody.remotePath).into(binding.bivPic)
        } else {
            GlideApp.with(binding.root).load(File(msgBody.localPath)).into(binding.bivPic)
        }
    }

}
