package com.lijiahao.sharechargingpile2.ui.mainModule.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.lijiahao.sharechargingpile2.databinding.FragmentHomeBinding
import com.lijiahao.sharechargingpile2.ui.mapModule.MapActivity
import com.lijiahao.sharechargingpile2.ui.mapModule.MapModuleActivity
import com.lijiahao.sharechargingpile2.ui.publishStationModule.PublishStationActivity

class HomeFragment : Fragment() {


    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        requireActivity().window.apply {
//            statusBarColor = Color.TRANSPARENT
//            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//        }

        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.cardPublishChargingStation.setOnClickListener {
            val intent = Intent(context, PublishStationActivity::class.java)
            startActivity(intent)
        }

        binding.cardSearchPublish.setOnClickListener {
            val intent = Intent(context, MapModuleActivity::class.java)
            startActivity(intent)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}