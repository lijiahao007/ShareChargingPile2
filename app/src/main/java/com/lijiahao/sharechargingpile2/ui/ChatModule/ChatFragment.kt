package com.lijiahao.sharechargingpile2.ui.ChatModule

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.lijiahao.sharechargingpile2.dao.MessageDao
import com.lijiahao.sharechargingpile2.data.Message
import com.lijiahao.sharechargingpile2.data.MsgState
import com.lijiahao.sharechargingpile2.data.MsgType
import com.lijiahao.sharechargingpile2.data.TextMsgBody
import com.lijiahao.sharechargingpile2.databinding.FragmentChatBinding
import com.lijiahao.sharechargingpile2.network.service.MessageService
import com.lijiahao.sharechargingpile2.repository.MessageRepository
import com.lijiahao.sharechargingpile2.ui.ChatModule.adapter.ChatAdapter
import com.lijiahao.sharechargingpile2.utils.SoftKeyBoardUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private val binding: FragmentChatBinding by lazy {
        FragmentChatBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var messageRepository: MessageRepository

    private lateinit var adapter: ChatAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initUI()
        return binding.root
    }


    private fun initUI() {
        initToolBar()
        initChatView()
        initBottomSendLayout()
    }

    private fun initToolBar() {
        binding.ivNavigationUp.setOnClickListener {
            navigateUp()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initBottomSendLayout() {
        binding.ivAdd.setOnClickListener {
            if (binding.addLayout.visibility == View.GONE) {
                binding.addLayout.visibility = View.VISIBLE
            } else {
                binding.addLayout.visibility = View.GONE
            }
        }

        binding.etContent.addTextChangedListener {
            if (binding.etContent.text.toString() != "") {
                binding.ivAdd.visibility = View.GONE
                binding.btnSend.visibility = View.VISIBLE
                binding.addLayout.visibility = View.GONE
            } else {
                binding.ivAdd.visibility = View.VISIBLE
                binding.btnSend.visibility = View.GONE
            }
        }

        // 空白地方说起键盘
        binding.rvChatList.setOnTouchListener { _, _ ->
            hideSoftKeyboard()
            false
        }

        binding.btnSend.setOnClickListener {
            // 发送按钮
            val list = ArrayList<Message>(adapter.currentList)
            val text = binding.etContent.text.toString()

            val message = Message(
                0,
                UUID.randomUUID().toString(),
                "1",
                "2",
                System.currentTimeMillis(),
                true,
                MsgType.TEXT,
                TextMsgBody(text, ""),
                MsgState.SENDING
            )
            // 设置成空白
            binding.etContent.setText("")
            list.add(message)
            adapter.submitList(list)

            // TODO ：把文本消息通过发送出去。并且存储在Room数据库中
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    messageRepository.sendAndSaveTextMessage(message)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Snackbar.make(binding.root, "出错了", Snackbar.LENGTH_SHORT).show()
                }

                withContext(Dispatchers.Main) {
                    val list1 = ArrayList<Message>(adapter.currentList)
                    val index = list1.indexOfFirst { it.uuid == message.uuid }
                    list1[index].state = MsgState.SENT
                    adapter.submitList(list1)
                    adapter.notifyItemChanged(index)
                    binding.rvChatList.smoothScrollToPosition(index)
                }
            }

        }
    }

    private fun initChatView() {
        adapter = ChatAdapter("1")
        binding.rvChatList.adapter = adapter
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }

    private fun hideSoftKeyboard() {
        SoftKeyBoardUtils.hideKeyBoard(requireActivity())
    }


    companion object {
        const val TAG = "ChatFragment"
    }

}