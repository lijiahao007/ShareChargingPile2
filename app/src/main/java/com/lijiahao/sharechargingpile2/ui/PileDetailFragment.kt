package com.lijiahao.sharechargingpile2.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.databinding.FragmentPileDetailBinding
import com.lijiahao.sharechargingpile2.ui.viewmodel.PileDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PileDetailFragment : Fragment() {

    private val binding: FragmentPileDetailBinding by lazy {
        FragmentPileDetailBinding.inflate(layoutInflater)
    }

    private val viewModel: PileDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding.ivBack.setOnClickListener {
            navigationBack()
        }

        return binding.root
    }

    private fun navigationBack() {
        findNavController().navigateUp()
    }

}