package com.lijiahao.sharechargingpile2.ui.chatModule

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.lijiahao.sharechargingpile2.dao.MessageDao
import com.lijiahao.sharechargingpile2.data.*
import com.lijiahao.sharechargingpile2.databinding.FragmentMessageListBinding
import com.lijiahao.sharechargingpile2.ui.broadcastreceiver.MessageReceiver
import com.lijiahao.sharechargingpile2.ui.chatModule.adapter.MessageListAdapter
import com.lijiahao.sharechargingpile2.ui.chatModule.viewmodel.MessageListViewModel
import com.lijiahao.sharechargingpile2.ui.mainModule.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import okhttp3.WebSocket
import javax.inject.Inject

@AndroidEntryPoint
class MessageListFragment : Fragment() {

    private val binding: FragmentMessageListBinding by lazy {
        FragmentMessageListBinding.inflate(layoutInflater)
    }

    private val adapter = MessageListAdapter(this)

    // 该viewModel数据在整个聊天模块共享
    private val viewModel: MessageListViewModel by activityViewModels()

    @Inject
    lateinit var messageDao: MessageDao
    @Inject
    lateinit var sharedPreferenceData: SharedPreferenceData

    // 初始化WebSocket（从MainActivity中获取）
    private val webSocket: WebSocket? by lazy {
        (requireActivity() as MainActivity).webSocket
    }

    lateinit var messageReceiver: MessageReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBroadcastReceiver()
    }

    private fun initBroadcastReceiver() {
        messageReceiver = object : MessageReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                super.onReceive(context, intent)
                message?.let { msg ->
                    val list = ArrayList(adapter.currentList)
                    var pos = -1
                    val userId = sharedPreferenceData.userId // 当前用户ID
                    val otherId = if (msg.sendId == userId) msg.targetId else msg.sendId
                    list.forEachIndexed { index, messageListItem ->
                        if (messageListItem.user.userId == otherId) {
                            pos = index
                            return@forEachIndexed
                        }
                    }
                    if (pos != -1) {
                        // 如果该用户已经存在，就调整以下位置
                        val item = list[pos]
                        item.message = msg
                        list[pos] = list[0]
                        list[0] = item
                        adapter.submitList(list)
                        adapter.notifyItemChanged(0)
                        adapter.notifyItemChanged(pos)
                    } else {
                        // 如果该用户不存在，则获取用户信息，然后重新设置列表信息
                        viewModel.addUserInfoResponse(otherId)
                    }
                }
            }
        }
        context?.registerReceiver(messageReceiver, messageReceiver.getIntentFilter())
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel // 加载以下
        initUI()

        return binding.root
    }


    private fun initUI() {

        binding.messageRecyclerview.adapter = adapter

        viewModel.userInfoResponseList.observe(this) { userInfoResponseList ->
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                // 1. 从数据库中获取所有的用户
                val userIdList = ArrayList<String>()
                userInfoResponseList.forEach {
                    userIdList.add(it.userId)
                }
                val messageList = messageDao.queryLatestMessage(userIdList)
                // 2. 构建MessageListItem
                val itemList = ArrayList<MessageListItem>()
                messageList.forEachIndexed { index, message ->
                    val item = MessageListItem(message, userInfoResponseList[index])
                    itemList.add(item)
                }
                itemList.sortByDescending { it.message.sendTime }

                itemList.forEach {
                    Log.i(TAG, "${it.message.sendTime}")
                }

                withContext(Dispatchers.Main) {
                    adapter.submitList(itemList)
                    binding.swipeMessage.isRefreshing = false
                }
            }
        }

        binding.swipeMessage.setOnRefreshListener {
            viewModel.userInfoResponseList.value?.let {
                viewModel.getData() // 重新获取数据
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(messageReceiver)
    }

    companion object {
        const val TAG = "MessageListFragment"
    }
}