package com.lijiahao.sharechargingpile2.ui.mapModule

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.lijiahao.sharechargingpile2.R
import com.lijiahao.sharechargingpile2.data.ChargingPile
import com.lijiahao.sharechargingpile2.databinding.FragmentChoosePileBinding
import com.lijiahao.sharechargingpile2.ui.mapModule.adapter.ChoosePileListAdapter
import com.lijiahao.sharechargingpile2.ui.mapModule.viewmodel.BookViewModel


class ChoosePileFragment : Fragment() {

    private val binding: FragmentChoosePileBinding by lazy {
        FragmentChoosePileBinding.inflate(
            layoutInflater
        )
    }

    private val args: ChoosePileFragmentArgs by navArgs()

    private val pileList: List<ChargingPile> by lazy {
        args.pileArray.toList()
    }

    lateinit var adapter: ChoosePileListAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        pileList.forEach {
            Log.e(TAG, "$it")
        }
        initUI()

        return binding.root
    }



    private fun initUI() {
        adapter = ChoosePileListAdapter(this)
        adapter.submitList(pileList)
        binding.recyclerView.adapter = adapter

        binding.close.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    companion object {
        const val TAG = "ChoosePileFragment"
        const val PILE_LIST_RESULT_KEY = "PILE_LIST_RESULT_KEY"
        const val PILE_LIST_BUNDLE_KEY = "PILE_LIST_BUNDLE_KEY"
    }
}