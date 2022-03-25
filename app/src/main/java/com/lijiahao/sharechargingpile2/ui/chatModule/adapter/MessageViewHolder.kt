package com.lijiahao.sharechargingpile2.ui.chatModule.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.lijiahao.sharechargingpile2.data.Message

abstract class MessageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    abstract fun bind(message: Message)
}