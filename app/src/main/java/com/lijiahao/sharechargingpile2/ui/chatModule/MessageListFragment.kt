package com.lijiahao.sharechargingpile2.ui.chatModule

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.lijiahao.sharechargingpile2.data.*
import com.lijiahao.sharechargingpile2.databinding.FragmentMessageListBinding
import com.lijiahao.sharechargingpile2.network.response.UserInfoResponse
import com.lijiahao.sharechargingpile2.ui.chatModule.adapter.MessageListAdapter
import com.lijiahao.sharechargingpile2.ui.mainModule.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.WebSocket

@AndroidEntryPoint
class MessageListFragment : Fragment() {

    private val binding: FragmentMessageListBinding by lazy {
        FragmentMessageListBinding.inflate(layoutInflater)
    }

    private val adapter = MessageListAdapter()

    // 初始化WebSocket（从MainActivity中获取）
    private val webSocket:WebSocket? by lazy {
        (requireActivity() as MainActivity).webSocket
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        initUI()

        return binding.root
    }


    private fun initUI() {
        binding.btnToChat.setOnClickListener {
            val action = MessageListFragmentDirections.actionMessageListFragmentToChatFragment()
            findNavController().navigate(action)
        }

        binding.messageRecyclerview.adapter = adapter

        Log.i(TAG, "websocket in MessageListFragmet = $webSocket")
    }

    companion object{
        const val TAG = "MessageListFragment"
    }
}