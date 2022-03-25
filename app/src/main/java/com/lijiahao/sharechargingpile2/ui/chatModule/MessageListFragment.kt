package com.lijiahao.sharechargingpile2.ui.chatModule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.lijiahao.sharechargingpile2.databinding.FragmentMessageListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MessageListFragment : Fragment() {

    private val binding: FragmentMessageListBinding by lazy {
        FragmentMessageListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding.btnToChat.setOnClickListener {
            val action = MessageListFragmentDirections.actionMessageListFragmentToChatFragment()
            findNavController().navigate(action)
        }
        return binding.root
    }
}