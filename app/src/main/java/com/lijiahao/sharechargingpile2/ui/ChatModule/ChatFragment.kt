package com.lijiahao.sharechargingpile2.ui.ChatModule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.databinding.FragmentChatBinding

class ChatFragment : Fragment() {

    private val binding: FragmentChatBinding by lazy {
        FragmentChatBinding.inflate(layoutInflater)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

}