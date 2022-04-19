package com.lijiahao.sharechargingpile2.ui.mainModule.notifications

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.databinding.FragmentServiceOrderBinding


class ServiceOrderFragment : Fragment() {

    private val binding: FragmentServiceOrderBinding by lazy {
        FragmentServiceOrderBinding.inflate(layoutInflater)
    }

    private val viewModel: OrderViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

}