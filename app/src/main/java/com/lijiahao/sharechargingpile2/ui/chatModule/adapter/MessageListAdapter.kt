package com.lijiahao.sharechargingpile2.ui.chatModule.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lijiahao.sharechargingpile2.data.Message
import com.lijiahao.sharechargingpile2.data.MessageListItem
import com.lijiahao.sharechargingpile2.data.MsgType
import com.lijiahao.sharechargingpile2.data.TextMsgBody
import com.lijiahao.sharechargingpile2.databinding.ItemMessageBinding
import com.lijiahao.sharechargingpile2.di.GlideApp
import com.lijiahao.sharechargingpile2.ui.chatModule.MessageListFragment
import com.lijiahao.sharechargingpile2.ui.chatModule.MessageListFragmentDirections
import com.lijiahao.sharechargingpile2.utils.TimeUtils

class MessageListAdapter(private val messageListFragment: MessageListFragment) :
    ListAdapter<MessageListItem, MessageListAdapter.MyViewHolder>(MessageItemDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding, this, parent.context, messageListFragment)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    class MyViewHolder(
        val binding: ItemMessageBinding,
        private val adapter: MessageListAdapter,
        private val context: Context,
        private val fragment: MessageListFragment
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(messageListItem: MessageListItem) {
            // 加载用户图片
            GlideApp.with(context).load(messageListItem.user.avatarUrl)
                .into(binding.messageItemPhoto)

            // 加载用户名
            binding.messageItemTitle.text = messageListItem.user.name

            // 加载信息
            val message = messageListItem.message
            when (message.msgType) {
                MsgType.TEXT -> {
                    val msgBody = message.msgBody as TextMsgBody
                    binding.messageItemInfo.text = msgBody.message
                }
                MsgType.IMAGE -> {
                    binding.messageItemInfo.text = "[图片]"
                }
            }

            // 加载时间
            binding.messageItemTimeText.text =
                TimeUtils.getSendTimeText(messageListItem.message.sendTime)

            // 设置点击事件
            binding.messageItem.setOnClickListener {
                val userId = fragment.sharedPreferenceData.userId
                val otherId = if (message.sendId == userId) message.targetId else message.sendId
                val action = MessageListFragmentDirections.actionMessageListFragmentToChatFragment(otherId)
                fragment.findNavController().navigate(action)
            }

        }

    }

    class MessageItemDiffItemCallback : DiffUtil.ItemCallback<MessageListItem>() {

        override fun areItemsTheSame(oldItem: MessageListItem, newItem: MessageListItem): Boolean {
            return oldItem.user.userId == newItem.user.userId
        }

        override fun areContentsTheSame(
            oldItem: MessageListItem,
            newItem: MessageListItem
        ): Boolean {
            return oldItem == newItem
        }

    }
}
